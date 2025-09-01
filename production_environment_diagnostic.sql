-- 生产环境诊断脚本
-- 请在生产环境数据库中执行以下SQL来诊断问题

-- 1. 检查数据库版本和配置
SELECT VERSION() AS database_version;
SELECT @@sql_mode AS sql_mode;
SELECT @@character_set_database AS charset;
SELECT @@collation_database AS collation;

-- 2. 检查表是否存在
SHOW TABLES LIKE 'inbound_record';
SHOW TABLES LIKE 'product';

-- 3. 检查 inbound_record 表结构
DESCRIBE inbound_record;

-- 4. 检查 product 表结构  
DESCRIBE product;

-- 5. 检查表的索引
SHOW INDEX FROM inbound_record;
SHOW INDEX FROM product;

-- 6. 检查外键约束
SELECT 
    CONSTRAINT_NAME,
    TABLE_NAME,
    COLUMN_NAME,
    REFERENCED_TABLE_NAME,
    REFERENCED_COLUMN_NAME,
    UPDATE_RULE,
    DELETE_RULE
FROM information_schema.KEY_COLUMN_USAGE 
WHERE TABLE_NAME IN ('inbound_record', 'product') 
AND CONSTRAINT_NAME != 'PRIMARY';

-- 7. 检查数据量
SELECT COUNT(*) AS inbound_record_count FROM inbound_record;
SELECT COUNT(*) AS product_count FROM product;

-- 8. 检查是否有问题数据
SELECT * FROM inbound_record WHERE product_id IS NULL LIMIT 5;
SELECT * FROM inbound_record WHERE in_date IS NULL LIMIT 5;

-- 9. 测试基本查询
SELECT * FROM inbound_record ORDER BY id DESC LIMIT 5;

-- 10. 测试JOIN查询
SELECT i.*, p.name 
FROM inbound_record i 
LEFT JOIN product p ON i.product_id = p.id 
LIMIT 5;

-- 11. 检查数据类型兼容性
SELECT 
    COLUMN_NAME,
    DATA_TYPE,
    IS_NULLABLE,
    COLUMN_DEFAULT,
    CHARACTER_MAXIMUM_LENGTH
FROM information_schema.COLUMNS 
WHERE TABLE_NAME = 'inbound_record' 
ORDER BY ORDINAL_POSITION;

-- 12. 检查是否存在损坏的外键引用
SELECT i.id, i.product_id 
FROM inbound_record i 
LEFT JOIN product p ON i.product_id = p.id 
WHERE p.id IS NULL 
LIMIT 10;