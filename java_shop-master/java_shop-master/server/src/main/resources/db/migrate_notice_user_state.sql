-- 用户维度的“消息状态表”：用于按用户删除/已读扩展
CREATE TABLE IF NOT EXISTS `b_notice_user_state` (
  `id` BIGINT(20) NOT NULL AUTO_INCREMENT,
  `notice_id` BIGINT(20) NOT NULL,
  `user_id` BIGINT(20) NOT NULL,
  `deleted` TINYINT(1) NOT NULL DEFAULT 0,
  `delete_time` VARCHAR(30) NULL DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_notice_user` (`notice_id`, `user_id`),
  KEY `idx_user_deleted` (`user_id`, `deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

