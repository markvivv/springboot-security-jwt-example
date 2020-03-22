# 数据库表结构
```sql
SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for login_user
-- ----------------------------
DROP TABLE IF EXISTS `login_user`;
CREATE TABLE `login_user` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `user_name` varchar(50) DEFAULT NULL COMMENT '登录用户名',
  `nick_name` varchar(200) DEFAULT NULL COMMENT '用户昵称',
  `bcrypt_passwd` char(68) DEFAULT NULL COMMENT '加密密码',
  `status` char(1) DEFAULT 'Y' COMMENT '用户状态\nY：正常 \nL：锁定\nD：逻辑删除',
  `create_date` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '账号创建日期',
  `update_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `idx_user_name` (`user_name`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COMMENT='用户登录信息表';

-- ----------------------------
-- Records of login_user
-- ----------------------------
BEGIN;
-- 当前默认密码是123456
INSERT INTO `login_user` VALUES (1, 'admin', '系统管理员', '{bcrypt}$2a$10$XMj0iNiUZNf5Dec5QL4.WOVa92tSY6xcRH6SjY.LajLZvVMVxb8Vy', 'Y', '2020-03-22 00:36:54', '2020-03-22 20:20:34');
COMMIT;

SET FOREIGN_KEY_CHECKS = 1;
```

# 生成密码的方法
调用`PasswordTools`可以生成密码，填入数据库即可
