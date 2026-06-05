const state = {
  view: "dashboard",
  currentAdmin: null,
  token: localStorage.getItem("adminToken") || "",
  selectedRoleId: null,
  roles: [],
  permissions: [],
  rolePermissions: {},
  rolePermissionDraft: new Set(),
};

const $ = (selector) => document.querySelector(selector);
const $$ = (selector) => Array.from(document.querySelectorAll(selector));

const pageNames = {
  dashboard: "看板总览",
  artifacts: "文物数据管理",
  platformUsers: "平台用户管理",
  adminUsers: "管理员管理",
  roles: "角色权限管理",
  content: "内容审核",
  sensitive: "敏感词库",
  logs: "日志与备份",
};

function apiBase() {
  return $("#apiBase").value.replace(/\/$/, "");
}

async function request(path, options = {}) {
  const headers = {
    "Content-Type": "application/json; charset=utf-8",
    ...(state.token ? { Authorization: `Bearer ${state.token}` } : {}),
    ...(options.headers || {}),
  };
  const response = await fetch(`${apiBase()}${path}`, {
    ...options,
    headers,
  });
  const result = await response.json();
  if (response.status === 401) {
    logout();
    throw new Error(result.message || "请重新登录");
  }
  if (!response.ok) throw new Error(result.message || `HTTP ${response.status}`);
  if (result.code !== 200) throw new Error(result.message || "接口返回失败");
  return result.data;
}

async function download(path, filename) {
  const response = await fetch(`${apiBase()}${path}`, {
    headers: {
      ...(state.token ? { Authorization: `Bearer ${state.token}` } : {}),
    },
  });
  if (!response.ok) throw new Error(`下载失败：HTTP ${response.status}`);
  const blob = await response.blob();
  const url = URL.createObjectURL(blob);
  const link = document.createElement("a");
  link.href = url;
  link.download = filename;
  document.body.appendChild(link);
  link.click();
  link.remove();
  URL.revokeObjectURL(url);
}

function qs(params) {
  const query = new URLSearchParams();
  Object.entries(params).forEach(([key, value]) => {
    if (value !== undefined && value !== null && value !== "") query.set(key, value);
  });
  return query.toString();
}

function escapeHtml(value) {
  return String(value ?? "")
    .replaceAll("&", "&amp;")
    .replaceAll("<", "&lt;")
    .replaceAll(">", "&gt;")
    .replaceAll('"', "&quot;");
}

function fmt(value) {
  if (!value) return "-";
  return String(value).replace("T", " ").slice(0, 19);
}

function records(data) {
  if (Array.isArray(data)) return data;
  return data?.records || [];
}

function badge(value, labels = {}) {
  const text = labels[value] ?? labels[String(value)] ?? value ?? "未知";
  let cls = "";
  if (value === 1 || value === "1" || value === "success") cls = "ok";
  if (value === 0 || value === "0" || value === 3 || value === "3") cls = "warn";
  if (value === 2 || value === "2" || value === "danger") cls = "danger";
  return `<span class="badge ${cls}">${escapeHtml(text)}</span>`;
}

function showAlert(message, type = "info") {
  const alert = $("#alert");
  alert.textContent = message;
  alert.className = `alert ${type === "error" ? "error" : ""}`;
  clearTimeout(showAlert.timer);
  showAlert.timer = setTimeout(() => alert.classList.add("hidden"), 3500);
}

function showDetail(title, data) {
  $("#detailTitle").textContent = title;
  $("#detailBody").textContent = JSON.stringify(data, null, 2);
  $("#detailModal").showModal();
}

function openForm(title, html, onSubmit) {
  $("#modalTitle").textContent = title;
  $("#modalBody").innerHTML = html;
  $("#modal").showModal();
  $("#modalForm").onsubmit = async (event) => {
    event.preventDefault();
    try {
      await onSubmit(new FormData(event.currentTarget));
      $("#modal").close();
    } catch (error) {
      showAlert(`保存失败：${error.message}`, "error");
    }
  };
}

function formValue(form, key, fallback = "") {
  const value = form.get(key);
  return value === null || value === "" ? fallback : value;
}

function numberOrNull(value) {
  return value === "" || value === null || value === undefined ? null : Number(value);
}

function dateTimeParam(selector) {
  const value = $(selector)?.value;
  return value ? `${value}:00` : "";
}

function checkedIds(selector) {
  return $$(selector).filter((item) => item.checked).map((item) => Number(item.value));
}

async function login(event) {
  event.preventDefault();
  $("#loginMessage").textContent = "";
  try {
    const loginResult = await request("/api/admin/auth/login", {
      method: "POST",
      body: JSON.stringify({
        username: $("#loginUsername").value.trim(),
        password: $("#loginPassword").value,
      }),
    });
    state.token = loginResult.token;
    localStorage.setItem("adminToken", loginResult.token);
    const admin = loginResult.adminUser;
    state.currentAdmin = admin;
    $("#operatorName").textContent = admin.realName || admin.username;
    $("#loginScreen").classList.add("hidden");
    $("#appScreen").classList.remove("hidden");
    await loadAll();
  } catch (error) {
    $("#loginMessage").textContent = `登录失败：${error.message}`;
  }
}

function logout() {
  state.currentAdmin = null;
  state.token = "";
  localStorage.removeItem("adminToken");
  $("#appScreen").classList.add("hidden");
  $("#loginScreen").classList.remove("hidden");
}

async function loadAll() {
  await Promise.allSettled([
    loadDashboard(),
    loadArtifacts(),
    loadPlatformUsers(),
    loadAdminUsers(),
    loadRoles(),
    loadContent(),
    loadSensitiveWords(),
    loadLogs(),
  ]);
}

