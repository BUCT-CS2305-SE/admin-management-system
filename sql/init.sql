CREATE TABLE IF NOT EXISTS `admin_user` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `username` VARCHAR(100) NOT NULL UNIQUE COMMENT '用户名',
    `password` VARCHAR(255) NOT NULL COMMENT '密码',
    `real_name` VARCHAR(100) COMMENT '真实姓名',
    `role_id` BIGINT NOT NULL COMMENT '角色ID',
    `status` TINYINT DEFAULT 1 COMMENT '状态：1启用，0禁用',
    `last_login_time` DATETIME COMMENT '最后登录时间',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_username (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='后台管理员表';

CREATE TABLE IF NOT EXISTS `role` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `role_name` VARCHAR(100) NOT NULL COMMENT '角色名称',
    `role_code` VARCHAR(100) NOT NULL UNIQUE COMMENT '角色编码',
    `description` VARCHAR(255) COMMENT '角色描述',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色表';

CREATE TABLE IF NOT EXISTS `permission` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `permission_name` VARCHAR(100) NOT NULL COMMENT '权限名称',
    `permission_code` VARCHAR(100) NOT NULL UNIQUE COMMENT '权限编码',
    `module_name` VARCHAR(100) NOT NULL COMMENT '模块名称',
    `description` VARCHAR(255) COMMENT '权限描述'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='权限表';

CREATE TABLE IF NOT EXISTS `role_permission` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `role_id` BIGINT NOT NULL COMMENT '角色ID',
    `permission_id` BIGINT NOT NULL COMMENT '权限ID',
    UNIQUE KEY uk_role_permission (`role_id`, `permission_id`),
    INDEX idx_role_id (`role_id`),
    INDEX idx_permission_id (`permission_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色权限关联表';

CREATE TABLE IF NOT EXISTS `login_log` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `admin_id` BIGINT NOT NULL COMMENT '管理员ID',
    `username` VARCHAR(50) NOT NULL COMMENT '用户名',
    `login_ip` VARCHAR(50) COMMENT '登录IP',
    `login_time` DATETIME COMMENT '登录时间',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_admin_id (`admin_id`),
    INDEX idx_login_time (`login_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='登录日志表';

INSERT INTO `role` (`id`, `role_name`, `role_code`, `description`, `create_time`) VALUES
(1, '超级管理员', 'SUPER_ADMIN', '系统最高权限管理员，拥有所有操作权限', NOW()),
(2, '内容审核员', 'CONTENT_REVIEWER', '负责文物内容审核工作', NOW()),
(3, '数据管理员', 'DATA_ADMIN', '负责文物数据的增删改查', NOW()),
(4, '普通用户', 'NORMAL_USER', '仅拥有查看权限', NOW());

INSERT INTO `permission` (`id`, `permission_name`, `permission_code`, `module_name`, `description`) VALUES
(1, '文物管理', 'artifact:manage', '文物管理', '文物管理模块权限'),
(2, '文物查看', 'artifact:view', '文物管理', '查看文物列表'),
(3, '文物新增', 'artifact:add', '文物管理', '新增文物'),
(4, '文物编辑', 'artifact:edit', '文物管理', '编辑文物'),
(5, '文物删除', 'artifact:delete', '文物管理', '删除文物'),
(6, '文物审核', 'artifact:audit', '文物管理', '审核文物'),
(7, '用户管理', 'admin:manage', '用户管理', '用户管理模块权限'),
(8, '用户查看', 'admin:view', '用户管理', '查看用户列表'),
(9, '用户新增', 'admin:add', '用户管理', '新增用户'),
(10, '用户编辑', 'admin:edit', '用户管理', '编辑用户'),
(11, '用户禁用', 'admin:disable', '用户管理', '禁用用户'),
(12, '角色管理', 'role:manage', '角色管理', '角色管理模块权限'),
(13, '角色查看', 'role:view', '角色管理', '查看角色列表'),
(14, '角色权限分配', 'role:assign', '角色管理', '分配角色权限'),
(15, '权限管理', 'permission:manage', '权限管理', '权限管理模块权限'),
(16, '权限查看', 'permission:view', '权限管理', '查看权限列表'),
(17, '登录日志', 'log:manage', '日志管理', '日志管理模块权限'),
(18, '日志查看', 'log:view', '日志管理', '查看登录日志');

INSERT INTO `role_permission` (`role_id`, `permission_id`) VALUES
(1, 1), (1, 2), (1, 3), (1, 4), (1, 5), (1, 6),
(1, 7), (1, 8), (1, 9), (1, 10), (1, 11),
(1, 12), (1, 13), (1, 14), (1, 15), (1, 16),
(1, 17), (1, 18),
(2, 1), (2, 2), (2, 4), (2, 6),
(3, 1), (3, 2), (3, 3), (3, 4), (3, 5),
(4, 1), (4, 2);

INSERT INTO `admin_user` (`id`, `username`, `password`, `real_name`, `role_id`, `status`, `create_time`, `update_time`) VALUES
(1, 'admin', '123456', '系统管理员', 1, 1, NOW(), NOW()),
(2, 'reviewer', '123456', '审核员张三', 2, 1, NOW(), NOW()),
(3, 'dataadmin', '123456', '数据管理员李四', 3, 1, NOW(), NOW()),
(4, 'user1', '123456', '普通用户王五', 4, 1, NOW(), NOW());
