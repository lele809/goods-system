-- 生产环境修复脚本
-- 请在生产环境中按顺序执行以下步骤

-- 第一步：检查当前表结构
DESCRIBE inbound_record;

-- 第二步：检查是否缺少索引（如果缺少则添加）
CREATE INDEX IF NOT EXISTS idx_inbound_product_id ON inbound_record(product_id);
CREATE INDEX IF NOT EXISTS idx_inbound_date ON inbound_record(in_date);
CREATE INDEX IF NOT EXISTS idx_inbound_created_at ON inbound_record(created_at);

-- 第三步：检查并修复外键约束（如果不存在）
-- 注意：只有当product表存在且数据完整时才执行
-- SELECT COUNT(*) FROM information_schema.KEY_COLUMN_USAGE 
-- WHERE TABLE_NAME = 'inbound_record' AND CONSTRAINT_NAME LIKE '%ibfk%';

-- 如果没有外键约束，可以添加（谨慎操作）
-- ALTER TABLE inbound_record 
-- ADD CONSTRAINT inbound_record_ibfk_1 
-- FOREIGN KEY (product_id) REFERENCES product(id) 
-- ON DELETE CASCADE ON UPDATE RESTRICT;

-- 第四步：检查数据完整性
-- 查找没有对应商品的入库记录
SELECT i.id, i.product_id 
FROM inbound_record i 
LEFT JOIN product p ON i.product_id = p.id 
WHERE p.id IS NULL;

-- 第五步：清理无效数据（如果有的话）
-- DELETE FROM inbound_record 
-- WHERE product_id NOT IN (SELECT id FROM product);

-- 第六步：优化表
OPTIMIZE TABLE inbound_record;
OPTIMIZE TABLE product;

-- 第七步：更新表统计信息
ANALYZE TABLE inbound_record;
ANALYZE TABLE product;

-- 第八步：检查表状态
SHOW TABLE STATUS LIKE 'inbound_record';
SHOW TABLE STATUS LIKE 'product';

-- 第九步：测试查询性能
EXPLAIN SELECT * FROM inbound_record 
WHERE product_id = 1 
AND in_date >= '2024-01-01' 
AND in_date <= '2024-12-31'
ORDER BY id DESC 
LIMIT 10;

-- 第十步：如果使用了分区表，检查分区状态
-- SELECT PARTITION_NAME, TABLE_ROWS 
-- FROM information_schema.PARTITIONS 
-- WHERE TABLE_NAME = 'inbound_record';