async function loadCurrent() {
  const loaders = {
    dashboard: loadDashboard,
    artifacts: loadArtifacts,
    platformUsers: loadPlatformUsers,
    adminUsers: loadAdminUsers,
    roles: loadRoles,
    content: loadContent,
    sensitive: loadSensitiveWords,
    logs: loadLogs,
  };
  try {
    await loaders[state.view]();
    showAlert("当前模块已刷新");
  } catch (error) {
    showAlert(`加载失败：${error.message}`, "error");
  }
}

async function loadDashboard() {
  const [summary, artifactStat, contentStat] = await Promise.all([
    request("/api/admin/dashboard/summary"),
    request("/api/admin/dashboard/artifact-stat"),
    request("/api/admin/dashboard/content-stat"),
  ]);
  const metrics = [
    ["文物总量", summary.artifactCount ?? 0],
    ["平台用户", summary.userCount ?? 0],
    ["待审核内容", summary.pendingCount ?? summary.pendingContentCount ?? 0],
    ["今日提交", summary.todayContentCount ?? 0],
  ];
  $("#metricGrid").innerHTML = metrics.map(([label, value]) => `
    <article class="metric"><span>${label}</span><strong>${value}</strong></article>
  `).join("");
  renderBars("#artifactBars", normalizeStat(artifactStat), "暂无统计数据");
  renderBars("#contentBars", normalizeStat(contentStat), "暂无审核数据");
}

function normalizeStat(data) {
  if (!data) return [];
  if (Array.isArray(data)) return data;
  return Object.entries(data).map(([name, value]) => ({ name, value }));
}

function renderBars(selector, list, empty) {
  const max = Math.max(...list.map((item) => Number(item.value ?? item.count ?? 0)), 1);
  $(selector).innerHTML = list.length ? list.map((item) => {
    const label = item.name ?? item.type ?? item.status ?? "未分类";
    const value = Number(item.value ?? item.count ?? 0);
    return `
      <div class="bar-row">
        <span>${escapeHtml(label)}</span>
        <div class="bar-track"><div class="bar-fill" style="width:${Math.max(4, value / max * 100)}%"></div></div>
        <strong>${value}</strong>
      </div>
    `;
  }).join("") : `<p class="hint">${empty}</p>`;
}

async function loadArtifacts() {
  const data = await request(`/api/admin/artifacts/page?${qs({
    pageNum: 1,
    pageSize: 50,
    title: $("#artifactTitle")?.value,
    type: $("#artifactType")?.value,
    museum: $("#artifactMuseum")?.value,
  })}`);
  $("#artifactTotal").textContent = `共 ${data.total ?? 0} 条`;
  $("#artifactTable").innerHTML = records(data).map((item) => `
    <tr>
      <td>${item.id}</td>
      <td><strong>${escapeHtml(item.title)}</strong><br><span class="hint">${escapeHtml(item.objectId)}</span></td>
      <td>${escapeHtml(item.period)}</td>
      <td>${escapeHtml(item.type)}</td>
      <td>${escapeHtml(item.museum)}</td>
      <td>${badge(item.auditStatus, {0: "待审核", 1: "发布", 2: "下架"})} ${badge(item.kgSyncStatus, {0: "未同步", 1: "已同步", 2: "同步失败"})}</td>
      <td class="actions">
        <button onclick="viewArtifact(${item.id})">详情</button>
        <button onclick="openArtifactEdit(${item.id})">编辑</button>
        <button class="secondary" onclick="deleteArtifact(${item.id})">删除</button>
      </td>
    </tr>
  `).join("") || `<tr><td colspan="7">暂无文物数据</td></tr>`;
}

function artifactForm(item = {}) {
  return `
    <div class="form-grid">
      <label>文物唯一标识<input name="objectId" value="${escapeHtml(item.objectId || `front_${Date.now()}`)}" required></label>
      <label>文物名称<input name="title" value="${escapeHtml(item.title || "")}" required></label>
      <label>年代/时期<input name="period" value="${escapeHtml(item.period || "")}" required></label>
      <label>类型<input name="type" value="${escapeHtml(item.type || "")}" required></label>
      <label>材质<input name="material" value="${escapeHtml(item.material || "")}"></label>
      <label>尺寸<input name="dimensions" value="${escapeHtml(item.dimensions || "")}"></label>
      <label>所属博物馆<input name="museum" value="${escapeHtml(item.museum || "")}" required></label>
      <label>所在地<input name="location" value="${escapeHtml(item.location || "")}" required></label>
      <label>详情页 URL<input name="detailUrl" value="${escapeHtml(item.detailUrl || "https://example.com/artifact")}" required></label>
      <label>图片 URL<input name="imageUrl" value="${escapeHtml(item.imageUrl || "https://example.com/image.jpg")}" required></label>
      <label>图片本地路径<input name="imagePath" value="${escapeHtml(item.imagePath || "images/demo.jpg")}" required></label>
      <label>藏品编号<input name="accessionNumber" value="${escapeHtml(item.accessionNumber || "")}"></label>
      <label>版权来源<input name="creditLine" value="${escapeHtml(item.creditLine || "")}"></label>
      <label>爬取日期<input name="crawlDate" type="date" value="${escapeHtml(item.crawlDate || "2026-05-29")}" required></label>
      <label>发布状态
        <select name="auditStatus">
          <option value="1" ${item.auditStatus === 1 ? "selected" : ""}>已发布</option>
          <option value="0" ${item.auditStatus === 0 ? "selected" : ""}>待审核</option>
          <option value="2" ${item.auditStatus === 2 ? "selected" : ""}>已下架</option>
        </select>
      </label>
      <label>图谱同步
        <select name="kgSyncStatus">
          <option value="0" ${item.kgSyncStatus === 0 ? "selected" : ""}>未同步</option>
          <option value="1" ${item.kgSyncStatus === 1 ? "selected" : ""}>已同步</option>
          <option value="2" ${item.kgSyncStatus === 2 ? "selected" : ""}>同步失败</option>
        </select>
      </label>
      <label class="wide">介绍<textarea name="description" required>${escapeHtml(item.description || "")}</textarea></label>
    </div>
  `;
}

