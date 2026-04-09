-- 将 b_user 和 b_classification 表的 create_time 字段从 VARCHAR 改为 DATETIME
-- 执行前请备份数据库！

USE java_shop;

-- 1. 备份原表
CREATE TABLE IF NOT EXISTS b_user_backup AS SELECT * FROM b_user;
CREATE TABLE IF NOT EXISTS b_classification_backup AS SELECT * FROM b_classification;

-- 2. 为 b_user 表添加临时 DATETIME 列
ALTER TABLE b_user ADD COLUMN create_time_tmp DATETIME NULL DEFAULT NULL;

-- 3. 将时间戳转换为 DATETIME 并更新到临时列
UPDATE b_user SET create_time_tmp = FROM_UNIXTIME(create_time/1000) WHERE create_time IS NOT NULL;

-- 4. 删除原 VARCHAR 列
ALTER TABLE b_user DROP COLUMN create_time;

-- 5. 将临时列改名为 create_time
ALTER TABLE b_user CHANGE COLUMN create_time_tmp create_time DATETIME NULL DEFAULT NULL;

-- 6. 为 b_classification 表添加临时 DATETIME 列
ALTER TABLE b_classification ADD COLUMN create_time_tmp DATETIME NULL DEFAULT NULL;

-- 7. 将时间戳转换为 DATETIME 并更新到临时列
UPDATE b_classification SET create_time_tmp = FROM_UNIXTIME(create_time/1000) WHERE create_time IS NOT NULL;

-- 8. 删除原 VARCHAR 列
ALTER TABLE b_classification DROP COLUMN create_time;

-- 9. 将临时列改名为 create_time 并设置为 NOT NULL
ALTER TABLE b_classification CHANGE COLUMN create_time_tmp create_time DATETIME NOT NULL;

-- 10. 验证结果
SELECT 'b_user' as table_name, create_time FROM b_user LIMIT 3;
SELECT 'b_classification' as table_name, create_time FROM b_classification LIMIT 3;
