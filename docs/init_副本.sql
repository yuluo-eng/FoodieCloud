CREATE DATABASE IF NOT EXISTS yueshihui
DEFAULT CHARACTER SET utf8mb4
DEFAULT COLLATE utf8mb4_general_ci;

USE yueshihui;

SET NAMES utf8mb4;

CREATE TABLE IF NOT EXISTS user (
  id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '用户ID',
  username VARCHAR(50) NOT NULL UNIQUE COMMENT '用户名',
  password VARCHAR(100) NOT NULL COMMENT '密码(加密后)',
  phone VARCHAR(20) UNIQUE COMMENT '手机号',
  nickname VARCHAR(50) COMMENT '昵称',
  avatar VARCHAR(255) COMMENT '头像URL',
  receiver_name VARCHAR(50) COMMENT '收货人',
  shipping_phone VARCHAR(20) COMMENT '收货电话',
  shipping_address VARCHAR(500) COMMENT '收货详细地址',
  shipping_lat DECIMAL(10, 7) COMMENT '收货地纬度',
  shipping_lng DECIMAL(10, 7) COMMENT '收货地经度',
  status TINYINT NOT NULL DEFAULT 1 COMMENT '状态 1正常 0禁用',
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) COMMENT='用户表';

CREATE TABLE IF NOT EXISTS merchant_shop (
  id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '店铺ID',
  shop_name VARCHAR(100) NOT NULL COMMENT '店铺名称',
  address VARCHAR(255) COMMENT '地址',
  phone VARCHAR(20) COMMENT '联系电话',
  business_status TINYINT NOT NULL DEFAULT 1 COMMENT '营业状态 1营业 0打烊',
  notice VARCHAR(255) COMMENT '公告',
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) COMMENT='店铺表';

CREATE TABLE IF NOT EXISTS role (
  id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '角色ID',
  role_name VARCHAR(50) NOT NULL UNIQUE COMMENT '角色名',
  role_code VARCHAR(50) NOT NULL UNIQUE COMMENT '角色编码',
  status TINYINT NOT NULL DEFAULT 1 COMMENT '状态 1启用 0禁用',
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) COMMENT='角色表';

CREATE TABLE IF NOT EXISTS permission (
  id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '权限ID',
  perm_name VARCHAR(100) NOT NULL COMMENT '权限名',
  perm_code VARCHAR(100) NOT NULL UNIQUE COMMENT '权限编码',
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) COMMENT='权限表';

CREATE TABLE IF NOT EXISTS role_permission (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  role_id BIGINT NOT NULL,
  permission_id BIGINT NOT NULL,
  UNIQUE KEY uk_role_perm (role_id, permission_id),
  CONSTRAINT fk_rp_role FOREIGN KEY (role_id) REFERENCES role(id),
  CONSTRAINT fk_rp_perm FOREIGN KEY (permission_id) REFERENCES permission(id)
) COMMENT='角色权限关联表';

CREATE TABLE IF NOT EXISTS employee (
  id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '员工ID',
  username VARCHAR(50) NOT NULL UNIQUE COMMENT '登录账号',
  password VARCHAR(100) NOT NULL COMMENT '密码(加密后)',
  real_name VARCHAR(50) NOT NULL COMMENT '姓名',
  phone VARCHAR(20) UNIQUE COMMENT '手机号',
  shop_id BIGINT NOT NULL COMMENT '所属店铺ID',
  role_id BIGINT NOT NULL COMMENT '角色ID',
  enabled TINYINT NOT NULL DEFAULT 1 COMMENT '启用状态 1启用 0禁用',
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  CONSTRAINT fk_emp_shop FOREIGN KEY (shop_id) REFERENCES merchant_shop(id),
  CONSTRAINT fk_emp_role FOREIGN KEY (role_id) REFERENCES role(id)
) COMMENT='员工表';

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

