-- 已有数据库：将江南小厨(shop_id=2)、韩味食堂(shop_id=3) 六道菜的图片改为本地静态资源（与 frontend/public/dishes 下文件对应）
-- 执行前请确保已复制图片到前端 public 目录并完成构建/部署。

UPDATE dish SET image_url = '/dishes/jiangnan-spare-ribs-rice.jpg'
WHERE shop_id = 2 AND dish_name = '葱香排骨饭';

UPDATE dish SET image_url = '/dishes/jiangnan-osmanthus-lotus.jpg'
WHERE shop_id = 2 AND dish_name = '桂花糖藕';

UPDATE dish SET image_url = '/dishes/jiangnan-plum-soda.jpg'
WHERE shop_id = 2 AND dish_name = '青梅苏打';

UPDATE dish SET image_url = '/dishes/korean-bibimbap.jpg'
WHERE shop_id = 3 AND dish_name = '韩式石锅拌饭';

UPDATE dish SET image_url = '/dishes/korean-fried-chicken.jpg'
WHERE shop_id = 3 AND dish_name = '韩式炸鸡块';

UPDATE dish SET image_url = '/dishes/korean-yuzu-honey-tea.jpg'
WHERE shop_id = 3 AND dish_name = '柚子蜂蜜茶';
