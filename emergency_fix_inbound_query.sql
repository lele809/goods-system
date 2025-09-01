-- 紧急修复入库记录查询问题
-- 如果Java代码修改后仍有问题，请执行以下SQL检查

-- 1. 检查表是否存在
SHOW TABLES LIKE 'inbound_record';

-- 2. 检查表结构
DESCRIBE inbound_record;

-- 3. 检查是否有数据
SELECT COUNT(*) FROM inbound_record;

-- 4. 检查外键约束
SELECT 
    CONSTRAINT_NAME,
    TABLE_NAME,
    COLUMN_NAME,
    REFERENCED_TABLE_NAME,
    REFERENCED_COLUMN_NAME
FROM information_schema.KEY_COLUMN_USAGE 
WHERE TABLE_NAME = 'inbound_record' 
AND CONSTRAINT_NAME != 'PRIMARY';

-- 5. 测试基本查询
SELECT * FROM inbound_record LIMIT 5;

-- 6. 检查product表是否存在
SHOW TABLES LIKE 'product';

-- 7. 检查product表结构
DESCRIBE product;

-- 8. 检查product表数据
SELECT COUNT(*) FROM product;

-- 9. 测试JOIN查询
SELECT i.*, p.name as product_name 
FROM inbound_record i 
LEFT JOIN product p ON i.product_id = p.id 
LIMIT 5;

-- 10. 如果有外键约束问题，临时删除约束（谨慎使用）
-- ALTER TABLE inbound_record DROP FOREIGN KEY inbound_record_ibfk_1;

-- 11. 重新添加外键约束（在确认product表正常后）
-- ALTER TABLE inbound_record ADD CONSTRAINT inbound_record_ibfk_1 
-- FOREIGN KEY (product_id) REFERENCES product(id) ON DELETE CASCADE ON UPDATE RESTRICT;