CREATE TABLE IF NOT EXISTS dish_category (
  id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '分类ID',
  shop_id BIGINT NOT NULL COMMENT '店铺ID',
  category_name VARCHAR(50) NOT NULL COMMENT '分类名',
  sort INT NOT NULL DEFAULT 0 COMMENT '排序',
  status TINYINT NOT NULL DEFAULT 1 COMMENT '状态 1启用 0禁用',
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  CONSTRAINT fk_dc_shop FOREIGN KEY (shop_id) REFERENCES merchant_shop(id)
) COMMENT='菜品分类表';

CREATE TABLE IF NOT EXISTS dish (
  id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '菜品ID',
  shop_id BIGINT NOT NULL COMMENT '店铺ID',
  category_id BIGINT NOT NULL COMMENT '分类ID',
  dish_name VARCHAR(100) NOT NULL COMMENT '菜品名称',
  price DECIMAL(10,2) NOT NULL COMMENT '价格',
  image_url VARCHAR(255) COMMENT '图片URL',
  description VARCHAR(500) COMMENT '描述',
  stock INT NOT NULL DEFAULT 9999 COMMENT '库存',
  status TINYINT NOT NULL DEFAULT 1 COMMENT '状态 1上架 0下架',
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  CONSTRAINT fk_dish_shop FOREIGN KEY (shop_id) REFERENCES merchant_shop(id),
  CONSTRAINT fk_dish_category FOREIGN KEY (category_id) REFERENCES dish_category(id),
  INDEX idx_dish_shop_category (shop_id, category_id)
) COMMENT='菜品表';

CREATE TABLE IF NOT EXISTS cart_item (
  id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '购物车项ID',
  user_id BIGINT NOT NULL COMMENT '用户ID',
  dish_id BIGINT NOT NULL COMMENT '菜品ID',
  quantity INT NOT NULL DEFAULT 1 COMMENT '数量',
  unit_price DECIMAL(10,2) NOT NULL COMMENT '加入购物车时单价',
  selected TINYINT NOT NULL DEFAULT 1 COMMENT '是否选中 1是 0否',
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  UNIQUE KEY uk_user_dish (user_id, dish_id),
  CONSTRAINT fk_cart_user FOREIGN KEY (user_id) REFERENCES user(id),
  CONSTRAINT fk_cart_dish FOREIGN KEY (dish_id) REFERENCES dish(id)
) COMMENT='购物车项表';

CREATE TABLE IF NOT EXISTS orders (
  id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '订单ID',
  order_no VARCHAR(64) NOT NULL UNIQUE COMMENT '订单号',
  user_id BIGINT NOT NULL COMMENT '用户ID',
  shop_id BIGINT NOT NULL COMMENT '店铺ID',
  total_amount DECIMAL(10,2) NOT NULL COMMENT '订单总金额',
  remark VARCHAR(255) COMMENT '备注',
  status TINYINT NOT NULL DEFAULT 0 COMMENT '订单状态 0待支付 1已支付 2已接单 3配送中 4已完成 5已取消',
  pay_status TINYINT NOT NULL DEFAULT 0 COMMENT '支付状态 0未支付 1已支付 2已退款',
  rider_id BIGINT NULL COMMENT '骑手ID',
  rider_accept_time DATETIME NULL COMMENT '骑手接单时间',
  rider_arrive_shop_time DATETIME NULL COMMENT '骑手到店时间',
  rider_pickup_time DATETIME NULL COMMENT '骑手取餐时间',
  rider_delivered_time DATETIME NULL COMMENT '骑手送达时间',
  pay_time DATETIME NULL COMMENT '支付时间',
  finish_time DATETIME NULL COMMENT '完成时间',
  cancel_time DATETIME NULL COMMENT '取消时间',
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  CONSTRAINT fk_order_user FOREIGN KEY (user_id) REFERENCES user(id),
  CONSTRAINT fk_order_shop FOREIGN KEY (shop_id) REFERENCES merchant_shop(id),
  CONSTRAINT fk_order_rider FOREIGN KEY (rider_id) REFERENCES rider(id),
  INDEX idx_order_user_time (user_id, create_time),
  INDEX idx_order_shop_time (shop_id, create_time)
) COMMENT='订单主表';

