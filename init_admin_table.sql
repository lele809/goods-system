-- 创建admin表
CREATE TABLE IF NOT EXISTS admin (
    id BIGSERIAL PRIMARY KEY,
    admin_name VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    last_login TIMESTAMP
);

-- 插入默认管理员账户
INSERT INTO admin (admin_name, password) 
VALUES ('admin', '$2a$10$N.zmdr9k7uOCQb96VdOYx.93UGHmZJINVMe8uP8jUp2OKIBOYsqce')
ON CONFLICT (admin_name) DO NOTHING;

-- 注意：密码是 "admin123" 的bcrypt加密结果