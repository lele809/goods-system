-- 紧急修复image_url字段类型不匹配问题
-- 解决添加图片时的JDBC异常

USE goods_system;

-- 1. 检查当前字段状态
SELECT 
    COLUMN_NAME,
    DATA_TYPE,
    CHARACTER_MAXIMUM_LENGTH,
    COLUMN_TYPE,
    IS_NULLABLE,
    COLUMN_COMMENT
FROM INFORMATION_SCHEMA.COLUMNS 
WHERE TABLE_SCHEMA = 'goods_system' 
  AND TABLE_NAME = 'product' 
  AND COLUMN_NAME = 'image_url';

-- 2. 修改字段类型为TEXT（与Java实体类匹配）
-- 这将支持更长的图片URL和base64数据
ALTER TABLE product 
MODIFY COLUMN image_url TEXT COMMENT '商品图片URL或base64数据';

-- 3. 同样修复其他表的image_url字段（如果存在类似问题）
-- inbound_record表已经是LONGTEXT，无需修改
-- outbound_record表已经是LONGTEXT，无需修改

-- 4. 验证修改结果
SELECT 
    COLUMN_NAME,
    DATA_TYPE,
    CHARACTER_MAXIMUM_LENGTH,
    COLUMN_TYPE,
    IS_NULLABLE,
    COLUMN_COMMENT
FROM INFORMATION_SCHEMA.COLUMNS 
WHERE TABLE_SCHEMA = 'goods_system' 
  AND TABLE_NAME = 'product' 
  AND COLUMN_NAME = 'image_url';

-- 5. 查看当前表结构
DESCRIBE product;

-- 6. 测试查询（这应该不再报错）
SELECT id, name, image_url FROM product LIMIT 3;

-- 修复完成说明：
-- - 将product表的image_url字段从VARCHAR(2000)改为TEXT
-- - TEXT类型可以存储最多65,535字符，足够存储大部分图片URL和中小型base64图片
-- - 如果需要存储更大的base64图片，可以进一步改为LONGTEXT