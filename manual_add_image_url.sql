-- ==========================================
-- 手动添加image_url字段的SQL脚本
-- 适用于MySQL数据库
-- ==========================================

-- 使用goods_system数据库
USE goods_system;

-- 检查image_url字段是否已存在
SELECT COUNT(*) as field_exists 
FROM INFORMATION_SCHEMA.COLUMNS 
WHERE TABLE_SCHEMA = 'goods_system' 
  AND TABLE_NAME = 'product' 
  AND COLUMN_NAME = 'image_url';

-- 如果字段不存在，添加image_url字段
-- 注意：在MySQL中，没有"IF NOT EXISTS"语法用于添加列，需要先检查
-- 如果上面的查询返回0，说明字段不存在，可以执行下面的语句

ALTER TABLE product ADD COLUMN image_url VARCHAR(2000) COMMENT '商品图片URL';

-- 验证字段是否添加成功
DESCRIBE product;

-- 可选：查看所有商品数据
SELECT id, name, spec, unit, initial_stock, price, amount, remaining_quantity, image_url, created_at 
FROM product 
LIMIT 10; 