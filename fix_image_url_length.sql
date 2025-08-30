-- 修复image_url字段长度限制问题
-- 将VARCHAR(2000)改为LONGTEXT以支持base64图片数据

USE goods_system;

-- 1. 检查当前字段类型
SELECT 
    COLUMN_NAME,
    DATA_TYPE,
    CHARACTER_MAXIMUM_LENGTH,
    COLUMN_TYPE
FROM INFORMATION_SCHEMA.COLUMNS 
WHERE TABLE_SCHEMA = 'goods_system' 
  AND TABLE_NAME = 'product' 
  AND COLUMN_NAME = 'image_url';

-- 2. 修改字段类型为LONGTEXT
ALTER TABLE product 
MODIFY COLUMN image_url LONGTEXT COMMENT '商品图片URL或base64数据';

-- 3. 验证修改结果
SELECT 
    COLUMN_NAME,
    DATA_TYPE,
    CHARACTER_MAXIMUM_LENGTH,
    COLUMN_TYPE
FROM INFORMATION_SCHEMA.COLUMNS 
WHERE TABLE_SCHEMA = 'goods_system' 
  AND TABLE_NAME = 'product' 
  AND COLUMN_NAME = 'image_url';

-- 4. 查看表结构
DESCRIBE product; 