CREATE TABLE IF NOT EXISTS order_item (
  id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '明细ID',
  order_id BIGINT NOT NULL COMMENT '订单ID',
  dish_id BIGINT NOT NULL COMMENT '菜品ID',
  dish_name VARCHAR(100) NOT NULL COMMENT '下单时菜品名',
  dish_price DECIMAL(10,2) NOT NULL COMMENT '下单时单价',
  quantity INT NOT NULL COMMENT '数量',
  amount DECIMAL(10,2) NOT NULL COMMENT '小计金额',
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  CONSTRAINT fk_oi_order FOREIGN KEY (order_id) REFERENCES orders(id),
  CONSTRAINT fk_oi_dish FOREIGN KEY (dish_id) REFERENCES dish(id),
  INDEX idx_oi_order (order_id)
) COMMENT='订单明细表';

CREATE TABLE IF NOT EXISTS payment_record (
  id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '支付记录ID',
  order_id BIGINT NOT NULL COMMENT '订单ID',
  payment_no VARCHAR(64) NOT NULL UNIQUE COMMENT '支付流水号',
  pay_channel VARCHAR(30) NOT NULL DEFAULT 'MOCK' COMMENT '支付渠道 MOCK/ALIPAY/WECHAT',
  pay_amount DECIMAL(10,2) NOT NULL COMMENT '支付金额',
  pay_status TINYINT NOT NULL DEFAULT 0 COMMENT '支付状态 0处理中 1成功 2失败',
  transaction_no VARCHAR(64) COMMENT '第三方交易号',
  paid_time DATETIME NULL COMMENT '支付成功时间',
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  CONSTRAINT fk_pay_order FOREIGN KEY (order_id) REFERENCES orders(id),
  INDEX idx_pay_order (order_id)
) COMMENT='支付记录表';

CREATE TABLE IF NOT EXISTS operation_log (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  operator_type VARCHAR(20) NOT NULL COMMENT 'USER/EMPLOYEE',
  operator_id BIGINT NOT NULL COMMENT '操作人ID',
  module VARCHAR(50) NOT NULL COMMENT '模块',
  action VARCHAR(50) NOT NULL COMMENT '动作',
  content VARCHAR(500) COMMENT '操作内容',
  ip VARCHAR(50) COMMENT 'IP',
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  INDEX idx_log_operator_time (operator_id, create_time)
) COMMENT='操作日志表';

INSERT INTO merchant_shop (id, shop_name, address, phone, business_status, notice)
VALUES (1, '悦食汇总店', 'XX大学商业街1号', '13800000000', 1, '欢迎光临悦食汇')
ON DUPLICATE KEY UPDATE
  shop_name = VALUES(shop_name),
  address = VALUES(address),
  phone = VALUES(phone),
  business_status = VALUES(business_status),
  notice = VALUES(notice);

INSERT INTO merchant_shop (id, shop_name, address, phone, business_status, notice)
VALUES (2, '江南小厨', 'XX大学南门美食街8号', '13800000001', 1, '现炒现做，口味清爽')
ON DUPLICATE KEY UPDATE
  shop_name = VALUES(shop_name),
  address = VALUES(address),
  phone = VALUES(phone),
  business_status = VALUES(business_status),
  notice = VALUES(notice);

INSERT INTO merchant_shop (id, shop_name, address, phone, business_status, notice)
VALUES (3, '韩味食堂', 'XX大学北门创业广场3楼', '13800000002', 1, '韩式风味，人气套餐')
ON DUPLICATE KEY UPDATE
  shop_name = VALUES(shop_name),
  address = VALUES(address),
  phone = VALUES(phone),
  business_status = VALUES(business_status),
  notice = VALUES(notice);

INSERT INTO role (id, role_name, role_code, status) VALUES
(1, '超级管理员', 'SUPER_ADMIN', 1),
(2, '店长', 'SHOP_MANAGER', 1),
(3, '员工', 'STAFF', 1)
ON DUPLICATE KEY UPDATE
  role_name = VALUES(role_name),
  role_code = VALUES(role_code),
  status = VALUES(status);

