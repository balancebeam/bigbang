CREATE TABLE `framework_mq_consumer_idempotent` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `mq_type` varchar(16) NOT NULL COMMENT '消息中间件类型',
  `transaction_id` varchar(64) NOT NULL COMMENT '业务流水号',
  `message_id` varchar(64) NOT NULL COMMENT '消息中间返回消息id',
  `destination` varchar(32) NOT NULL COMMENT '消息目标地址',
  `message_header` varchar(512) NOT NULL COMMENT '消息头内容',
  `message_body` varchar(1024) NOT NULL COMMENT '消息体内容',
  `tags` varchar(32) DEFAULT '' COMMENT '消息标识',
  `attribute` varchar(512) DEFAULT '{}' COMMENT '消息扩展信息',
  `created_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '记录创建时间',
  `updated_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '自动更新时间',
  `deleted` tinyint(1) DEFAULT '0' COMMENT '逻辑删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `mq_consumer_idempotent_transaction` (`transaction_id`),
  UNIQUE KEY `mq_consumer_idempotent_message` (`message_id`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4;