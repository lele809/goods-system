-- ====================================
-- 数据库检查和修复脚本
-- 请在数据库管理工具中执行此脚本
-- ====================================

USE goods_system;

-- 1. 检查当前product表结构
SELECT '=== 当前 product 表结构 ===' as info;
DESCRIBE product;

-- 2. 检查image_url字段是否存在
SELECT '=== 检查 image_url 字段是否存在 ===' as info;
SELECT COUNT(*) as image_url_field_exists 
FROM INFORMATION_SCHEMA.COLUMNS 
WHERE TABLE_SCHEMA = 'goods_system' 
  AND TABLE_NAME = 'product' 
  AND COLUMN_NAME = 'image_url';

-- 3. 如果字段不存在，添加image_url字段
-- 注意：如果上面的查询返回0，说明字段不存在，需要执行下面的语句
-- 如果返回1，说明字段已存在，跳过下面的语句

-- ALTER TABLE product ADD COLUMN image_url VARCHAR(2000) COMMENT '商品图片URL';

-- 4. 验证修复结果
SELECT '=== 修复后的 product 表结构 ===' as info;
DESCRIBE product;

-- 5. 查看现有商品数据（包括image_url字段）
SELECT '=== 现有商品数据预览 ===' as info;
SELECT id, name, spec, unit, initial_stock, price, amount, remaining_quantity, 
       CASE 
         WHEN image_url IS NULL THEN '无图片'
         WHEN image_url = '' THEN '无图片'
         ELSE '有图片'
       END as image_status,
       created_at 
FROM product 
ORDER BY id 
LIMIT 5;

-- 6. 测试插入带图片的商品数据（可选）
-- INSERT INTO product (name, spec, unit, initial_stock, price, amount, remaining_quantity, image_url) 
-- VALUES ('测试商品', '测试规格', '个', 10, 15.00, 150.00, 10, 'data:image/jpeg;base64,/9j/4AAQSkZJRgABAQAAAQABAAD/...');

SELECT '=== 脚本执行完成 ===' as info; 