INSERT INTO permission (perm_name, perm_code) VALUES
('菜品管理', 'dish:manage'),
('员工管理', 'employee:manage'),
('店铺状态管理', 'shop:status:update'),
('订单处理', 'order:manage')
ON DUPLICATE KEY UPDATE
  perm_name = VALUES(perm_name);

DELETE FROM role_permission WHERE role_id IN (1, 2, 3);

INSERT INTO role_permission (role_id, permission_id)
SELECT 1, id FROM permission;

INSERT INTO role_permission (role_id, permission_id)
SELECT 2, id FROM permission;

INSERT INTO role_permission (role_id, permission_id)
SELECT 3, id FROM permission WHERE perm_code IN ('dish:manage', 'order:manage');

INSERT INTO employee (username, password, real_name, phone, shop_id, role_id, enabled)
VALUES ('admin', '$2a$10$FsoODkPuGj26CrGK9uXpU.TrmVRB80WDUm0OaeXAtEwx/v9.V4foa', '系统管理员', '13900000000', 1, 1, 1)
ON DUPLICATE KEY UPDATE
  real_name = VALUES(real_name),
  phone = VALUES(phone),
  shop_id = VALUES(shop_id),
  role_id = VALUES(role_id),
  enabled = VALUES(enabled);

INSERT INTO rider (username, password, real_name, phone, enabled, work_status)
VALUES ('rider01', '$2a$10$FsoODkPuGj26CrGK9uXpU.TrmVRB80WDUm0OaeXAtEwx/v9.V4foa', '默认骑手', '13900000009', 1, 'ONLINE')
ON DUPLICATE KEY UPDATE
  real_name = VALUES(real_name),
  phone = VALUES(phone),
  enabled = VALUES(enabled),
  work_status = VALUES(work_status);

INSERT INTO user (username, password, phone, nickname, status)
VALUES ('testuser', '$2a$10$FsoODkPuGj26CrGK9uXpU.TrmVRB80WDUm0OaeXAtEwx/v9.V4foa', '13700000000', '测试用户', 1)
ON DUPLICATE KEY UPDATE
  phone = VALUES(phone),
  nickname = VALUES(nickname),
  status = VALUES(status);

INSERT INTO dish_category (shop_id, category_name, sort, status)
SELECT 1, '主食', 1, 1
WHERE NOT EXISTS (
  SELECT 1 FROM dish_category WHERE shop_id = 1 AND category_name = '主食'
);

INSERT INTO dish_category (shop_id, category_name, sort, status)
SELECT 1, '小吃', 2, 1
WHERE NOT EXISTS (
  SELECT 1 FROM dish_category WHERE shop_id = 1 AND category_name = '小吃'
);

INSERT INTO dish_category (shop_id, category_name, sort, status)
SELECT 1, '饮品', 3, 1
WHERE NOT EXISTS (
  SELECT 1 FROM dish_category WHERE shop_id = 1 AND category_name = '饮品'
);

INSERT INTO dish_category (shop_id, category_name, sort, status)
SELECT 2, '主食', 1, 1
WHERE NOT EXISTS (
  SELECT 1 FROM dish_category WHERE shop_id = 2 AND category_name = '主食'
);

INSERT INTO dish_category (shop_id, category_name, sort, status)
SELECT 2, '小吃', 2, 1
WHERE NOT EXISTS (
  SELECT 1 FROM dish_category WHERE shop_id = 2 AND category_name = '小吃'
);

INSERT INTO dish_category (shop_id, category_name, sort, status)
SELECT 2, '饮品', 3, 1
WHERE NOT EXISTS (
  SELECT 1 FROM dish_category WHERE shop_id = 2 AND category_name = '饮品'
);

INSERT INTO dish_category (shop_id, category_name, sort, status)
SELECT 3, '主食', 1, 1
WHERE NOT EXISTS (
  SELECT 1 FROM dish_category WHERE shop_id = 3 AND category_name = '主食'
);