function artifactPayload(form) {
  return {
    objectId: formValue(form, "objectId"),
    title: formValue(form, "title"),
    period: formValue(form, "period"),
    type: formValue(form, "type"),
    material: formValue(form, "material"),
    description: formValue(form, "description"),
    dimensions: formValue(form, "dimensions"),
    museum: formValue(form, "museum"),
    location: formValue(form, "location"),
    detailUrl: formValue(form, "detailUrl"),
    imageUrl: formValue(form, "imageUrl"),
    imagePath: formValue(form, "imagePath"),
    creditLine: formValue(form, "creditLine"),
    accessionNumber: formValue(form, "accessionNumber"),
    crawlDate: formValue(form, "crawlDate"),
    auditStatus: Number(formValue(form, "auditStatus", 1)),
    kgSyncStatus: Number(formValue(form, "kgSyncStatus", 0)),
  };
}

function openArtifactCreate() {
  openForm("新增文物", artifactForm(), async (form) => {
    await request("/api/admin/artifacts", { method: "POST", body: JSON.stringify(artifactPayload(form)) });
    showAlert("文物已新增");
    await loadArtifacts();
    await loadDashboard();
  });
}

async function openArtifactEdit(id) {
  const item = await request(`/api/admin/artifacts/${id}`);
  openForm("编辑文物", artifactForm(item), async (form) => {
    await request(`/api/admin/artifacts/${id}`, { method: "PUT", body: JSON.stringify(artifactPayload(form)) });
    showAlert("文物已更新");
    await loadArtifacts();
    await loadDashboard();
  });
}

async function viewArtifact(id) {
  showDetail("文物详情", await request(`/api/admin/artifacts/${id}`));
}

async function deleteArtifact(id) {
  if (!confirm("确定删除该文物吗？")) return;
  await request(`/api/admin/artifacts/${id}`, { method: "DELETE" });
  showAlert("文物已删除");
  await loadArtifacts();
  await loadDashboard();
}

async function loadPlatformUsers() {
  const data = await request(`/api/admin/platform-users/page?${qs({
    pageNum: 1,
    pageSize: 50,
    username: $("#platformUsername")?.value,
    source: $("#platformSource")?.value,
    status: $("#platformStatus")?.value,
  })}`);
  $("#platformUserTable").innerHTML = records(data).map((user) => `
    <tr>
      <td><input type="checkbox" class="platform-user-check" value="${user.id}"></td>
      <td>${user.id}</td>
      <td><strong>${escapeHtml(user.username)}</strong></td>
      <td>${escapeHtml(user.phone || "-")}<br><span class="hint">${escapeHtml(user.email || "-")}</span></td>
      <td>${escapeHtml(user.source)}</td>
      <td>${badge(user.status, {0: "禁用", 1: "启用"})}</td>
      <td>${badge(user.banComment, {0: "可评论", 1: "禁评"})} ${badge(user.banUpload, {0: "可上传", 1: "禁上传"})}</td>
      <td class="actions">
        <button onclick="openPlatformUserEdit(${user.id})">编辑</button>
        <button onclick="togglePlatformStatus(${user.id}, ${user.status === 1 ? 0 : 1})">${user.status === 1 ? "禁用" : "启用"}</button>
        <button onclick="togglePlatformComment(${user.id}, ${user.banComment === 1 ? 0 : 1})">${user.banComment === 1 ? "允许评论" : "禁止评论"}</button>
        <button onclick="togglePlatformUpload(${user.id}, ${user.banUpload === 1 ? 0 : 1})">${user.banUpload === 1 ? "允许上传" : "禁止上传"}</button>
        <button class="secondary" onclick="viewPlatformContents(${user.id})">内容</button>
        <button class="secondary" onclick="deletePlatformUser(${user.id})">删除</button>
      </td>
    </tr>
  `).join("") || `<tr><td colspan="8">暂无用户</td></tr>`;
}

async function batchPlatformUsers(type) {
  const ids = checkedIds(".platform-user-check");
  if (!ids.length) return showAlert("请先勾选用户", "error");
  const actions = {
    status: ["/api/admin/platform-users/batch/status", { ids, status: 0 }, "已批量禁用用户"],
    banComment: ["/api/admin/platform-users/batch/ban-comment", { ids, banComment: 1 }, "已批量禁止评论"],
    banUpload: ["/api/admin/platform-users/batch/ban-upload", { ids, banUpload: 1 }, "已批量禁止上传"],
  };
  const [path, body, message] = actions[type];
  await request(path, { method: "PUT", body: JSON.stringify(body) });
  showAlert(message);
  await loadPlatformUsers();
}

