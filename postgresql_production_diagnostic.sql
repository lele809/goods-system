-- PostgreSQL 生产环境诊断脚本（专门针对 Neon 数据库）
-- 请在您的 Neon 控制台或 PostgreSQL 客户端中执行

-- 1. 检查数据库版本和配置
SELECT version() AS postgresql_version;
SHOW timezone;
SHOW lc_collate;
SHOW default_transaction_isolation;

-- 2. 检查表是否存在
SELECT tablename FROM pg_tables WHERE tablename IN ('inbound_record', 'product');

-- 3. 检查 inbound_record 表结构
\d inbound_record

-- 4. 检查 product 表结构  
\d product

-- 5. 检查表的索引
SELECT indexname, tablename, indexdef 
FROM pg_indexes 
WHERE tablename IN ('inbound_record', 'product');

-- 6. 检查外键约束
SELECT
    tc.table_name, 
    tc.constraint_name, 
    tc.constraint_type, 
    kcu.column_name,
    ccu.table_name AS foreign_table_name,
    ccu.column_name AS foreign_column_name 
FROM 
    information_schema.table_constraints AS tc 
    JOIN information_schema.key_column_usage AS kcu
      ON tc.constraint_name = kcu.constraint_name
    JOIN information_schema.constraint_column_usage AS ccu
      ON ccu.constraint_name = tc.constraint_name
WHERE tc.table_name IN ('inbound_record', 'product')
  AND tc.constraint_type = 'FOREIGN KEY';

-- 7. 检查数据量
SELECT COUNT(*) AS inbound_record_count FROM inbound_record;
SELECT COUNT(*) AS product_count FROM product;

-- 8. 检查数据类型兼容性
SELECT 
    column_name,
    data_type,
    is_nullable,
    column_default,
    character_maximum_length
FROM information_schema.columns 
WHERE table_name = 'inbound_record' 
ORDER BY ordinal_position;

-- 9. 检查是否有问题数据
SELECT COUNT(*) as null_product_id_count FROM inbound_record WHERE product_id IS NULL;
SELECT COUNT(*) as null_in_date_count FROM inbound_record WHERE in_date IS NULL;

-- 10. 测试基本查询
SELECT * FROM inbound_record ORDER BY id DESC LIMIT 5;

-- 11. 测试JOIN查询（这是最可能出问题的地方）
SELECT i.id, i.product_id, i.in_date, i.quantity, p.name as product_name
FROM inbound_record i 
LEFT JOIN product p ON i.product_id = p.id 
ORDER BY i.id DESC 
LIMIT 5;

-- 12. 检查是否存在损坏的外键引用
SELECT i.id, i.product_id, 'Missing Product' as issue
FROM inbound_record i 
LEFT JOIN product p ON i.product_id = p.id 
WHERE p.id IS NULL 
LIMIT 10;

-- 13. 测试日期查询（这是报错的查询类型）
SELECT COUNT(*) 
FROM inbound_record i 
WHERE i.in_date >= CAST('2024-01-01' AS DATE)
  AND i.in_date <= CAST('2024-12-31' AS DATE);

-- 14. 测试参数化查询（模拟Spring Boot的查询）
-- 注意：这在某些PostgreSQL客户端中可能不工作，但可以帮助理解问题
PREPARE test_query (bigint, date, date) AS
SELECT * FROM inbound_record 
WHERE ($1 IS NULL OR product_id = $1) 
  AND ($2 IS NULL OR in_date >= $2) 
  AND ($3 IS NULL OR in_date <= $3)
ORDER BY id DESC 
LIMIT 10;

-- 执行测试查询
EXECUTE test_query(NULL, '2024-01-01', '2024-12-31');

-- 清理
DEALLOCATE test_query;

-- 15. 检查权限
SELECT grantee, privilege_type 
FROM information_schema.role_table_grants 
WHERE table_name IN ('inbound_record', 'product');

-- 16. 检查连接限制和当前连接数
SELECT setting FROM pg_settings WHERE name = 'max_connections';
SELECT count(*) as current_connections FROM pg_stat_activity;

-- 17. 检查表统计信息
SELECT schemaname, tablename, n_tup_ins, n_tup_upd, n_tup_del, n_live_tup, n_dead_tup
FROM pg_stat_user_tables 
WHERE tablename IN ('inbound_record', 'product');

-- 18. 如果表为空，插入测试数据
-- 注意：只有在确认表为空且需要测试时才执行
/*
INSERT INTO product (name, spec, unit, initial_stock, price, amount, remaining_quantity) 
VALUES ('测试商品', '测试规格', '个', 100, 10.00, 1000.00, 100);

INSERT INTO inbound_record (product_id, in_date, quantity) 
VALUES (1, CURRENT_DATE, 10);
*/