INSERT INTO dish_category (shop_id, category_name, sort, status)
SELECT 3, '小吃', 2, 1
WHERE NOT EXISTS (
  SELECT 1 FROM dish_category WHERE shop_id = 3 AND category_name = '小吃'
);

INSERT INTO dish_category (shop_id, category_name, sort, status)
SELECT 3, '饮品', 3, 1
WHERE NOT EXISTS (
  SELECT 1 FROM dish_category WHERE shop_id = 3 AND category_name = '饮品'
);

INSERT INTO dish (shop_id, category_id, dish_name, price, image_url, description, stock, status)
SELECT 1, dc.id, '招牌牛肉饭', 18.00, '/dishes/beef-rice-bowl.jpg', '畅销款', 100, 1
FROM dish_category dc
WHERE dc.shop_id = 1
  AND dc.category_name = '主食'
  AND NOT EXISTS (
    SELECT 1 FROM dish d WHERE d.shop_id = 1 AND d.dish_name = '招牌牛肉饭'
  );

INSERT INTO dish (shop_id, category_id, dish_name, price, image_url, description, stock, status)
SELECT 1, dc.id, '香辣鸡翅', 12.00, '/dishes/chicken-wings.jpg', '微辣', 100, 1
FROM dish_category dc
WHERE dc.shop_id = 1
  AND dc.category_name = '小吃'
  AND NOT EXISTS (
    SELECT 1 FROM dish d WHERE d.shop_id = 1 AND d.dish_name = '香辣鸡翅'
  );

INSERT INTO dish (shop_id, category_id, dish_name, price, image_url, description, stock, status)
SELECT 1, dc.id, '柠檬红茶', 6.00, '/dishes/lemon-tea.jpg', '解腻推荐', 100, 1
FROM dish_category dc
WHERE dc.shop_id = 1
  AND dc.category_name = '饮品'
  AND NOT EXISTS (
    SELECT 1 FROM dish d WHERE d.shop_id = 1 AND d.dish_name = '柠檬红茶'
  );

INSERT INTO dish (shop_id, category_id, dish_name, price, image_url, description, stock, status)
SELECT 2, dc.id, '葱香排骨饭', 20.00, '/dishes/jiangnan-spare-ribs-rice.jpg', '红烧排骨配米饭，咸甜适口', 100, 1
FROM dish_category dc
WHERE dc.shop_id = 2
  AND dc.category_name = '主食'
  AND NOT EXISTS (
    SELECT 1 FROM dish d WHERE d.shop_id = 2 AND d.dish_name = '葱香排骨饭'
  );

INSERT INTO dish (shop_id, category_id, dish_name, price, image_url, description, stock, status)
SELECT 2, dc.id, '桂花糖藕', 12.00, '/dishes/jiangnan-osmanthus-lotus.jpg', '糯米藕淋桂花蜜，软糯清甜', 100, 1
FROM dish_category dc
WHERE dc.shop_id = 2
  AND dc.category_name = '小吃'
  AND NOT EXISTS (
    SELECT 1 FROM dish d WHERE d.shop_id = 2 AND d.dish_name = '桂花糖藕'
  );

INSERT INTO dish (shop_id, category_id, dish_name, price, image_url, description, stock, status)
SELECT 2, dc.id, '青梅苏打', 8.00, '/dishes/jiangnan-plum-soda.jpg', '青梅果酱气泡水，清爽解腻', 100, 1
FROM dish_category dc
WHERE dc.shop_id = 2
  AND dc.category_name = '饮品'
  AND NOT EXISTS (
    SELECT 1 FROM dish d WHERE d.shop_id = 2 AND d.dish_name = '青梅苏打'
  );

INSERT INTO dish (shop_id, category_id, dish_name, price, image_url, description, stock, status)
SELECT 3, dc.id, '韩式石锅拌饭', 23.00, '/dishes/korean-bibimbap.jpg', '石锅保温，溏心蛋配辣酱拌饭', 100, 1
FROM dish_category dc
WHERE dc.shop_id = 3
  AND dc.category_name = '主食'
  AND NOT EXISTS (
    SELECT 1 FROM dish d WHERE d.shop_id = 3 AND d.dish_name = '韩式石锅拌饭'
  );

INSERT INTO dish (shop_id, category_id, dish_name, price, image_url, description, stock, status)
SELECT 3, dc.id, '韩式炸鸡块', 17.00, '/dishes/korean-fried-chicken.jpg', '翅根翅中外酥里嫩，可选甜辣/酱油', 100, 1
FROM dish_category dc
WHERE dc.shop_id = 3
  AND dc.category_name = '小吃'
  AND NOT EXISTS (
    SELECT 1 FROM dish d WHERE d.shop_id = 3 AND d.dish_name = '韩式炸鸡块'
  );

INSERT INTO dish (shop_id, category_id, dish_name, price, image_url, description, stock, status)
SELECT 3, dc.id, '柚子蜂蜜茶', 10.00, '/dishes/korean-yuzu-honey-tea.jpg', '柚子酱冲饮，冷热皆宜', 100, 1
FROM dish_category dc
WHERE dc.shop_id = 3
  AND dc.category_name = '饮品'
  AND NOT EXISTS (
    SELECT 1 FROM dish d WHERE d.shop_id = 3 AND d.dish_name = '柚子蜂蜜茶'
  );

-- 江南小厨（shop_id=2）扩充菜品：定价参考校园商圈快餐（主食 13～24 元，小吃 9～18 元，饮品 5～11 元）
INSERT INTO dish (shop_id, category_id, dish_name, price, image_url, description, stock, status)
SELECT 2, dc.id, '盐水鸭饭', 22.00, '/dishes/beef-rice-bowl.jpg', '南京风味盐水鸭切片盖饭，配青菜', 80, 1
FROM dish_category dc
WHERE dc.shop_id = 2 AND dc.category_name = '主食'
  AND NOT EXISTS (SELECT 1 FROM dish d WHERE d.shop_id = 2 AND d.dish_name = '盐水鸭饭');

INSERT INTO dish (shop_id, category_id, dish_name, price, image_url, description, stock, status)
SELECT 2, dc.id, '虾仁滑蛋饭', 24.00, '/dishes/beef-rice-bowl.jpg', '嫩滑鸡蛋配鲜虾仁，葱油提香', 80, 1
FROM dish_category dc
WHERE dc.shop_id = 2 AND dc.category_name = '主食'
  AND NOT EXISTS (SELECT 1 FROM dish d WHERE d.shop_id = 2 AND d.dish_name = '虾仁滑蛋饭');

INSERT INTO dish (shop_id, category_id, dish_name, price, image_url, description, stock, status)
SELECT 2, dc.id, '阳春面', 13.00, '/dishes/beef-rice-bowl.jpg', '清汤细面，猪油香葱经典搭配', 120, 1
FROM dish_category dc
WHERE dc.shop_id = 2 AND dc.category_name = '主食'
  AND NOT EXISTS (SELECT 1 FROM dish d WHERE d.shop_id = 2 AND d.dish_name = '阳春面');

INSERT INTO dish (shop_id, category_id, dish_name, price, image_url, description, stock, status)
SELECT 2, dc.id, '糯米烧麦(两只)', 9.00, '/dishes/chicken-wings.jpg', '皮薄糯米馅，早餐下午茶皆宜', 100, 1
FROM dish_category dc
WHERE dc.shop_id = 2 AND dc.category_name = '小吃'
  AND NOT EXISTS (SELECT 1 FROM dish d WHERE d.shop_id = 2 AND d.dish_name = '糯米烧麦(两只)');

INSERT INTO dish (shop_id, category_id, dish_name, price, image_url, description, stock, status)
SELECT 2, dc.id, '香干马兰头', 11.00, '/dishes/jiangnan-osmanthus-lotus.jpg', '时令野菜拌香干，爽口小菜', 80, 1
FROM dish_category dc
WHERE dc.shop_id = 2 AND dc.category_name = '小吃'
  AND NOT EXISTS (SELECT 1 FROM dish d WHERE d.shop_id = 2 AND d.dish_name = '香干马兰头');

