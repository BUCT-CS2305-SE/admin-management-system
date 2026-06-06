# 后台管理子系统 — 服务器部署指南

本文档面向一台干净的 Linux 服务器（已在 **Ubuntu 22.04 / 24.04 LTS** + 阿里云 ECS 验证），从 `git clone` 开始，
完整覆盖系统依赖安装、JDK / Maven、MySQL 8.0 初始化、JAR 打包、systemd 托管、
Nginx 反向代理 + HTTPS、域名绑定、与知识图谱子系统的 JWT 互信对接。

> **本文档约定**
> - 服务器项目根目录：`/root/se_apps/admin-management-system`
> - MySQL 业务账号 `root` 密码：`se_jk2305`（也用于业务库 `admin_management_system`）
> - 系统 Java 版本：**JDK 21**（Spring Boot 3.5 同时支持 17 / 21）
> 如你机器上的路径或密码不同，请同步替换下文相关命令中的占位。

---

## 0. 总览

部署完成后服务器上将运行 3 个组件：

| 组件 | 默认端口 | 说明 |
|------|----------|------|
| MySQL 8.0 | 3306 | 业务库 `admin_management_system` |
| Spring Boot (java -jar) | 8080 | 后台管理 API + 静态前端（`/api/admin/...` 与 `index.html`） |
| Nginx | 80 / 443 | 反向代理 + HTTPS + 域名 |

目录约定：仓库根 = `/root/se_apps/admin-management-system`。如需替换路径，请同步修改下文 `cd` 命令与 systemd 配置中的 `WorkingDirectory`。

