DROP TABLE IF EXISTS `mq_client_idempotent`;
CREATE TABLE IF NOT EXISTS `mq_client_idempotent`
(
    `id`            BIGINT unsigned                          NOT NULL AUTO_INCREMENT,
    `mq_type`       VARCHAR(32) COLLATE utf8mb4_unicode_ci,
    `message_key`   VARCHAR(128) COLLATE utf8mb4_unicode_ci,
    `message_id`    VARCHAR(128) COLLATE utf8mb4_unicode_ci,
    `message`       VARCHAR(2048) COLLATE utf8mb4_unicode_ci NOT NULL,
    `message_type`  VARCHAR(64) COLLATE utf8mb4_unicode_ci   NOT NULL,
    `destination`   VARCHAR(128) COLLATE utf8mb4_unicode_ci  NOT NULL,
    `tags`          VARCHAR(128) COLLATE utf8mb4_unicode_ci  NOT NULL,
    `created_at`    TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    `updated_at`    TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `partition_key` BIGINT unsigned                          NOT NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `message_id_key` (`mq_type`, `message_key`),
    KEY `par_key` (`partition_key`)
) ENGINE = InnoDB
  AUTO_INCREMENT = 1
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci