DROP TABLE IF EXISTS `mq_client_messages`;
CREATE TABLE IF NOT EXISTS `mq_client_messages`
(
    `id`             BIGINT unsigned                          NOT NULL AUTO_INCREMENT,
    `biz_id`         VARCHAR(128) COLLATE utf8mb4_unicode_ci  NOT NULL,
    `biz_type`       VARCHAR(128) COLLATE utf8mb4_unicode_ci  NOT NULL,
    `mq_type`        VARCHAR(32) COLLATE utf8mb4_unicode_ci   NOT NULL,
    `message`        VARCHAR(2048) COLLATE utf8mb4_unicode_ci NOT NULL,
    `message_type`   VARCHAR(64) COLLATE utf8mb4_unicode_ci   NOT NULL,
    `message_key`    VARCHAR(128) COLLATE utf8mb4_unicode_ci,
    `destination`    VARCHAR(128) COLLATE utf8mb4_unicode_ci  NOT NULL,
    `tags`           VARCHAR(128) COLLATE utf8mb4_unicode_ci  NOT NULL,
    `send_opts`      VARCHAR(64) COLLATE utf8mb4_unicode_ci,
    `status`         VARCHAR(32) COLLATE utf8mb4_unicode_ci   NOT NULL,
    `next_retry_at`  TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    `retry_count`    INT       DEFAULT 0,
    `failure_reason` VARCHAR(2048) COLLATE utf8mb4_unicode_ci,
    `message_id`     VARCHAR(128) COLLATE utf8mb4_unicode_ci,
    `persist_mode`   TINYINT(4) COLLATE utf8mb4_unicode_ci,
    `created_at`     TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    `updated_at`     TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `partition_key`  BIGINT unsigned                          NOT NULL,
    PRIMARY KEY (`id`),
    KEY `status_key` (`status`)
) ENGINE = InnoDB
  AUTO_INCREMENT = 1
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci