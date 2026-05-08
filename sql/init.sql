CREATE TABLE IF NOT EXISTS `admin_user` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `username` VARCHAR(50) NOT NULL UNIQUE COMMENT '用户名',
    `password` VARCHAR(100) NOT NULL COMMENT '密码',
    `role_id` BIGINT COMMENT '角色ID',
    `status` TINYINT DEFAULT 1 COMMENT '状态：1启用，0禁用',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_username (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='后台管理员表';

CREATE TABLE IF NOT EXISTS `role` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `role_name` VARCHAR(50) NOT NULL COMMENT '角色名称',
    `description` VARCHAR(200) COMMENT '角色描述',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_role_name (`role_name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色表';

CREATE TABLE IF NOT EXISTS `permission` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `permission_name` VARCHAR(100) NOT NULL COMMENT '权限名称',
    `permission_code` VARCHAR(100) NOT NULL COMMENT '权限编码',
    `type` VARCHAR(20) COMMENT '权限类型：menu/button',
    `parent_id` BIGINT DEFAULT 0 COMMENT '父权限ID',
    `sort` INT DEFAULT 0 COMMENT '排序',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_permission_code (`permission_code`)
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

INSERT INTO `role` (`id`, `role_name`, `description`, `create_time`) VALUES
(1, '超级管理员', '系统最高权限管理员', NOW()),
(2, '普通管理员', '普通权限管理员', NOW());

INSERT INTO `permission` (`id`, `permission_name`, `permission_code`, `type`, `parent_id`, `sort`, `create_time`) VALUES
(1, '文物管理', 'artifact:manage', 'menu', 0, 1, NOW()),
(2, '文物查看', 'artifact:view', 'button', 1, 1, NOW()),
(3, '文物新增', 'artifact:add', 'button', 1, 2, NOW()),
(4, '文物编辑', 'artifact:edit', 'button', 1, 3, NOW()),
(5, '文物删除', 'artifact:delete', 'button', 1, 4, NOW()),
(6, '用户管理', 'admin:manage', 'menu', 0, 2, NOW()),
(7, '用户查看', 'admin:view', 'button', 6, 1, NOW()),
(8, '用户新增', 'admin:add', 'button', 6, 2, NOW()),
(9, '用户编辑', 'admin:edit', 'button', 6, 3, NOW()),
(10, '用户禁用', 'admin:disable', 'button', 6, 4, NOW()),
(11, '角色管理', 'role:manage', 'menu', 0, 3, NOW()),
(12, '角色查看', 'role:view', 'button', 11, 1, NOW()),
(13, '角色权限分配', 'role:assign', 'button', 11, 2, NOW()),
(14, '权限管理', 'permission:manage', 'menu', 0, 4, NOW()),
(15, '权限查看', 'permission:view', 'button', 14, 1, NOW());

INSERT INTO `role_permission` (`role_id`, `permission_id`) VALUES
(1, 1), (1, 2), (1, 3), (1, 4), (1, 5),
(1, 6), (1, 7), (1, 8), (1, 9), (1, 10),
(1, 11), (1, 12), (1, 13), (1, 14), (1, 15),
(2, 1), (2, 2), (2, 3), (2, 4), (2, 11), (2, 12);

INSERT INTO `admin_user` (`id`, `username`, `password`, `role_id`, `status`, `create_time`, `update_time`) VALUES
(1, 'admin', '123456', 1, 1, NOW(), NOW()),
(2, 'user', '123456', 2, 1, NOW(), NOW());