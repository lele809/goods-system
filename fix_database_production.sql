-- 修复生产环境数据库字段类型和优化脚本
-- 执行前请备份数据库！

BEGIN;

-- 1. 修复 image_url 字段类型（如果还是 VARCHAR(2000)）
-- 使用安全的方式修改字段类型
DO $$
BEGIN
    -- 检查并修复product表的image_url字段
    IF EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_name = 'product' 
        AND column_name = 'image_url' 
        AND data_type = 'character varying'
    ) THEN
        ALTER TABLE product ALTER COLUMN image_url TYPE TEXT;
        RAISE NOTICE 'Updated product.image_url to TEXT';
    END IF;
    
    -- 检查并修复inbound_record表的image_url字段
    IF EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_name = 'inbound_record' 
        AND column_name = 'image_url' 
        AND data_type = 'character varying'
    ) THEN
        ALTER TABLE inbound_record ALTER COLUMN image_url TYPE TEXT;
        RAISE NOTICE 'Updated inbound_record.image_url to TEXT';
    END IF;
    
    -- 检查并修复outbound_record表的image_url字段
    IF EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_name = 'outbound_record' 
        AND column_name = 'image_url' 
        AND data_type = 'character varying'
    ) THEN
        ALTER TABLE outbound_record ALTER COLUMN image_url TYPE TEXT;
        RAISE NOTICE 'Updated outbound_record.image_url to TEXT';
    END IF;
END
$$;

-- 2. 添加索引以提高查询性能
CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_product_name ON product(name);
CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_product_spec ON product(spec);
CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_inbound_record_product_id ON inbound_record(product_id);
CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_inbound_record_in_date ON inbound_record(in_date);
CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_outbound_record_product_id ON outbound_record(product_id);
CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_outbound_record_out_date ON outbound_record(out_date);
CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_outbound_record_payment_status ON outbound_record(payment_status);

-- 3. 确保字符串字段有适当的长度限制
ALTER TABLE product ALTER COLUMN name TYPE VARCHAR(255);
ALTER TABLE product ALTER COLUMN spec TYPE VARCHAR(255);
ALTER TABLE product ALTER COLUMN unit TYPE VARCHAR(50);
ALTER TABLE outbound_record ALTER COLUMN name TYPE VARCHAR(255);

-- 4. 优化PostgreSQL配置（如果有权限）
-- 注意：这些配置需要数据库管理员权限
-- ALTER SYSTEM SET shared_preload_libraries = 'pg_stat_statements';
-- ALTER SYSTEM SET max_connections = 100;
-- ALTER SYSTEM SET shared_buffers = '256MB';
-- ALTER SYSTEM SET effective_cache_size = '1GB';
-- ALTER SYSTEM SET work_mem = '4MB';
-- ALTER SYSTEM SET maintenance_work_mem = '64MB';
-- SELECT pg_reload_conf();

-- 5. 清理可能的孤立prepared statements（如果有权限）
-- DEALLOCATE ALL;

-- 6. 更新表统计信息
ANALYZE product;
ANALYZE inbound_record;
ANALYZE outbound_record;
ANALYZE admin;

COMMIT;