function platformUserForm(user = {}) {
  return `
    <div class="form-grid">
      <label>用户名<input name="username" value="${escapeHtml(user.username || "")}" required></label>
      <label>手机号<input name="phone" value="${escapeHtml(user.phone || "")}"></label>
      <label>邮箱<input name="email" value="${escapeHtml(user.email || "")}"></label>
      <label>头像URL<input name="avatar" value="${escapeHtml(user.avatar || "")}"></label>
      <label>来源
        <select name="source">
          <option value="WEB" ${user.source === "WEB" ? "selected" : ""}>WEB</option>
          <option value="APP" ${user.source === "APP" ? "selected" : ""}>APP</option>
          <option value="KNOWLEDGE" ${user.source === "KNOWLEDGE" ? "selected" : ""}>知识服务子系统</option>
          <option value="MUSEUM" ${user.source === "MUSEUM" ? "selected" : ""}>掌上博物馆</option>
        </select>
      </label>
      <label>账号状态
        <select name="status">
          <option value="1" ${Number(user.status ?? 1) === 1 ? "selected" : ""}>启用</option>
          <option value="0" ${Number(user.status) === 0 ? "selected" : ""}>禁用</option>
        </select>
      </label>
      <label>评论权限
        <select name="banComment">
          <option value="0" ${Number(user.banComment ?? 0) === 0 ? "selected" : ""}>允许评论</option>
          <option value="1" ${Number(user.banComment) === 1 ? "selected" : ""}>禁止评论</option>
        </select>
      </label>
      <label>上传权限
        <select name="banUpload">
          <option value="0" ${Number(user.banUpload ?? 0) === 0 ? "selected" : ""}>允许上传</option>
          <option value="1" ${Number(user.banUpload) === 1 ? "selected" : ""}>禁止上传</option>
        </select>
      </label>
    </div>
  `;
}

function platformUserPayload(form) {
  return {
    username: formValue(form, "username"),
    phone: formValue(form, "phone"),
    email: formValue(form, "email"),
    avatar: formValue(form, "avatar"),
    source: formValue(form, "source", "WEB"),
    status: Number(formValue(form, "status", 1)),
    banComment: Number(formValue(form, "banComment", 0)),
    banUpload: Number(formValue(form, "banUpload", 0)),
  };
}

function openPlatformUserCreate() {
  openForm("新增平台用户", platformUserForm(), async (form) => {
    await request("/api/admin/platform-users", { method: "POST", body: JSON.stringify(platformUserPayload(form)) });
    showAlert("平台用户已新增");
    await loadPlatformUsers();
    await loadDashboard();
  });
}

async function openPlatformUserEdit(id) {
  const user = await request(`/api/admin/platform-users/${id}`);
  openForm("编辑平台用户", platformUserForm(user), async (form) => {
    await request(`/api/admin/platform-users/${id}`, { method: "PUT", body: JSON.stringify(platformUserPayload(form)) });
    showAlert("平台用户已更新");
    await loadPlatformUsers();
  });
}

async function deletePlatformUser(id) {
  if (!confirm("确定删除该平台用户吗？")) return;
  await request(`/api/admin/platform-users/${id}`, { method: "DELETE" });
  showAlert("平台用户已删除");
  await loadPlatformUsers();
  await loadDashboard();
}

async function togglePlatformStatus(id, status) {
  await request(`/api/admin/platform-users/${id}/status`, { method: "PUT", body: JSON.stringify({ status }) });
  showAlert("用户状态已更新");
  await loadPlatformUsers();
}

async function togglePlatformComment(id, banComment) {
  await request(`/api/admin/platform-users/${id}/ban-comment`, { method: "PUT", body: JSON.stringify({ banComment }) });
  showAlert("评论权限已更新");
  await loadPlatformUsers();
}

async function togglePlatformUpload(id, banUpload) {
  await request(`/api/admin/platform-users/${id}/ban-upload`, { method: "PUT", body: JSON.stringify({ banUpload }) });
  showAlert("上传权限已更新");
  await loadPlatformUsers();
}

async function viewPlatformContents(id) {
  showDetail("用户提交内容", await request(`/api/admin/platform-users/${id}/contents`));
}

async function loadAdminUsers() {
  const data = await request(`/api/admin/admin-users/page?${qs({
    pageNum: 1,
    pageSize: 50,
    username: $("#adminUsername")?.value,
  })}`);
  $("#adminUserTable").innerHTML = records(data).map((user) => `
    <tr>
      <td>${user.id}</td>
      <td><strong>${escapeHtml(user.username)}</strong></td>
      <td>${escapeHtml(user.realName || "-")}</td>
      <td>${escapeHtml(user.roleName || user.roleId)}</td>
      <td>${badge(user.status, {0: "禁用", 1: "启用"})}</td>
      <td>${fmt(user.lastLoginTime)}</td>
      <td class="actions">
        <button onclick="openAdminEdit(${user.id})">编辑</button>
        <button class="secondary" onclick="toggleAdminStatus(${user.id}, ${user.status === 1 ? 0 : 1})">${user.status === 1 ? "禁用" : "启用"}</button>
      </td>
    </tr>
  `).join("") || `<tr><td colspan="7">暂无管理员</td></tr>`;
}

function adminForm(user = {}) {
  const adminRoles = state.roles.filter((role) => role.roleCode !== "NORMAL_USER");
  const roleOptions = adminRoles.map((role) => `<option value="${role.id}" ${Number(user.roleId) === Number(role.id) ? "selected" : ""}>${escapeHtml(role.roleName)}</option>`).join("");
  return `
    <div class="form-grid">
      <label>账号<input name="username" value="${escapeHtml(user.username || "")}" required></label>
      <label>密码<input name="password" value="${escapeHtml(user.password || "123456")}" required></label>
      <label>真实姓名<input name="realName" value="${escapeHtml(user.realName || "")}"></label>
      <label>角色<select name="roleId" required>${roleOptions}</select></label>
    </div>
  `;
}

function adminPayload(form) {
  return {
    username: formValue(form, "username"),
    password: formValue(form, "password"),
    realName: formValue(form, "realName"),
    roleId: Number(formValue(form, "roleId", 1)),
  };
}

async function openAdminCreate() {
  if (!state.roles.length) await loadRoles();
  openForm("新增管理员", adminForm(), async (form) => {
    await request("/api/admin/admin-users", { method: "POST", body: JSON.stringify(adminPayload(form)) });
    showAlert("管理员已新增");
    await loadAdminUsers();
  });
}

async function openAdminEdit(id) {
  if (!state.roles.length) await loadRoles();
  const list = records(await request("/api/admin/admin-users/page?pageNum=1&pageSize=100"));
  const user = list.find((item) => Number(item.id) === Number(id));
  openForm("编辑管理员", adminForm(user), async (form) => {
    await request(`/api/admin/admin-users/${id}`, { method: "PUT", body: JSON.stringify(adminPayload(form)) });
    showAlert("管理员已更新");
    await loadAdminUsers();
  });
}

async function toggleAdminStatus(id, status) {
  await request(`/api/admin/admin-users/${id}/status`, { method: "PUT", body: JSON.stringify({ status }) });
  showAlert("管理员状态已更新");
  await loadAdminUsers();
}

async function loadRoles() {
  const [roles, permissions] = await Promise.all([
    request("/api/admin/roles"),
    request("/api/admin/permissions"),
  ]);
  state.roles = records(roles);
  state.permissions = records(permissions);
  const rolePermissionEntries = await Promise.all(
    state.roles.map(async (role) => [role.id, records(await request(`/api/admin/roles/${role.id}/permissions`))])
  );
  state.rolePermissions = Object.fromEntries(rolePermissionEntries);
  if (!state.selectedRoleId && state.roles[0]) state.selectedRoleId = state.roles[0].id;
  syncRolePermissionDraft();
  renderRoles();
  renderPermissions();
}

function renderRoles() {
  $("#roleList").innerHTML = state.roles.map((role) => `
    <article class="role-card ${Number(role.id) === Number(state.selectedRoleId) ? "active" : ""}" onclick="selectRole(${role.id})">
      <strong>${escapeHtml(role.roleName)}</strong>
      <span>${escapeHtml(role.roleCode)}</span>
      <p>${escapeHtml(role.description || "无说明")}</p>
      <div class="role-permissions">
        ${renderRolePermissionBadges(role.id)}
      </div>
      <div class="actions inline-actions">
        <button onclick="event.stopPropagation(); openRoleEdit(${role.id})">编辑</button>
        <button class="secondary" onclick="event.stopPropagation(); deleteRole(${role.id})">删除</button>
      </div>
    </article>
  `).join("") || `<p class="hint">暂无角色</p>`;
}

function roleForm(role = {}) {
  return `
    <div class="form-grid">
      <label>角色名称<input name="roleName" value="${escapeHtml(role.roleName || "")}" required></label>
      <label>角色编码<input name="roleCode" value="${escapeHtml(role.roleCode || "")}" required></label>
      <label class="wide">角色说明<textarea name="description">${escapeHtml(role.description || "")}</textarea></label>
    </div>
  `;
}

function rolePayload(form) {
  return {
    roleName: formValue(form, "roleName"),
    roleCode: formValue(form, "roleCode"),
    description: formValue(form, "description"),
  };
}

function openRoleCreate() {
  openForm("新增角色", roleForm(), async (form) => {
    await request("/api/admin/roles", { method: "POST", body: JSON.stringify(rolePayload(form)) });
    showAlert("角色已新增");
    await loadRoles();
  });
}

function openRoleEdit(id) {
  const role = state.roles.find((item) => Number(item.id) === Number(id));
  openForm("编辑角色", roleForm(role), async (form) => {
    await request(`/api/admin/roles/${id}`, { method: "PUT", body: JSON.stringify(rolePayload(form)) });
    showAlert("角色已更新");
    await loadRoles();
  });
}

async function deleteRole(id) {
  if (!confirm("确定删除该角色吗？内置角色不允许删除。")) return;
  await request(`/api/admin/roles/${id}`, { method: "DELETE" });
  showAlert("角色已删除");
  if (Number(state.selectedRoleId) === Number(id)) state.selectedRoleId = null;
  await loadRoles();
}

function renderRolePermissionBadges(roleId) {
  const permissions = state.rolePermissions[roleId] || [];
  if (!permissions.length) return `<span class="permission-chip muted">未分配权限</span>`;
  return permissions.map((permission) => `
    <span class="permission-chip">${escapeHtml(permission.permissionName)}</span>
  `).join("");
}

function renderPermissions() {
  const role = state.roles.find((item) => Number(item.id) === Number(state.selectedRoleId));
  const currentPermissions = state.rolePermissions[state.selectedRoleId] || [];
  $("#permissionTitle").textContent = role ? `${role.roleName}权限分配（当前 ${currentPermissions.length} 项）` : "权限分配";
  $("#permissionList").innerHTML = state.permissions.map((permission) => `
    <label class="check-item">
      <input type="checkbox" value="${permission.id}" ${state.rolePermissionDraft.has(Number(permission.id)) ? "checked" : ""}>
      <span>
        <strong>${escapeHtml(permission.permissionName)}</strong>
        <small>${escapeHtml(permission.moduleName)} / ${escapeHtml(permission.permissionCode)}</small>
      </span>
    </label>
  `).join("") || `<p class="hint">暂无权限</p>`;
}

