-- 货架商品管理系统性能优化脚本
-- 适用于 MySQL/MariaDB 数据库
-- 执行前请备份数据库！

-- ====================
-- 1. 检查当前表结构和索引状态
-- ====================

-- 检查现有索引
SHOW INDEX FROM product;
SHOW INDEX FROM inbound_record;
SHOW INDEX FROM outbound_record;

-- 查看表状态
SHOW TABLE STATUS LIKE 'product';
SHOW TABLE STATUS LIKE 'inbound_record';
SHOW TABLE STATUS LIKE 'outbound_record';

-- ====================
-- 2. 添加缺失的索引以提高查询性能
-- ====================

-- 商品表索引优化
CREATE INDEX IF NOT EXISTS idx_product_name_spec ON product(name, spec);
CREATE INDEX IF NOT EXISTS idx_product_remaining_quantity ON product(remaining_quantity);
CREATE INDEX IF NOT EXISTS idx_product_price ON product(price);
CREATE INDEX IF NOT EXISTS idx_product_created_at_desc ON product(created_at DESC);

-- 入库记录表索引优化
CREATE INDEX IF NOT EXISTS idx_inbound_product_date ON inbound_record(product_id, in_date);
CREATE INDEX IF NOT EXISTS idx_inbound_date_desc ON inbound_record(in_date DESC);
CREATE INDEX IF NOT EXISTS idx_inbound_created_at_desc ON inbound_record(created_at DESC);

-- 出库记录表索引优化
CREATE INDEX IF NOT EXISTS idx_outbound_product_date ON outbound_record(product_id, out_date);
CREATE INDEX IF NOT EXISTS idx_outbound_date_desc ON outbound_record(out_date DESC);
CREATE INDEX IF NOT EXISTS idx_outbound_payment_status ON outbound_record(payment_status);
CREATE INDEX IF NOT EXISTS idx_outbound_name_payment ON outbound_record(name, payment_status);
CREATE INDEX IF NOT EXISTS idx_outbound_created_at_desc ON outbound_record(created_at DESC);

-- 复合索引优化（针对常用查询条件）
CREATE INDEX IF NOT EXISTS idx_inbound_product_date_quantity ON inbound_record(product_id, in_date, quantity);
CREATE INDEX IF NOT EXISTS idx_outbound_product_date_quantity ON outbound_record(product_id, out_date, quantity);

-- ====================
-- 3. 优化表结构（如果需要）
-- ====================

-- 确保字符串字段有合适的长度
ALTER TABLE product 
  MODIFY COLUMN name VARCHAR(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci,
  MODIFY COLUMN spec VARCHAR(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci,
  MODIFY COLUMN unit VARCHAR(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

ALTER TABLE outbound_record 
  MODIFY COLUMN name VARCHAR(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- ====================
-- 4. 优化表存储引擎和配置
-- ====================

-- 确保使用InnoDB引擎（支持事务和外键）
ALTER TABLE product ENGINE=InnoDB;
ALTER TABLE inbound_record ENGINE=InnoDB;
ALTER TABLE outbound_record ENGINE=InnoDB;

-- 优化表的行格式
ALTER TABLE product ROW_FORMAT=DYNAMIC;
ALTER TABLE inbound_record ROW_FORMAT=DYNAMIC;
ALTER TABLE outbound_record ROW_FORMAT=DYNAMIC;

-- ====================
-- 5. 更新表统计信息
-- ====================

-- 重新分析表以更新索引统计信息
ANALYZE TABLE product;
ANALYZE TABLE inbound_record;
ANALYZE TABLE outbound_record;

-- 优化表空间
OPTIMIZE TABLE product;
OPTIMIZE TABLE inbound_record;
OPTIMIZE TABLE outbound_record;

-- ====================
-- 6. 性能测试查询
-- ====================

-- 测试商品列表查询性能
EXPLAIN SELECT * FROM product 
WHERE name LIKE '%汤料%' 
ORDER BY created_at DESC 
LIMIT 10;

-- 测试入库记录查询性能
EXPLAIN SELECT ir.*, p.name, p.spec, p.unit, p.price 
FROM inbound_record ir 
LEFT JOIN product p ON ir.product_id = p.id 
WHERE ir.in_date >= '2024-01-01' 
ORDER BY ir.created_at DESC 
LIMIT 10;

-- 测试出库记录查询性能
EXPLAIN SELECT or_table.*, p.name, p.spec, p.unit, p.price 
FROM outbound_record or_table 
LEFT JOIN product p ON or_table.product_id = p.id 
WHERE or_table.out_date >= '2024-01-01' 
ORDER BY or_table.created_at DESC 
LIMIT 10;

-- 测试库存统计查询性能
EXPLAIN SELECT 
    p.id,
    p.name,
    p.spec,
    p.unit,
    p.initial_stock,
    COALESCE(inbound_sum.total_inbound, 0) as total_inbound,
    COALESCE(outbound_sum.total_outbound, 0) as total_outbound
FROM product p
LEFT JOIN (
    SELECT product_id, SUM(quantity) as total_inbound 
    FROM inbound_record 
    WHERE in_date <= CURDATE() 
    GROUP BY product_id
) inbound_sum ON p.id = inbound_sum.product_id
LEFT JOIN (
    SELECT product_id, SUM(quantity) as total_outbound 
    FROM outbound_record 
    WHERE out_date <= CURDATE() 
    GROUP BY product_id
) outbound_sum ON p.id = outbound_sum.product_id;

-- ====================
-- 7. 清理和维护建议
-- ====================

-- 检查重复数据
SELECT name, spec, COUNT(*) as count 
FROM product 
GROUP BY name, spec 
HAVING COUNT(*) > 1;

-- 检查数据完整性
SELECT 'inbound_orphans' as issue_type, COUNT(*) as count
FROM inbound_record ir 
LEFT JOIN product p ON ir.product_id = p.id 
WHERE p.id IS NULL

UNION ALL

SELECT 'outbound_orphans' as issue_type, COUNT(*) as count
FROM outbound_record or_table 
LEFT JOIN product p ON or_table.product_id = p.id 
WHERE p.id IS NULL;

-- ====================
-- 执行完成提示
-- ====================
SELECT '性能优化脚本执行完成！' as message,
       NOW() as execution_time;