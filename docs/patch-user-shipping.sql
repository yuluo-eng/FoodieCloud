-- 已有库执行一次即可（与 docs/init.sql 全新建库二选一，不要重复加列）
-- 在 mysql 客户端执行：SOURCE /path/to/patch-user-shipping.sql;
-- 或复制下面整段执行。执行后用：DESCRIBE user; 确认出现 receiver_name 等字段。

USE yueshihui;

ALTER TABLE `user`
  ADD COLUMN receiver_name VARCHAR(50) NULL COMMENT '收货人' AFTER avatar,
  ADD COLUMN shipping_phone VARCHAR(20) NULL COMMENT '收货电话' AFTER receiver_name,
  ADD COLUMN shipping_address VARCHAR(500) NULL COMMENT '收货详细地址' AFTER shipping_phone,
  ADD COLUMN shipping_lat DECIMAL(10, 7) NULL COMMENT '纬度' AFTER shipping_address,
  ADD COLUMN shipping_lng DECIMAL(10, 7) NULL COMMENT '经度' AFTER shipping_lat;
