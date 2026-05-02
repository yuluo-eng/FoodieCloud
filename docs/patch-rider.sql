USE yueshihui;

-- 1) 骑手表：不存在则创建
CREATE TABLE IF NOT EXISTS rider (
  id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '骑手ID',
  username VARCHAR(50) NOT NULL UNIQUE COMMENT '登录账号',
  password VARCHAR(100) NOT NULL COMMENT '密码(加密后)',
  real_name VARCHAR(50) NOT NULL COMMENT '姓名',
  phone VARCHAR(20) UNIQUE COMMENT '手机号',
  enabled TINYINT NOT NULL DEFAULT 1 COMMENT '启用状态 1启用 0禁用',
  work_status VARCHAR(20) NOT NULL DEFAULT 'ONLINE' COMMENT '工作状态 ONLINE/OFFLINE',
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) COMMENT='骑手表';

-- 2) 骑手表：历史库补齐字段（可重复执行）
SET @db = DATABASE();

SET @col_exists = (
  SELECT COUNT(*) FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = @db AND TABLE_NAME = 'rider' AND COLUMN_NAME = 'work_status'
);
SET @sql = IF(@col_exists = 0,
  "ALTER TABLE rider ADD COLUMN work_status VARCHAR(20) NOT NULL DEFAULT 'ONLINE' COMMENT '工作状态 ONLINE/OFFLINE'",
  "SELECT 1"
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @col_exists = (
  SELECT COUNT(*) FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = @db AND TABLE_NAME = 'rider' AND COLUMN_NAME = 'enabled'
);
SET @sql = IF(@col_exists = 0,
  "ALTER TABLE rider ADD COLUMN enabled TINYINT NOT NULL DEFAULT 1 COMMENT '启用状态 1启用 0禁用'",
  "SELECT 1"
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- 3) orders 表：补齐骑手履约字段（可重复执行）
SET @col_exists = (
  SELECT COUNT(*) FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = @db AND TABLE_NAME = 'orders' AND COLUMN_NAME = 'rider_id'
);
SET @sql = IF(@col_exists = 0,
  "ALTER TABLE orders ADD COLUMN rider_id BIGINT NULL COMMENT '骑手ID'",
  "SELECT 1"
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @col_exists = (
  SELECT COUNT(*) FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = @db AND TABLE_NAME = 'orders' AND COLUMN_NAME = 'rider_accept_time'
);
SET @sql = IF(@col_exists = 0,
  "ALTER TABLE orders ADD COLUMN rider_accept_time DATETIME NULL COMMENT '骑手接单时间'",
  "SELECT 1"
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @col_exists = (
  SELECT COUNT(*) FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = @db AND TABLE_NAME = 'orders' AND COLUMN_NAME = 'rider_arrive_shop_time'
);
SET @sql = IF(@col_exists = 0,
  "ALTER TABLE orders ADD COLUMN rider_arrive_shop_time DATETIME NULL COMMENT '骑手到店时间'",
  "SELECT 1"
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @col_exists = (
  SELECT COUNT(*) FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = @db AND TABLE_NAME = 'orders' AND COLUMN_NAME = 'rider_pickup_time'
);
SET @sql = IF(@col_exists = 0,
  "ALTER TABLE orders ADD COLUMN rider_pickup_time DATETIME NULL COMMENT '骑手取餐时间'",
  "SELECT 1"
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @col_exists = (
  SELECT COUNT(*) FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = @db AND TABLE_NAME = 'orders' AND COLUMN_NAME = 'rider_delivered_time'
);
SET @sql = IF(@col_exists = 0,
  "ALTER TABLE orders ADD COLUMN rider_delivered_time DATETIME NULL COMMENT '骑手送达时间'",
  "SELECT 1"
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- 4) 外键与索引：仅在不存在时创建
SET @fk_exists = (
  SELECT COUNT(*) FROM information_schema.TABLE_CONSTRAINTS
  WHERE TABLE_SCHEMA = @db
    AND TABLE_NAME = 'orders'
    AND CONSTRAINT_NAME = 'fk_order_rider'
    AND CONSTRAINT_TYPE = 'FOREIGN KEY'
);
SET @sql = IF(@fk_exists = 0,
  "ALTER TABLE orders ADD CONSTRAINT fk_order_rider FOREIGN KEY (rider_id) REFERENCES rider(id)",
  "SELECT 1"
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @idx_exists = (
  SELECT COUNT(*) FROM information_schema.STATISTICS
  WHERE TABLE_SCHEMA = @db AND TABLE_NAME = 'orders' AND INDEX_NAME = 'idx_orders_rider_status'
);
SET @sql = IF(@idx_exists = 0,
  "CREATE INDEX idx_orders_rider_status ON orders (rider_id, status, update_time)",
  "SELECT 1"
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- 可选索引（按需开启）
-- 说明：当前数据量较小时可不创建，避免增加写入开销。
-- 若后续“可接订单池”查询出现慢 SQL，再打开：
-- CREATE INDEX idx_orders_dispatch_pool ON orders (status, rider_id);

-- 5) 默认骑手账号（可重复执行）
INSERT INTO rider (username, password, real_name, phone, enabled, work_status)
VALUES ('rider01', '$2a$10$FsoODkPuGj26CrGK9uXpU.TrmVRB80WDUm0OaeXAtEwx/v9.V4foa', '默认骑手', '13900000009', 1, 'ONLINE')
ON DUPLICATE KEY UPDATE
  real_name = VALUES(real_name),
  phone = VALUES(phone),
  enabled = VALUES(enabled),
  work_status = VALUES(work_status);