INSERT INTO dish (shop_id, category_id, dish_name, price, image_url, description, stock, status)
SELECT 2, dc.id, '糖醋小排(例份)', 18.00, '/dishes/jiangnan-spare-ribs-rice.jpg', '无锡风味酸甜排骨，分享装', 60, 1
FROM dish_category dc
WHERE dc.shop_id = 2 AND dc.category_name = '小吃'
  AND NOT EXISTS (SELECT 1 FROM dish d WHERE d.shop_id = 2 AND d.dish_name = '糖醋小排(例份)');

INSERT INTO dish (shop_id, category_id, dish_name, price, image_url, description, stock, status)
SELECT 2, dc.id, '桂花米酒酿', 7.00, '/dishes/lemon-tea.jpg', '酒酿小杯，桂花点缀，酒精度低', 100, 1
FROM dish_category dc
WHERE dc.shop_id = 2 AND dc.category_name = '饮品'
  AND NOT EXISTS (SELECT 1 FROM dish d WHERE d.shop_id = 2 AND d.dish_name = '桂花米酒酿');

INSERT INTO dish (shop_id, category_id, dish_name, price, image_url, description, stock, status)
SELECT 2, dc.id, '龙井奶绿', 11.00, '/dishes/jiangnan-plum-soda.jpg', '龙井茶底鲜奶茶，少糖推荐', 100, 1
FROM dish_category dc
WHERE dc.shop_id = 2 AND dc.category_name = '饮品'
  AND NOT EXISTS (SELECT 1 FROM dish d WHERE d.shop_id = 2 AND d.dish_name = '龙井奶绿');

INSERT INTO dish (shop_id, category_id, dish_name, price, image_url, description, stock, status)
SELECT 2, dc.id, '酸梅汤', 5.00, '/dishes/lemon-tea.jpg', '冰镇乌梅熬制，解暑开胃', 150, 1
FROM dish_category dc
WHERE dc.shop_id = 2 AND dc.category_name = '饮品'
  AND NOT EXISTS (SELECT 1 FROM dish d WHERE d.shop_id = 2 AND d.dish_name = '酸梅汤');

-- 韩味食堂（shop_id=3）扩充菜品：韩式简餐定价略高于江南店（主食 21～27 元，小吃 11～17 元，饮品 8～12 元）
INSERT INTO dish (shop_id, category_id, dish_name, price, image_url, description, stock, status)
SELECT 3, dc.id, '泡菜五花肉饭', 25.00, '/dishes/korean-bibimbap.jpg', '韩式辣酱炒五花肉配泡菜盖饭', 80, 1
FROM dish_category dc
WHERE dc.shop_id = 3 AND dc.category_name = '主食'
  AND NOT EXISTS (SELECT 1 FROM dish d WHERE d.shop_id = 3 AND d.dish_name = '泡菜五花肉饭');

INSERT INTO dish (shop_id, category_id, dish_name, price, image_url, description, stock, status)
SELECT 3, dc.id, '韩式牛肉汤饭', 27.00, '/dishes/korean-bibimbap.jpg', '牛骨清汤+bulgogi，配米饭和小菜', 70, 1
FROM dish_category dc
WHERE dc.shop_id = 3 AND dc.category_name = '主食'
  AND NOT EXISTS (SELECT 1 FROM dish d WHERE d.shop_id = 3 AND d.dish_name = '韩式牛肉汤饭');

INSERT INTO dish (shop_id, category_id, dish_name, price, image_url, description, stock, status)
SELECT 3, dc.id, '芝士年糕拉面', 21.00, '/dishes/korean-bibimbap.jpg', '拉面配年糕鱼饼，芝士浓汤', 90, 1
FROM dish_category dc
WHERE dc.shop_id = 3 AND dc.category_name = '主食'
  AND NOT EXISTS (SELECT 1 FROM dish d WHERE d.shop_id = 3 AND d.dish_name = '芝士年糕拉面');