> 与 [knowledge-graph-subsystem/docs/DEPLOY.md](file:///root/se_apps/knowledge-graph-subsystem/docs/DEPLOY.md) 的 **JWT 互信约定**：
> 本子系统 `JwtUtil` 的 `SECRET` 与 `ISSUER` 必须与 KG 子系统 `.env` 的 `KG_JWT_SECRET` / `KG_JWT_ISSUER` **完全一致**，
> 否则 KG 侧无法解析 admin 签发的 token。当前默认值：
> - SECRET = `admin-management-system-hjj-secret`
> - ISSUER = `admin-management-system`

---

## 1. 系统准备

### 1.1 更新系统

```bash
apt update && apt -y upgrade
```

> 服务器以 root 直连（你当前就是 root），下文命令一律不再写 `sudo`。如换成普通用户，请按需补 `sudo`。

### 1.2 安装基础工具

```bash
apt -y install \
    git curl wget vim unzip \
    ca-certificates gnupg lsb-release ufw
```

### 1.3 Java 21（Spring Boot 3.5 兼容）

如果系统已经装了 JDK 21，直接确认：

```bash
java -version            # 应输出 21.x
which java               # 通常 /usr/bin/java
```

如未安装：

```bash
apt -y install openjdk-21-jdk-headless
```

> 项目源码 [pom.xml](file:///root/se_apps/admin-management-system/backend/pom.xml) 默认 `<java.version>17</java.version>`，编译产物为 17 字节码，可直接在 JDK 21 上运行；如希望以 21 为目标编译，把该值改为 `21` 即可。

### 1.4（可选）安装 Maven

仓库自带 `mvnw`（Maven Wrapper），可不装系统级 Maven，直接 `./mvnw` 即可。
如需手动 Maven：

```bash
apt -y install maven
mvn -v
```

---

## 2. 拉取代码

```bash
mkdir -p /root/se_apps
cd /root/se_apps
git clone <你的仓库地址> admin-management-system
cd admin-management-system/backend
```

后续所有命令默认在 `/root/se_apps/admin-management-system/backend` 下执行（除非特别说明）。

---

## 3. 安装并初始化 MySQL 8.0

### 3.1 安装

```bash
apt -y install mysql-server
systemctl enable --now mysql
mysql_secure_installation         # 按提示设置 root 密码、移除匿名/远程 root 等
```

### 3.2 重置 root 认证插件 + 设置密码

Ubuntu/Debian 下 apt 装的 MySQL 默认 root 用 `auth_socket` 插件，**JDBC 用密码连不进去**，必须改成 `mysql_native_password`。

由于密码 `se_jk2305` 不包含大写或特殊字符，会被 8.0 的 `validate_password` 策略拒绝，需要先临时降低策略再设密码：

```bash
mysql <<'SQL'
SET GLOBAL validate_password.policy = LOW;
SET GLOBAL validate_password.length = 6;
ALTER USER 'root'@'localhost' IDENTIFIED WITH mysql_native_password BY 'se_jk2305';
ALTER USER 'root'@'127.0.0.1' IDENTIFIED WITH mysql_native_password BY 'se_jk2305';
FLUSH PRIVILEGES;
SQL
```

> 上述 `mysql` 不带 `-p` 是因为 root 此刻还在 `auth_socket` 模式，本机 root 进程可直接进。
> 如这条命令报 "Unknown system variable `validate_password.policy`"，说明你装的是较老版本，请把变量名换成 `validate_password_policy` / `validate_password_length`（去掉点）。

验证 TCP 连接（应该能进）：

```bash
mysql -h 127.0.0.1 -uroot -p'se_jk2305' -e "SELECT 'ok';"
```

### 3.3 初始化业务库（建表 + 种子数据）

仓库自带 [sql/init.sql](file:///root/se_apps/admin-management-system/sql/init.sql)，包含 `admin_user / role / permission / artifact / ...` 全部表结构和默认管理员账号 `admin / 123456`：

```bash
cd /root/se_apps/admin-management-system
mysql -h 127.0.0.1 -uroot -p'se_jk2305' < sql/init.sql

mysql -h 127.0.0.1 -uroot -p'se_jk2305' \
  -e "USE admin_management_system; SELECT id,username,status,role_id FROM admin_user;"
```

期望输出 `id=1 / username=admin / status=1 / role_id=1`。

> 安全提醒：本部署直接用 root 账号连库（业务密码 `se_jk2305`），适合个人 / 实验环境。
> 生产环境强烈建议另建一个最小权限账号，例如：
> ```sql
> CREATE USER 'adminapp'@'127.0.0.1' IDENTIFIED WITH mysql_native_password BY 'AdminApp@2025';
> GRANT ALL PRIVILEGES ON admin_management_system.* TO 'adminapp'@'127.0.0.1';
> FLUSH PRIVILEGES;
> ```
> 然后把 §4 的 `username/password` 换成这个账号。

---

## 4. 配置 application.yml

编辑 [backend/src/main/resources/application.yml](file:///root/se_apps/admin-management-system/backend/src/main/resources/application.yml)：

```yaml
server:
  port: 8080

spring:
  datasource:
    driver-class-name: com.mysql.cj.jdbc.Driver
    url: jdbc:mysql://127.0.0.1:3306/admin_management_system?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai&useSSL=false&allowPublicKeyRetrieval=true
    username: root
    password: "se_jk2305"
```

### 4.1（推荐）改用环境变量，避免泄漏密码

```yaml
    username: ${DB_USER:root}
    password: ${DB_PASSWORD}
```

启动时由 systemd 注入（见 §6）。

---

## 5. 打包 JAR

```bash
cd /root/se_apps/admin-management-system/backend
./mvnw clean package -DskipTests
ls -lh target/*.jar         # 应见 backend-0.0.1-SNAPSHOT.jar
```

> mvnw 会用当前 PATH 上的 Java 调用编译器；JDK 21 完全兼容，编译产物可直接运行。

前台快速验证：

```bash
java -jar target/backend-0.0.1-SNAPSHOT.jar --server.port=8080
# 另一个终端
curl -s -X POST http://127.0.0.1:8080/api/admin/auth/login \
  -H 'Content-Type: application/json' \
  -d '{"username":"admin","password":"123456"}'
```

返回 `code:200` + `data.token` 即说明连库 + 鉴权链路通畅，Ctrl+C 终止后进入 §6 托管。

---

## 6. 生产部署 — systemd 托管

### 6.1 准备日志文件

直接以 root 运行（与你的服务器使用习惯一致），无需新建账户：

```bash
touch /var/log/admin-api.log
```

### 6.2 写 service 文件

```bash
tee /etc/systemd/system/admin-api.service > /dev/null <<'EOF'
[Unit]
Description=Admin Management System API (Spring Boot)
After=network.target mysql.service

[Service]
Type=simple
User=root
Group=root
WorkingDirectory=/root/se_apps/admin-management-system/backend
Environment=JAVA_OPTS=-Xms256m -Xmx512m -Dfile.encoding=UTF-8
Environment=DB_USER=root
Environment=DB_PASSWORD=se_jk2305
ExecStart=/usr/bin/java $JAVA_OPTS -jar /root/se_apps/admin-management-system/backend/target/backend-0.0.1-SNAPSHOT.jar --server.port=8080
Restart=on-failure
RestartSec=5
StandardOutput=append:/var/log/admin-api.log
StandardError=append:/var/log/admin-api.log

[Install]
WantedBy=multi-user.target
EOF
```

> 把密码写在 service 文件里有泄漏风险（任何能读 `/etc/systemd/system/admin-api.service` 的用户都能看到）。
> 更稳妥：单独写一个 `/etc/admin-api.env`，`chmod 600`，service 里改用 `EnvironmentFile=/etc/admin-api.env`。

### 6.3 启动

```bash
systemctl daemon-reload
systemctl enable --now admin-api
systemctl status admin-api
journalctl -u admin-api -f          # 实时日志
```

确认监听：

```bash
ss -ltnp | grep 8080
curl -s http://127.0.0.1:8080/api/admin/auth/login \
  -H 'Content-Type: application/json' \
  -d '{"username":"admin","password":"123456"}' | head
```

---

## 7. Nginx 反向代理 + 域名 + HTTPS

### 7.1 安装 Nginx

```bash
apt -y install nginx
systemctl enable --now nginx
```

### 7.2 站点配置

把下文 `admin.example.com` 替换为你买的域名（确保已在阿里云 DNS 加 A 记录指向 ECS 公网 IP）：

```bash
tee /etc/nginx/sites-available/admin-api > /dev/null <<'EOF'
server {
    listen 80;
    server_name admin.example.com;

    client_max_body_size 32m;

    location / {
        proxy_pass         http://127.0.0.1:8080;
        proxy_http_version 1.1;
        proxy_set_header   Host              $host;
        proxy_set_header   X-Real-IP         $remote_addr;
        proxy_set_header   X-Forwarded-For   $proxy_add_x_forwarded_for;
        proxy_set_header   X-Forwarded-Proto $scheme;
        proxy_read_timeout 60s;
    }
}
EOF

ln -sf /etc/nginx/sites-available/admin-api /etc/nginx/sites-enabled/
nginx -t && systemctl reload nginx
```

打开 `http://admin.example.com` 应能看到登录页（[backend/src/main/resources/static/index.html](file:///root/se_apps/admin-management-system/backend/src/main/resources/static/index.html)）。

### 7.3 启用 HTTPS（Let's Encrypt 免费证书）

```bash
apt -y install certbot python3-certbot-nginx
certbot --nginx -d admin.example.com
```

证书会自动续签（`/etc/cron.d/certbot`）。续签后 Nginx 自动 reload。

> 阿里云：第一次签发前，请确保安全组放行 80 / 443 端口，否则 certbot 的 HTTP-01 验证会失败。

---

## 8. 阿里云安全组 + 服务器防火墙

### 8.1 阿里云 ECS 安全组（控制台操作）

入方向放行：
- 22（SSH，建议限定来源 IP）
- 80 / 443（Web）
- **不要**对外放行 3306 / 8080

### 8.2 服务器侧 ufw

```bash
ufw allow OpenSSH
ufw allow 'Nginx Full'           # 80 + 443
ufw enable
ufw status
```

---

## 9. 与知识图谱子系统的 JWT 互信对接

本子系统签发 token，KG 子系统用同一密钥解析。任意一处不一致都会导致 KG 返回 401。

### 9.1 admin 侧（本子系统）

[backend/src/main/java/com/buct/backend/util/JwtUtil.java](file:///root/se_apps/admin-management-system/backend/src/main/java/com/buct/backend/util/JwtUtil.java)：

```
ISSUER = "admin-management-system"
SECRET = "admin-management-system-hjj-secret"
EXPIRE_HOURS = 8
```

> 生产建议把 SECRET / ISSUER 抽到环境变量，避免硬编码。

### 9.2 KG 侧（[knowledge-graph-subsystem/server/.env](file:///root/se_apps/knowledge-graph-subsystem/server/.env)）

```ini
KG_JWT_SECRET=admin-management-system-hjj-secret
KG_JWT_ISSUER=admin-management-system
KG_ADMIN_AUTH_ENABLED=1
```

### 9.3 客户端调用流程

```bash
# 1) 登录拿 token
TOKEN=$(curl -s -X POST https://admin.example.com/api/admin/auth/login \
  -H 'Content-Type: application/json' \
  -d '{"username":"admin","password":"123456"}' \
  | python3 -c 'import sys,json;print(json.load(sys.stdin)["data"]["token"])')

# 2) 拿 token 调 KG
curl -s "https://kg.example.com/api/artifacts?page=1&page_size=5" \
  -H "Authorization: Bearer $TOKEN"
```

---

## 10. 升级流程

```bash
cd /root/se_apps/admin-management-system
git pull
cd backend
./mvnw clean package -DskipTests
systemctl restart admin-api
journalctl -u admin-api -n 100
```

---

## 11. 卸载（如需重装）

```bash
systemctl disable --now admin-api nginx mysql
apt -y purge mysql-server
rm -rf /root/se_apps/admin-management-system \
       /etc/nginx/sites-{available,enabled}/admin-api \
       /etc/systemd/system/admin-api.service \
       /var/log/admin-*.log
systemctl daemon-reload
```

---

## 12. 运维与排错速查

| 现象 | 排查 |
|------|------|
| 服务起不来 | `journalctl -u admin-api -n 200` |
| 登录返回 `code:500, message:"系统异常：null"` | NPE：99% 是连库失败（用户/密码/库名错），看堆栈 `Caused by:` |
| `Access denied for user 'root'@'localhost' (using password: YES)` | application.yml 密码与 MySQL 不一致；或 `auth_socket` 未切到 `mysql_native_password`（见 §3.2） |
| `ERROR 1819 ... password does not satisfy current policy` | MySQL 8.0 密码策略，临时调低或改强密码（见 §3.2） |
| `Port 8080 was already in use`（WSL/本地） | `fuser -k 8080/tcp`；WSL 还要查 Windows 那边 `excludedportrange` |
| 登录用户名不存在 / 密码错误 | 跑 `sql/init.sql`；或 `SELECT * FROM admin_user WHERE username='admin'` 检查 `status=1` |
| Nginx 502 Bad Gateway | `systemctl status admin-api`；`ss -ltnp \| grep 8080` |
| HTTPS 续签失败 | 安全组 80 端口未放行；或 Nginx 配置被改 |
| KG 返回 401 Unauthorized | SECRET / ISSUER 不一致；token 过期（默认 8h）；或 `KG_ADMIN_AUTH_ENABLED=0` 时把鉴权关了 |
| 时区不对 | `application.yml` 的 `serverTimezone=Asia/Shanghai` + 服务器 `timedatectl set-timezone Asia/Shanghai` |
| 中文乱码 | 库 / 表 / 连接均需 `utf8mb4`；`init.sql` 已强制 |
| `UnsupportedClassVersionError` | JDK 版本过低（< 17），用 JDK 17 / 21 重新打包 |

---

## 附录 A：环境变量索引（systemd 注入示例）

| 变量 | 用途 | 默认值 |
|------|------|--------|
| `DB_USER` | MySQL 业务账号 | `root` |
| `DB_PASSWORD` | MySQL 业务密码 | `se_jk2305` |
| `JAVA_OPTS` | JVM 参数 | `-Xms256m -Xmx512m -Dfile.encoding=UTF-8` |
| `SERVER_PORT`（启动参数 `--server.port=`） | 监听端口 | `8080` |

## 附录 B：仓库目录映射

| 目录 / 文件 | 作用 |
|---|---|
| [backend/](file:///root/se_apps/admin-management-system/backend) | Spring Boot 后端工程 |
| [backend/src/main/java/com/buct/backend/controller/](file:///root/se_apps/admin-management-system/backend/src/main/java/com/buct/backend/controller) | REST 控制器（auth / 用户 / 角色 / 权限 / 内容审核 / 备份等） |
| [backend/src/main/java/com/buct/backend/util/JwtUtil.java](file:///root/se_apps/admin-management-system/backend/src/main/java/com/buct/backend/util/JwtUtil.java) | JWT 签发 / 校验（SECRET 与 KG 共享） |
| [backend/src/main/resources/application.yml](file:///root/se_apps/admin-management-system/backend/src/main/resources/application.yml) | 端口 / 数据源配置 |
| [backend/src/main/resources/static/](file:///root/se_apps/admin-management-system/backend/src/main/resources/static) | 后台前端（登录页 + dashboard） |
| [sql/init.sql](file:///root/se_apps/admin-management-system/sql/init.sql) | 一键建库 + 默认管理员 |
| [docs/接口文档.md](file:///root/se_apps/admin-management-system/docs/接口文档.md) | 全量接口说明 |
| [docs/数据库设计.md](file:///root/se_apps/admin-management-system/docs/数据库设计.md) | ER 设计 / 表结构 |

## 附录 C：默认管理员账号

| 用户名 | 密码 | 角色 |
|---|---|---|
| `admin` | `123456` | 超级管理员（role_id=1） |

> **首次登录后请立刻在 `/api/admin/users` 修改密码**，并删除/禁用本表中的明文凭据后再发布到公网。
