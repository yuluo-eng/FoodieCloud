-- 已有数据库增量：江南小厨(2)、韩味食堂(3) 调价 + 扩充菜品（与 docs/init.sql 保持一致）
-- 可重复执行：新菜品使用 NOT EXISTS；调价直接 UPDATE。

UPDATE dish SET price = 20.00, description = '红烧排骨配米饭，咸甜适口'
WHERE shop_id = 2 AND dish_name = '葱香排骨饭';
UPDATE dish SET price = 12.00, description = '糯米藕淋桂花蜜，软糯清甜'
WHERE shop_id = 2 AND dish_name = '桂花糖藕';
UPDATE dish SET description = '青梅果酱气泡水，清爽解腻'
WHERE shop_id = 2 AND dish_name = '青梅苏打';

UPDATE dish SET price = 23.00, description = '石锅保温，溏心蛋配辣酱拌饭'
WHERE shop_id = 3 AND dish_name = '韩式石锅拌饭';
UPDATE dish SET price = 17.00, description = '翅根翅中外酥里嫩，可选甜辣/酱油'
WHERE shop_id = 3 AND dish_name = '韩式炸鸡块';
UPDATE dish SET price = 10.00, description = '柚子酱冲饮，冷热皆宜'
WHERE shop_id = 3 AND dish_name = '柚子蜂蜜茶';

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
