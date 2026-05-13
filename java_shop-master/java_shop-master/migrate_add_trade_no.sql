-- 添加支付宝交易号字段
ALTER TABLE b_order ADD COLUMN trade_no VARCHAR(64) DEFAULT NULL COMMENT '支付宝交易号';
