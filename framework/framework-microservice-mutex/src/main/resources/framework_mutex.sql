CREATE TABLE `framework_mutex` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `service_id` varchar(32) NOT NULL COMMENT '服务唯一id',
  `mutex` varchar(32) NOT NULL COMMENT '互斥体名称',
  `host` varchar(32) NOT NULL COMMENT '主机地址',
  `version` varchar(64) NOT NULL COMMENT '版本号',
  `due_time` timestamp NOT NULL COMMENT '过期时间',
  `heartbeat` bigint(20) NOT NULL COMMENT '心跳时长',
  `created_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '记录创建时间',
  `updated_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '自动更新时间',
  `deleted` tinyint(1) DEFAULT '0' COMMENT '逻辑删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `framework_mutex_unique` (`service_id`,`mutex`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4;