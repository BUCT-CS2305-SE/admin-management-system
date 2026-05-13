CREATE DATABASE IF NOT EXISTS admin_management_system
DEFAULT CHARACTER SET utf8mb4
COLLATE utf8mb4_general_ci;

USE admin_management_system;

DROP TABLE IF EXISTS artifact;
CREATE TABLE artifact (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '数据库自增主键',

    object_id VARCHAR(100) NOT NULL COMMENT '文物唯一标识符',
    title VARCHAR(255) NOT NULL COMMENT '文物名称',
    period VARCHAR(255) NOT NULL COMMENT '年代/时期',
    type VARCHAR(100) NOT NULL COMMENT '文物类型',
    material VARCHAR(255) DEFAULT '' COMMENT '材质',
    description TEXT NOT NULL COMMENT '文物介绍',
    dimensions VARCHAR(255) DEFAULT '' COMMENT '尺寸',
    museum VARCHAR(255) NOT NULL COMMENT '所属博物馆',
    location VARCHAR(255) NOT NULL COMMENT '博物馆所在地',
    detail_url VARCHAR(800) NOT NULL COMMENT '文物详情页URL',
    image_url VARCHAR(800) NOT NULL COMMENT '图片原始下载链接',
    image_path VARCHAR(800) NOT NULL COMMENT '本地图片存储路径',
    credit_line VARCHAR(500) DEFAULT '' COMMENT '版权/来源说明',
    accession_number VARCHAR(255) DEFAULT '' COMMENT '藏品编号',
    crawl_date DATE NOT NULL COMMENT '爬取日期',

    audit_status TINYINT NOT NULL DEFAULT 1 COMMENT '数据状态：0待审核，1已发布，2已下架',
    kg_sync_status TINYINT NOT NULL DEFAULT 0 COMMENT '图数据库同步状态：0未同步，1已同步，2同步失败',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',

    UNIQUE KEY uk_artifact_object_id (object_id),
    INDEX idx_artifact_title (title),
    INDEX idx_artifact_period (period),
    INDEX idx_artifact_type (type),
    INDEX idx_artifact_museum (museum)
) COMMENT='文物数据表';

DROP TABLE IF EXISTS role;
CREATE TABLE role (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '角色ID',
    role_name VARCHAR(100) NOT NULL COMMENT '角色名称',
    role_code VARCHAR(100) NOT NULL COMMENT '角色编码',
    description VARCHAR(255) DEFAULT '' COMMENT '角色说明',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',

    UNIQUE KEY uk_role_code (role_code)
) COMMENT='角色表';

DROP TABLE IF EXISTS permission;
CREATE TABLE permission (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '权限ID',
    permission_name VARCHAR(100) NOT NULL COMMENT '权限名称',
    permission_code VARCHAR(100) NOT NULL COMMENT '权限编码',
    module_name VARCHAR(100) NOT NULL COMMENT '所属模块',
    description VARCHAR(255) DEFAULT '' COMMENT '权限说明',

    UNIQUE KEY uk_permission_code (permission_code)
) COMMENT='权限表';

DROP TABLE IF EXISTS role_permission;
CREATE TABLE role_permission (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    role_id BIGINT NOT NULL COMMENT '角色ID',
    permission_id BIGINT NOT NULL COMMENT '权限ID',

    UNIQUE KEY uk_role_permission (role_id, permission_id)
) COMMENT='角色权限关联表';

DROP TABLE IF EXISTS admin_user;
CREATE TABLE admin_user (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '管理员ID',
    username VARCHAR(100) NOT NULL COMMENT '管理员用户名',
    password VARCHAR(255) NOT NULL COMMENT '管理员密码',
    real_name VARCHAR(100) DEFAULT '' COMMENT '真实姓名',
    role_id BIGINT NOT NULL COMMENT '角色ID',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '账号状态：0禁用，1启用',
    last_login_time DATETIME DEFAULT NULL COMMENT '最后登录时间',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',

    UNIQUE KEY uk_admin_username (username)
) COMMENT='后台管理员表';