function selectRole(id) {
  state.selectedRoleId = id;
  syncRolePermissionDraft();
  renderRoles();
  renderPermissions();
  showAlert("已选择角色，可勾选权限后保存");
}

function syncRolePermissionDraft() {
  state.rolePermissionDraft = new Set((state.rolePermissions[state.selectedRoleId] || []).map((permission) => Number(permission.id)));
}

async function saveRolePermissions() {
  if (!state.selectedRoleId) return showAlert("请先选择角色", "error");
  const permissionIds = $$("#permissionList input:checked").map((input) => Number(input.value));
  await request(`/api/admin/roles/${state.selectedRoleId}/permissions`, {
    method: "PUT",
    body: JSON.stringify({ permissionIds }),
  });
  showAlert("权限已保存");
  state.rolePermissions[state.selectedRoleId] = state.permissions.filter((permission) => permissionIds.includes(Number(permission.id)));
  syncRolePermissionDraft();
  renderRoles();
  renderPermissions();
}

async function loadContent() {
  const data = await request(`/api/admin/content/audit/page?${qs({
    pageNum: 1,
    pageSize: 50,
    auditStatus: $("#contentStatus")?.value,
    contentType: $("#contentType")?.value,
  })}`);
  $("#contentTable").innerHTML = records(data).map((item) => `
    <tr>
      <td><input type="checkbox" class="content-check" value="${item.id}"></td>
      <td>${item.id}</td>
      <td>${item.userId}</td>
      <td>${escapeHtml(item.contentType)}</td>
      <td>${escapeHtml(item.artifactObjectId || "-")}</td>
      <td>${escapeHtml(item.contentText || item.fileUrl || "-")}</td>
      <td>${badge(item.auditStatus, {0: "待审核", 1: "通过", 2: "拒绝", 3: "复审"})}</td>
      <td class="actions">
        <button onclick="approveContent(${item.id})">通过</button>
        <button onclick="rejectContent(${item.id})">拒绝</button>
        <button onclick="recheckContent(${item.id})">复审</button>
        <button class="secondary" onclick="deleteContent(${item.id})">删除</button>
      </td>
    </tr>
  `).join("") || `<tr><td colspan="8">暂无内容</td></tr>`;
}

async function batchApproveContents() {
  const ids = checkedIds(".content-check");
  if (!ids.length) return showAlert("请先勾选内容", "error");
  await request("/api/admin/content/audit/batch/approve", { method: "PUT", body: JSON.stringify({ ids }) });
  showAlert("已批量通过内容");
  await loadContent();
  await loadDashboard();
}

async function batchRejectContents() {
  const ids = checkedIds(".content-check");
  if (!ids.length) return showAlert("请先勾选内容", "error");
  const rejectReason = prompt("请输入批量拒绝原因", "内容不符合平台要求");
  if (!rejectReason) return;
  await request("/api/admin/content/audit/batch/reject", { method: "PUT", body: JSON.stringify({ ids, rejectReason }) });
  showAlert("已批量拒绝内容");
  await loadContent();
}

async function approveContent(id) {
  await request(`/api/admin/content/audit/${id}/approve`, { method: "PUT" });
  showAlert("内容已通过");
  await loadContent();
  await loadDashboard();
}

async function rejectContent(id) {
  const rejectReason = prompt("请输入拒绝原因", "内容不符合平台要求");
  if (!rejectReason) return;
  await request(`/api/admin/content/audit/${id}/reject`, { method: "PUT", body: JSON.stringify({ rejectReason }) });
  showAlert("内容已拒绝");
  await loadContent();
}

async function recheckContent(id) {
  await request(`/api/admin/content/audit/${id}/recheck`, { method: "PUT" });
  showAlert("内容已标记复审");
  await loadContent();
}

async function deleteContent(id) {
  if (!confirm("确定删除该用户内容吗？")) return;
  await request(`/api/admin/content/audit/${id}`, { method: "DELETE" });
  showAlert("内容已删除");
  await loadContent();
}

async function loadSensitiveWords() {
  const data = await request(`/api/admin/sensitive-words/page?${qs({
    pageNum: 1,
    pageSize: 50,
    word: $("#wordQuery")?.value,
    status: $("#wordStatusQuery")?.value,
  })}`);
  $("#wordTable").innerHTML = records(data).map((word) => `
    <tr>
      <td>${word.id}</td>
      <td><strong>${escapeHtml(word.word)}</strong></td>
      <td>${badge(word.status, {0: "停用", 1: "启用"})}</td>
      <td>${fmt(word.createTime)}</td>
      <td class="actions">
        <button onclick="openWordEdit(${word.id}, '${escapeHtml(word.word)}', ${word.status})">编辑</button>
        <button class="secondary" onclick="deleteWord(${word.id})">删除</button>
      </td>
    </tr>
  `).join("") || `<tr><td colspan="5">暂无敏感词</td></tr>`;
}

function wordForm(word = "", status = 1) {
  return `
    <div class="form-grid">
      <label>敏感词<input name="word" value="${escapeHtml(word)}" required></label>
      <label>状态
        <select name="status">
          <option value="1" ${Number(status) === 1 ? "selected" : ""}>启用</option>
          <option value="0" ${Number(status) === 0 ? "selected" : ""}>停用</option>
        </select>
      </label>
    </div>
  `;
}

function openWordCreate() {
  openForm("新增敏感词", wordForm(), async (form) => {
    await request("/api/admin/sensitive-words", {
      method: "POST",
      body: JSON.stringify({ word: formValue(form, "word"), status: Number(formValue(form, "status", 1)) }),
    });
    showAlert("敏感词已新增");
    await loadSensitiveWords();
  });
}

