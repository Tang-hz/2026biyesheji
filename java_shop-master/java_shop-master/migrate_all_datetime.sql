-- 将所有表的 *_time 字段从 VARCHAR 转换为 DATETIME
USE java_shop;

-- b_ad.create_time
ALTER TABLE b_ad ADD COLUMN create_time_tmp DATETIME NULL DEFAULT NULL;
UPDATE b_ad SET create_time_tmp = FROM_UNIXTIME(create_time/1000) WHERE create_time IS NOT NULL AND create_time REGEXP '^[0-9]+$';
ALTER TABLE b_ad DROP COLUMN create_time;
ALTER TABLE b_ad CHANGE COLUMN create_time_tmp create_time DATETIME NULL DEFAULT NULL;

-- b_address.create_time
ALTER TABLE b_address ADD COLUMN create_time_tmp DATETIME NULL DEFAULT NULL;
UPDATE b_address SET create_time_tmp = FROM_UNIXTIME(create_time/1000) WHERE create_time IS NOT NULL AND create_time REGEXP '^[0-9]+$';
ALTER TABLE b_address DROP COLUMN create_time;
ALTER TABLE b_address CHANGE COLUMN create_time_tmp create_time DATETIME NULL DEFAULT NULL;

-- b_banner.create_time
ALTER TABLE b_banner ADD COLUMN create_time_tmp DATETIME NULL DEFAULT NULL;
UPDATE b_banner SET create_time_tmp = FROM_UNIXTIME(create_time/1000) WHERE create_time IS NOT NULL AND create_time REGEXP '^[0-9]+$';
ALTER TABLE b_banner DROP COLUMN create_time;
ALTER TABLE b_banner CHANGE COLUMN create_time_tmp create_time DATETIME NULL DEFAULT NULL;

-- b_comment.comment_time
ALTER TABLE b_comment ADD COLUMN comment_time_tmp DATETIME NULL DEFAULT NULL;
UPDATE b_comment SET comment_time_tmp = FROM_UNIXTIME(comment_time/1000) WHERE comment_time IS NOT NULL AND comment_time REGEXP '^[0-9]+$';
ALTER TABLE b_comment DROP COLUMN comment_time;
ALTER TABLE b_comment CHANGE COLUMN comment_time_tmp comment_time DATETIME NULL DEFAULT NULL;

-- b_error_log.log_time
ALTER TABLE b_error_log ADD COLUMN log_time_tmp DATETIME NULL DEFAULT NULL;
UPDATE b_error_log SET log_time_tmp = FROM_UNIXTIME(log_time/1000) WHERE log_time IS NOT NULL AND log_time REGEXP '^[0-9]+$';
ALTER TABLE b_error_log DROP COLUMN log_time;
ALTER TABLE b_error_log CHANGE COLUMN log_time_tmp log_time DATETIME NULL DEFAULT NULL;

-- b_notice.create_time
ALTER TABLE b_notice ADD COLUMN create_time_tmp DATETIME NULL DEFAULT NULL;
UPDATE b_notice SET create_time_tmp = FROM_UNIXTIME(create_time/1000) WHERE create_time IS NOT NULL AND create_time REGEXP '^[0-9]+$';
ALTER TABLE b_notice DROP COLUMN create_time;
ALTER TABLE b_notice CHANGE COLUMN create_time_tmp create_time DATETIME NULL DEFAULT NULL;

-- b_notice_user_state.delete_time
ALTER TABLE b_notice_user_state ADD COLUMN delete_time_tmp DATETIME NULL DEFAULT NULL;
UPDATE b_notice_user_state SET delete_time_tmp = FROM_UNIXTIME(delete_time/1000) WHERE delete_time IS NOT NULL AND delete_time REGEXP '^[0-9]+$';
ALTER TABLE b_notice_user_state DROP COLUMN delete_time;
ALTER TABLE b_notice_user_state CHANGE COLUMN delete_time_tmp delete_time DATETIME NULL DEFAULT NULL;

-- b_op_log.access_time
ALTER TABLE b_op_log ADD COLUMN access_time_tmp DATETIME NULL DEFAULT NULL;
UPDATE b_op_log SET access_time_tmp = FROM_UNIXTIME(access_time/1000) WHERE access_time IS NOT NULL AND access_time REGEXP '^[0-9]+$';
ALTER TABLE b_op_log DROP COLUMN access_time;
ALTER TABLE b_op_log CHANGE COLUMN access_time_tmp access_time DATETIME NULL DEFAULT NULL;

-- b_op_log.re_time
ALTER TABLE b_op_log ADD COLUMN re_time_tmp DATETIME NULL DEFAULT NULL;
UPDATE b_op_log SET re_time_tmp = FROM_UNIXTIME(re_time/1000) WHERE re_time IS NOT NULL AND re_time REGEXP '^[0-9]+$';
ALTER TABLE b_op_log DROP COLUMN re_time;
ALTER TABLE b_op_log CHANGE COLUMN re_time_tmp re_time DATETIME NULL DEFAULT NULL;

-- b_order.order_time
ALTER TABLE b_order ADD COLUMN order_time_tmp DATETIME NULL DEFAULT NULL;
UPDATE b_order SET order_time_tmp = FROM_UNIXTIME(order_time/1000) WHERE order_time IS NOT NULL AND order_time REGEXP '^[0-9]+$';
ALTER TABLE b_order DROP COLUMN order_time;
ALTER TABLE b_order CHANGE COLUMN order_time_tmp order_time DATETIME NULL DEFAULT NULL;

-- b_order.pay_time
ALTER TABLE b_order ADD COLUMN pay_time_tmp DATETIME NULL DEFAULT NULL;
UPDATE b_order SET pay_time_tmp = FROM_UNIXTIME(pay_time/1000) WHERE pay_time IS NOT NULL AND pay_time REGEXP '^[0-9]+$';
ALTER TABLE b_order DROP COLUMN pay_time;
ALTER TABLE b_order CHANGE COLUMN pay_time_tmp pay_time DATETIME NULL DEFAULT NULL;

-- b_tag.create_time
ALTER TABLE b_tag ADD COLUMN create_time_tmp DATETIME NULL DEFAULT NULL;
UPDATE b_tag SET create_time_tmp = FROM_UNIXTIME(create_time/1000) WHERE create_time IS NOT NULL AND create_time REGEXP '^[0-9]+$';
ALTER TABLE b_tag DROP COLUMN create_time;
ALTER TABLE b_tag CHANGE COLUMN create_time_tmp create_time DATETIME NULL DEFAULT NULL;

-- b_thing.create_time
ALTER TABLE b_thing ADD COLUMN create_time_tmp DATETIME NULL DEFAULT NULL;
UPDATE b_thing SET create_time_tmp = FROM_UNIXTIME(create_time/1000) WHERE create_time IS NOT NULL AND create_time REGEXP '^[0-9]+$';
ALTER TABLE b_thing DROP COLUMN create_time;
ALTER TABLE b_thing CHANGE COLUMN create_time_tmp create_time DATETIME NULL DEFAULT NULL;

-- 验证结果
SELECT table_name, column_name, column_type
FROM information_schema.columns
WHERE table_schema = 'java_shop'
  AND column_name LIKE '%_time%'
ORDER BY table_name;