DROP TABLE IF EXISTS platform_user;
CREATE TABLE platform_user (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '用户ID',
    username VARCHAR(100) NOT NULL COMMENT '用户名',
    email VARCHAR(100) DEFAULT '' COMMENT '邮箱',
    phone VARCHAR(30) DEFAULT '' COMMENT '手机号',
    avatar VARCHAR(800) DEFAULT '' COMMENT '头像URL',
    source VARCHAR(50) NOT NULL DEFAULT 'WEB' COMMENT '用户来源：WEB或APP',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '账号状态：0禁用，1启用',
    comment_banned TINYINT NOT NULL DEFAULT 0 COMMENT '是否禁止评论：0否，1是',
    upload_banned TINYINT NOT NULL DEFAULT 0 COMMENT '是否禁止上传：0否，1是',
    register_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '注册时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
) COMMENT='平台普通用户表';

DROP TABLE IF EXISTS user_content;
CREATE TABLE user_content (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '内容ID',
    user_id BIGINT NOT NULL COMMENT '提交用户ID',
    artifact_object_id VARCHAR(100) DEFAULT '' COMMENT '关联文物object_id',
    content_type VARCHAR(50) NOT NULL COMMENT '内容类型：COMMENT、IMAGE、AUDIO、VIDEO、POST',
    content_text TEXT COMMENT '文本内容',
    file_url VARCHAR(800) DEFAULT '' COMMENT '文件URL',
    source VARCHAR(50) NOT NULL DEFAULT 'WEB' COMMENT '来源：WEB或APP',
    audit_status TINYINT NOT NULL DEFAULT 0 COMMENT '审核状态：0待审核，1通过，2拒绝，3复审',
    reject_reason VARCHAR(500) DEFAULT '' COMMENT '拒绝原因',
    submit_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '提交时间',
    audit_time DATETIME DEFAULT NULL COMMENT '审核时间',
    auditor_id BIGINT DEFAULT NULL COMMENT '审核员ID'
) COMMENT='用户生成内容表';

DROP TABLE IF EXISTS sensitive_word;
CREATE TABLE sensitive_word (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '敏感词ID',
    word VARCHAR(100) NOT NULL COMMENT '敏感词',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '状态：0停用，1启用',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',

    UNIQUE KEY uk_sensitive_word (word)
) COMMENT='敏感词表';

DROP TABLE IF EXISTS operation_log;
CREATE TABLE operation_log (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '日志ID',
    admin_id BIGINT DEFAULT NULL COMMENT '管理员ID',
    admin_username VARCHAR(100) DEFAULT '' COMMENT '管理员用户名',
    module_name VARCHAR(100) NOT NULL COMMENT '模块名称',
    operation_type VARCHAR(100) NOT NULL COMMENT '操作类型',
    target_type VARCHAR(100) DEFAULT '' COMMENT '操作对象类型',
    target_id VARCHAR(100) DEFAULT '' COMMENT '操作对象ID',
    before_data TEXT COMMENT '操作前数据',
    after_data TEXT COMMENT '操作后数据',
    ip_address VARCHAR(100) DEFAULT '' COMMENT 'IP地址',
    operation_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '操作时间'
) COMMENT='操作日志表';

DROP TABLE IF EXISTS login_log;
CREATE TABLE login_log (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '登录日志ID',
    admin_id BIGINT DEFAULT NULL COMMENT '管理员ID',
    username VARCHAR(100) NOT NULL COMMENT '登录用户名',
    login_status TINYINT NOT NULL COMMENT '登录状态：0失败，1成功',
    ip_address VARCHAR(100) DEFAULT '' COMMENT 'IP地址',
    fail_reason VARCHAR(255) DEFAULT '' COMMENT '失败原因',
    login_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '登录时间'
) COMMENT='登录日志表';

DROP TABLE IF EXISTS backup_record;
CREATE TABLE backup_record (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '备份记录ID',
    backup_name VARCHAR(255) NOT NULL COMMENT '备份名称',
    backup_type VARCHAR(50) NOT NULL COMMENT '备份类型：FULL或PARTIAL',
    file_path VARCHAR(800) DEFAULT '' COMMENT '备份文件路径',
    file_size VARCHAR(100) DEFAULT '' COMMENT '文件大小',
    operator_id BIGINT DEFAULT NULL COMMENT '操作人ID',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '备份状态：0失败，1成功',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间'
) COMMENT='备份记录表';

INSERT INTO role(role_name, role_code, description) VALUES
('超级管理员', 'SUPER_ADMIN', '拥有全部操作权限'),
('内容审核员', 'CONTENT_REVIEWER', '仅拥有内容审核相关权限'),
('数据管理员', 'DATA_ADMIN', '拥有文物数据的增删改查权限'),
('普通用户', 'NORMAL_USER', '仅拥有基础查看权限');