function openWordEdit(id, word, status) {
  openForm("编辑敏感词", wordForm(word, status), async (form) => {
    await request(`/api/admin/sensitive-words/${id}`, {
      method: "PUT",
      body: JSON.stringify({ word: formValue(form, "word"), status: Number(formValue(form, "status", 1)) }),
    });
    showAlert("敏感词已更新");
    await loadSensitiveWords();
  });
}

async function deleteWord(id) {
  if (!confirm("确定删除该敏感词吗？")) return;
  await request(`/api/admin/sensitive-words/${id}`, { method: "DELETE" });
  showAlert("敏感词已删除");
  await loadSensitiveWords();
}

async function loadLogs() {
  const [loginLogs, backups, operationLogs] = await Promise.all([
    request(`/api/admin/logs/login/page?${qs({
      pageNum: 1,
      pageSize: 50,
      username: $("#loginLogUsername")?.value,
      loginStatus: $("#loginLogStatus")?.value,
      startTime: dateTimeParam("#loginLogStart"),
      endTime: dateTimeParam("#loginLogEnd"),
    })}`),
    request(`/api/admin/backups/page?${qs({
      pageNum: 1,
      pageSize: 50,
      backupType: $("#backupTypeQuery")?.value,
    })}`),
    request(`/api/admin/logs/operations/page?${qs({
      pageNum: 1,
      pageSize: 50,
      operator: $("#operationLogOperator")?.value,
      moduleName: $("#operationLogModule")?.value,
      operationType: $("#operationLogType")?.value,
      keyword: $("#operationLogKeyword")?.value,
      startTime: dateTimeParam("#operationLogStart"),
      endTime: dateTimeParam("#operationLogEnd"),
    })}`),
  ]);
  $("#loginLogTable").innerHTML = records(loginLogs).map((log) => `
    <tr>
      <td>${log.id}</td>
      <td>${escapeHtml(log.username)}</td>
      <td>${badge(log.loginStatus, {0: "失败", 1: "成功"})}</td>
      <td>${escapeHtml(log.ipAddress || "-")}</td>
      <td>${fmt(log.loginTime)}</td>
    </tr>
  `).join("") || `<tr><td colspan="5">暂无登录日志</td></tr>`;
  $("#backupTable").innerHTML = records(backups).map((item) => `
    <tr>
      <td>${item.id}</td>
      <td>${escapeHtml(item.backupName)}</td>
      <td>${escapeHtml(item.backupType)}</td>
      <td>${escapeHtml(item.filePath || "-")}</td>
      <td>${badge(item.status, {0: "失败", 1: "成功"})}</td>
      <td>${fmt(item.createTime)}</td>
      <td class="actions">
        <button onclick="restoreBackup(${item.id})">恢复</button>
        <button class="secondary" onclick="downloadBackup(${item.id})">下载</button>
      </td>
    </tr>
  `).join("") || `<tr><td colspan="7">暂无备份记录</td></tr>`;
  $("#operationLogTable").innerHTML = records(operationLogs).map((log) => `
    <tr>
      <td>${log.id}</td>
      <td>${escapeHtml(log.adminUsername || "-")}</td>
      <td>${escapeHtml(log.moduleName || "-")}</td>
      <td>${escapeHtml(log.operationType || "-")}</td>
      <td>${escapeHtml(log.targetType || "-")} #${escapeHtml(log.targetId || "-")}</td>
      <td>${fmt(log.operationTime)}</td>
    </tr>
  `).join("") || `<tr><td colspan="6">暂无操作日志</td></tr>`;
}

function clearLoginLogQuery() {
  $("#loginLogUsername").value = "";
  $("#loginLogStatus").value = "";
  $("#loginLogStart").value = "";
  $("#loginLogEnd").value = "";
  loadLogs().catch((error) => showAlert(error.message, "error"));
}

function clearOperationLogQuery() {
  $("#operationLogOperator").value = "";
  $("#operationLogModule").value = "";
  $("#operationLogType").value = "";
  $("#operationLogKeyword").value = "";
  $("#operationLogStart").value = "";
  $("#operationLogEnd").value = "";
  loadLogs().catch((error) => showAlert(error.message, "error"));
}

function clearBackupQuery() {
  $("#backupTypeQuery").value = "";
  loadLogs().catch((error) => showAlert(error.message, "error"));
}

function backupForm() {
  return `
    <div class="form-grid">
      <label>备份名称<input name="backupName" value="手动备份-${new Date().toLocaleDateString("zh-CN")}" required></label>
      <label>备份类型
        <select name="backupType">
          <option value="FULL">全量备份</option>
          <option value="PARTIAL">部分备份</option>
        </select>
      </label>
    </div>
  `;
}

function openBackupCreate() {
  openForm("手动备份", backupForm(), async (form) => {
    await request("/api/admin/backups", {
      method: "POST",
      body: JSON.stringify({ backupName: formValue(form, "backupName"), backupType: formValue(form, "backupType", "FULL") }),
    });
    showAlert("备份记录已创建");
    await loadLogs();
  });
}

async function restoreBackup(id) {
  if (!confirm("恢复操作会记录审计日志。当前为验收演示恢复，确认继续？")) return;
  await request(`/api/admin/backups/${id}/restore`, { method: "POST" });
  showAlert("备份恢复操作已记录");
}

async function downloadBackup(id) {
  await download(`/api/admin/backups/${id}/download`, `backup-${id}.sql`);
  showAlert("备份文件已下载");
}

async function exportArtifacts() {
  await download(`/api/admin/artifacts/export-csv?${qs({
    title: $("#artifactTitle")?.value,
    type: $("#artifactType")?.value,
    museum: $("#artifactMuseum")?.value,
  })}`, "artifacts.csv");
  showAlert("文物CSV已导出");
}

