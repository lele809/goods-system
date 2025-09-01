-- 安全的PostgreSQL admin表修复脚本
-- 处理表不存在的情况

-- 第1步：检查admin表是否存在
DO $$ 
BEGIN
    -- 如果admin表存在，先备份
    IF EXISTS (SELECT 1 FROM information_schema.tables 
               WHERE table_name = 'admin' AND table_schema = 'public') THEN
        
        -- 创建备份表
        DROP TABLE IF EXISTS admin_backup;
        CREATE TABLE admin_backup AS SELECT * FROM admin;
        RAISE NOTICE 'admin表已备份到admin_backup';
        
        -- 删除现有的admin表
        DROP TABLE admin CASCADE;
        RAISE NOTICE '已删除现有admin表';
    ELSE
        RAISE NOTICE 'admin表不存在，将创建新表';
    END IF;
END $$;

-- 第2步：创建正确的admin表结构（匹配Java Entity）
CREATE TABLE admin (
    id BIGSERIAL PRIMARY KEY,
    admin_name VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    last_login TIMESTAMP
);

-- 第3步：创建索引
CREATE INDEX idx_admin_name ON admin(admin_name);

-- 第4步：插入admin账户（scrypt格式，密码：123456）
INSERT INTO admin (admin_name, password, last_login) VALUES 
('admin', 'scrypt:32768:8:1$qMPOCdL2hjKHfcbW$f3a306956f807dd7887ac4609d2aec3c71eca58d9f86e6f95a57e38b7ed70df7b5af9f3a766a5602138cda8f72d18cbf768fb61e755e00b4987a64d3f26018e4', NULL);

-- 第5步：插入其他测试账户（密码都是123456）
INSERT INTO admin (admin_name, password, last_login) VALUES 
('lele', 'scrypt:32768:8:1$Sh7fpO5g0usdveeN$73fb14f3004958bfc85641af7fe6ea2de97f335cd1d0a1a12a1b3a6c3bedd04973f4ba48b98c1de2516ce8ca32288a61cc9cfb1567d9466286e01bfef4c61492', NULL),
('ling', 'scrypt:32768:8:1$s1UcREb40Colw6li$9b9335bbe8b8c7390c92a9944fb72be176e57e6995744f78530067071423986c37683933b1715bda4fa17d6d7cc2469f679a71022ee128742a6e043d71a45e54', NULL);

-- 第6步：验证结果
SELECT 
    id, 
    admin_name, 
    CASE 
        WHEN password LIKE 'scrypt:%' THEN '✓ scrypt格式正确'
        ELSE '✗ 密码格式错误: ' || LEFT(password, 20) || '...'
    END as password_format,
    last_login,
    '密码: 123456' as note
FROM admin 
ORDER BY id;

-- 显示表结构确认
SELECT column_name, data_type, is_nullable 
FROM information_schema.columns 
WHERE table_name = 'admin' AND table_schema = 'public'
ORDER BY ordinal_position;