CREATE TABLE `t_param` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `param_name` varchar(64) DEFAULT NULL COMMENT '参数名，条件字段',
  `param_value` varchar(64) DEFAULT NULL COMMENT '参数值，加密字段',
  `type` varchar(64) DEFAULT NULL COMMENT '类型，非加密字段',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB COMMENT '参数表（垂直表，某些行需要加密）';

CREATE TABLE `t_user_info` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `name` varchar(64) DEFAULT NULL COMMENT '姓名，加密字段',
  `bank_card_no` varchar(64) DEFAULT NULL COMMENT '身份证号，加密字段',
  `type` varchar(64) DEFAULT NULL COMMENT '类型，非加密字段',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB COMMENT '用户表';