INSERT INTO dish (shop_id, category_id, dish_name, price, image_url, description, stock, status)
SELECT 3, dc.id, '辣炒年糕', 14.00, '/dishes/korean-fried-chicken.jpg', '韩式辣酱炒年糕条，鱼饼搭档', 100, 1
FROM dish_category dc
WHERE dc.shop_id = 3 AND dc.category_name = '小吃'
  AND NOT EXISTS (SELECT 1 FROM dish d WHERE d.shop_id = 3 AND d.dish_name = '辣炒年糕');

INSERT INTO dish (shop_id, category_id, dish_name, price, image_url, description, stock, status)
SELECT 3, dc.id, '韩式煎饺(6只)', 12.00, '/dishes/chicken-wings.jpg', '脆皮猪肉白菜馅，配蘸醋', 100, 1
FROM dish_category dc
WHERE dc.shop_id = 3 AND dc.category_name = '小吃'
  AND NOT EXISTS (SELECT 1 FROM dish d WHERE d.shop_id = 3 AND d.dish_name = '韩式煎饺(6只)');

INSERT INTO dish (shop_id, category_id, dish_name, price, image_url, description, stock, status)
SELECT 3, dc.id, '鱼饼串(2串)', 11.00, '/dishes/chicken-wings.jpg', '关东煮风格鱼饼，甜辣酱可选', 120, 1
FROM dish_category dc
WHERE dc.shop_id = 3 AND dc.category_name = '小吃'
  AND NOT EXISTS (SELECT 1 FROM dish d WHERE d.shop_id = 3 AND d.dish_name = '鱼饼串(2串)');

INSERT INTO dish (shop_id, category_id, dish_name, price, image_url, description, stock, status)
SELECT 3, dc.id, '香蕉牛奶', 8.00, '/dishes/korean-yuzu-honey-tea.jpg', '韩式早餐奶昔口感，冰饮更佳', 100, 1
FROM dish_category dc
WHERE dc.shop_id = 3 AND dc.category_name = '饮品'
  AND NOT EXISTS (SELECT 1 FROM dish d WHERE d.shop_id = 3 AND d.dish_name = '香蕉牛奶');

INSERT INTO dish (shop_id, category_id, dish_name, price, image_url, description, stock, status)
SELECT 3, dc.id, '芦荟葡萄汁', 9.00, '/dishes/jiangnan-plum-soda.jpg', '芦荟粒配葡萄风味，清爽低负担', 100, 1
FROM dish_category dc
WHERE dc.shop_id = 3 AND dc.category_name = '饮品'
  AND NOT EXISTS (SELECT 1 FROM dish d WHERE d.shop_id = 3 AND d.dish_name = '芦荟葡萄汁');

INSERT INTO dish (shop_id, category_id, dish_name, price, image_url, description, stock, status)
SELECT 3, dc.id, '冰美式', 12.00, '/dishes/lemon-tea.jpg', '深烘美式咖啡，堂食外带皆可', 100, 1
FROM dish_category dc
WHERE dc.shop_id = 3 AND dc.category_name = '饮品'
  AND NOT EXISTS (SELECT 1 FROM dish d WHERE d.shop_id = 3 AND d.dish_name = '冰美式');

-- =========================
-- 性能优化（可选执行）
-- 说明：以下索引用于提升当前高频查询性能。
-- 若已存在同名索引，执行时会报错，可忽略或手动删除后重建。
-- =========================

-- 用户端菜品列表（按店铺/状态/分类过滤）
CREATE INDEX idx_dish_shop_status_category
ON dish (shop_id, status, category_id);

-- 商家订单列表（按店铺/状态/时间排序）
CREATE INDEX idx_orders_shop_status_time
ON orders (shop_id, status, create_time);

-- 购物车结算（按用户+勾选）
CREATE INDEX idx_cart_user_selected
ON cart_item (user_id, selected);

-- 支付记录后台查询（按订单+支付状态）
CREATE INDEX idx_payment_order_status
ON payment_record (order_id, pay_status);

SELECT DATABASE() AS current_db;
SHOW TABLES;