INSERT INTO permission(permission_name, permission_code, module_name, description) VALUES
('查看文物', 'artifact:view', '文物管理', '查看文物数据'),
('新增文物', 'artifact:add', '文物管理', '新增文物数据'),
('编辑文物', 'artifact:edit', '文物管理', '编辑文物数据'),
('删除文物', 'artifact:delete', '文物管理', '删除文物数据'),
('导入文物', 'artifact:import', '文物管理', '批量导入文物数据'),
('导出文物', 'artifact:export', '文物管理', '导出文物数据'),
('查看用户', 'user:view', '用户管理', '查看用户数据'),
('管理用户状态', 'user:status', '用户管理', '启用或禁用用户'),
('查看审核内容', 'content:view', '内容审核', '查看待审核内容'),
('审核内容', 'content:review', '内容审核', '通过或拒绝内容'),
('查看日志', 'log:view', '日志管理', '查看系统日志'),
('查看看板', 'dashboard:view', '系统看板', '查看统计看板');

INSERT INTO role_permission(role_id, permission_id)
SELECT 1, id FROM permission;

INSERT INTO role_permission(role_id, permission_id)
SELECT 2, id FROM permission
WHERE permission_code IN ('content:view', 'content:review', 'dashboard:view');

INSERT INTO role_permission(role_id, permission_id)
SELECT 3, id FROM permission
WHERE permission_code IN (
    'artifact:view',
    'artifact:add',
    'artifact:edit',
    'artifact:delete',
    'artifact:import',
    'artifact:export',
    'dashboard:view'
);

INSERT INTO role_permission(role_id, permission_id)
SELECT 4, id FROM permission
WHERE permission_code IN ('artifact:view', 'dashboard:view');

INSERT INTO admin_user(username, password, real_name, role_id, status) VALUES
('admin', '123456', '系统管理员', 1, 1),
('reviewer', '123456', '内容审核员', 2, 1),
('dataadmin', '123456', '数据管理员', 3, 1);

INSERT INTO artifact(
    object_id,
    title,
    period,
    type,
    material,
    description,
    dimensions,
    museum,
    location,
    detail_url,
    image_url,
    image_path,
    credit_line,
    accession_number,
    crawl_date,
    audit_status,
    kg_sync_status
) VALUES
(
    'demo_001',
    'Bowl with Dragon Pattern',
    'Ming Dynasty',
    'Ceramics',
    'Porcelain',
    'A porcelain bowl decorated with dragon pattern, representing traditional Chinese ceramic craftsmanship.',
    'H. 10 cm × W. 20 cm',
    'The Metropolitan Museum of Art',
    'New York, USA',
    'https://www.metmuseum.org/art/collection/search/demo001',
    'https://images.metmuseum.org/demo001.jpg',
    'images/met/demo_001.jpg',
    'Open Access',
    '1980.001',
    '2026-05-07',
    1,
    0
),
(
    'demo_002',
    'Jade Ornament',
    'Qing Dynasty',
    'Jade',
    'Jade',
    'A jade ornament from the Qing Dynasty, showing refined carving techniques.',
    'L. 8 cm',
    'Cleveland Museum of Art',
    'Cleveland, USA',
    'https://www.clevelandart.org/art/demo002',
    'https://images.clevelandart.org/demo002.jpg',
    'images/cleveland/demo_002.jpg',
    'Cleveland Museum of Art',
    '1950.002',
    '2026-05-07',
    1,
    0
);

INSERT INTO platform_user(username, email, phone, avatar, source, status, comment_banned, upload_banned) VALUES
('web_user_01', 'webuser01@example.com', '13800000001', '', 'WEB', 1, 0, 0),
('app_user_01', 'appuser01@example.com', '13800000002', '', 'APP', 1, 0, 0);

INSERT INTO user_content(
    user_id,
    artifact_object_id,
    content_type,
    content_text,
    file_url,
    source,
    audit_status
) VALUES
(1, 'demo_001', 'COMMENT', '这件文物非常精美，体现了中国陶瓷艺术的特色。', '', 'WEB', 0),
(2, 'demo_002', 'IMAGE', '我在博物馆参观时拍摄的相关照片。', 'uploads/app/photo_001.jpg', 'APP', 0);

INSERT INTO sensitive_word(word, status) VALUES
('违规词示例', 1),
('敏感词示例', 1);

SELECT '数据库初始化完成' AS result;
