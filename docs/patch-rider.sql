USE yueshihui;

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

ALTER TABLE orders
  ADD COLUMN IF NOT EXISTS rider_id BIGINT NULL COMMENT '骑手ID',
  ADD COLUMN IF NOT EXISTS rider_accept_time DATETIME NULL COMMENT '骑手接单时间',
  ADD COLUMN IF NOT EXISTS rider_arrive_shop_time DATETIME NULL COMMENT '骑手到店时间',
  ADD COLUMN IF NOT EXISTS rider_pickup_time DATETIME NULL COMMENT '骑手取餐时间',
  ADD COLUMN IF NOT EXISTS rider_delivered_time DATETIME NULL COMMENT '骑手送达时间';

ALTER TABLE orders
  ADD CONSTRAINT fk_order_rider FOREIGN KEY (rider_id) REFERENCES rider(id);

CREATE INDEX idx_orders_dispatch_pool ON orders (status, rider_id);
CREATE INDEX idx_orders_rider_status ON orders (rider_id, status, update_time);

INSERT INTO rider (username, password, real_name, phone, enabled, work_status)
VALUES ('rider01', '$2a$10$FsoODkPuGj26CrGK9uXpU.TrmVRB80WDUm0OaeXAtEwx/v9.V4foa', '默认骑手', '13900000009', 1, 'ONLINE')
ON DUPLICATE KEY UPDATE
  real_name = VALUES(real_name),
  phone = VALUES(phone),
  enabled = VALUES(enabled),
  work_status = VALUES(work_status);
