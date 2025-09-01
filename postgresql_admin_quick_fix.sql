-- PostgreSQL生产环境快速修复方案
-- 如果您不想重新创建admin表，可以使用这个轻量级修复

-- 方案1：如果表结构字段名错误，修改表结构
-- 检查当前表结构
SELECT column_name, data_type 
FROM information_schema.columns 
WHERE table_name = 'admin' AND table_schema = 'public';

-- 如果字段名是username, password_hash，需要重命名
DO $$ 
BEGIN
    -- 检查并重命名username字段为admin_name
    IF EXISTS (SELECT 1 FROM information_schema.columns 
               WHERE table_name = 'admin' AND column_name = 'username') THEN
        ALTER TABLE admin RENAME COLUMN username TO admin_name;
    END IF;
    
    -- 检查并重命名password_hash字段为password
    IF EXISTS (SELECT 1 FROM information_schema.columns 
               WHERE table_name = 'admin' AND column_name = 'password_hash') THEN
        ALTER TABLE admin RENAME COLUMN password_hash TO password;
    END IF;
    
    -- 删除不需要的salt字段（如果存在）
    IF EXISTS (SELECT 1 FROM information_schema.columns 
               WHERE table_name = 'admin' AND column_name = 'salt') THEN
        ALTER TABLE admin DROP COLUMN salt;
    END IF;
END $$;

-- 方案2：更新admin账户数据
-- 删除现有的admin账户
DELETE FROM admin WHERE admin_name = 'admin';

-- 插入正确的admin账户（scrypt格式，密码：123456）
INSERT INTO admin (admin_name, password, last_login) VALUES 
('admin', 'scrypt:32768:8:1$qMPOCdL2hjKHfcbW$f3a306956f807dd7887ac4609d2aec3c71eca58d9f86e6f95a57e38b7ed70df7b5af9f3a766a5602138cda8f72d18cbf768fb61e755e00b4987a64d3f26018e4', NULL);

-- 验证修复结果
SELECT admin_name, 
       CASE 
           WHEN password LIKE 'scrypt:%' THEN '✓ scrypt格式正确'
           ELSE '✗ 密码格式错误'
       END as status,
       last_login
FROM admin 
WHERE admin_name = 'admin';