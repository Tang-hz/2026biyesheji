-- 广告新增“标语”字段（前台红色文案展示）
ALTER TABLE `b_ad`
ADD COLUMN `slogan` varchar(200) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL AFTER `link`;

