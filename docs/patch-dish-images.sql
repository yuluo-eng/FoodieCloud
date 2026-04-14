-- 已有数据库时执行：为默认三道菜补齐前端静态图路径（与 docs/init.sql 一致）
-- 前端通过 Vite public 提供：http://localhost:5173/dishes/*.jpg

UPDATE dish SET image_url = '/dishes/beef-rice-bowl.jpg'
WHERE shop_id = 1 AND dish_name = '招牌牛肉饭';

UPDATE dish SET image_url = '/dishes/chicken-wings.jpg'
WHERE shop_id = 1 AND dish_name = '香辣鸡翅';

UPDATE dish SET image_url = '/dishes/lemon-tea.jpg'
WHERE shop_id = 1 AND dish_name = '柠檬红茶';