function openArtifactImport() {
  openForm("JSON批量导入文物", `
    <label>JSON数组<textarea name="json" class="large-textarea" required>[
  {
    "objectId": "import_${Date.now()}",
    "title": "导入演示文物",
    "period": "明代",
    "type": "陶瓷",
    "material": "瓷",
    "description": "用于验收展示的批量导入数据。",
    "dimensions": "高10cm",
    "museum": "海外博物馆",
    "location": "海外",
    "detailUrl": "https://example.com/detail",
    "imageUrl": "https://example.com/image.jpg",
    "imagePath": "images/import-demo.jpg",
    "creditLine": "Open Access",
    "accessionNumber": "IMPORT-001",
    "crawlDate": "2026-05-29",
    "auditStatus": 1,
    "kgSyncStatus": 0
  }
]</textarea></label>
  `, async (form) => {
    const payload = JSON.parse(formValue(form, "json"));
    const count = await request("/api/admin/artifacts/import-json", { method: "POST", body: JSON.stringify(payload) });
    showAlert(`已导入 ${count} 条文物`);
    await loadArtifacts();
    await loadDashboard();
  });
}

function switchView(view) {
  state.view = view;
  $("#pageTitle").textContent = pageNames[view];
  $$(".nav-item").forEach((item) => item.classList.toggle("active", item.dataset.view === view));
  $$(".view").forEach((item) => item.classList.toggle("active", item.id === view));
  loadCurrent().catch((error) => showAlert(error.message, "error"));
}

function bind() {
  $("#loginForm").addEventListener("submit", login);
  $("#logoutBtn").addEventListener("click", logout);
  $("#refreshBtn").addEventListener("click", () => loadCurrent());
  $("#modalClose").addEventListener("click", () => $("#modal").close());
  $("#modalCancel").addEventListener("click", () => $("#modal").close());
  $("#detailClose").addEventListener("click", () => $("#detailModal").close());
  $$(".nav-item").forEach((item) => item.addEventListener("click", () => switchView(item.dataset.view)));

  $("#queryArtifacts").addEventListener("click", loadArtifacts);
  $("#clearArtifactQuery").addEventListener("click", () => {
    $("#artifactTitle").value = "";
    $("#artifactType").value = "";
    $("#artifactMuseum").value = "";
    loadArtifacts();
  });
  $("#openArtifactCreate").addEventListener("click", openArtifactCreate);
  $("#openArtifactImport").addEventListener("click", openArtifactImport);
  $("#exportArtifacts").addEventListener("click", () => exportArtifacts().catch((error) => showAlert(error.message, "error")));

  $("#queryPlatformUsers").addEventListener("click", loadPlatformUsers);
  $("#openPlatformUserCreate").addEventListener("click", openPlatformUserCreate);
  $("#checkAllPlatformUsers").addEventListener("change", (event) => $$(".platform-user-check").forEach((item) => item.checked = event.target.checked));
  $("#batchDisableUsers").addEventListener("click", () => batchPlatformUsers("status").catch((error) => showAlert(error.message, "error")));
  $("#batchBanComment").addEventListener("click", () => batchPlatformUsers("banComment").catch((error) => showAlert(error.message, "error")));
  $("#batchBanUpload").addEventListener("click", () => batchPlatformUsers("banUpload").catch((error) => showAlert(error.message, "error")));
  $("#queryAdminUsers").addEventListener("click", loadAdminUsers);
  $("#openAdminCreate").addEventListener("click", openAdminCreate);
  $("#openRoleCreate").addEventListener("click", openRoleCreate);
  $("#saveRolePermissions").addEventListener("click", () => saveRolePermissions().catch((error) => showAlert(error.message, "error")));
  $("#queryContents").addEventListener("click", loadContent);
  $("#checkAllContents").addEventListener("change", (event) => $$(".content-check").forEach((item) => item.checked = event.target.checked));
  $("#batchApproveContents").addEventListener("click", () => batchApproveContents().catch((error) => showAlert(error.message, "error")));
  $("#batchRejectContents").addEventListener("click", () => batchRejectContents().catch((error) => showAlert(error.message, "error")));
  $("#queryWords").addEventListener("click", loadSensitiveWords);
  $("#openWordCreate").addEventListener("click", openWordCreate);
  $("#queryLoginLogs").addEventListener("click", () => loadLogs().catch((error) => showAlert(error.message, "error")));
  $("#clearLoginLogs").addEventListener("click", clearLoginLogQuery);
  $("#queryOperationLogs").addEventListener("click", () => loadLogs().catch((error) => showAlert(error.message, "error")));
  $("#clearOperationLogs").addEventListener("click", clearOperationLogQuery);
  $("#queryBackups").addEventListener("click", () => loadLogs().catch((error) => showAlert(error.message, "error")));
  $("#clearBackups").addEventListener("click", clearBackupQuery);
  $("#openBackupCreate").addEventListener("click", openBackupCreate);
}

bind();

Object.assign(window, {
  viewArtifact,
  openArtifactEdit,
  deleteArtifact,
  togglePlatformStatus,
  togglePlatformComment,
  togglePlatformUpload,
  openPlatformUserEdit,
  deletePlatformUser,
  viewPlatformContents,
  openAdminEdit,
  toggleAdminStatus,
  selectRole,
  openRoleEdit,
  deleteRole,
  approveContent,
  rejectContent,
  recheckContent,
  deleteContent,
  openWordEdit,
  deleteWord,
  restoreBackup,
  downloadBackup,
});
