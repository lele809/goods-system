-- 修复admin登录问题
-- 确保admin账户使用正确的scrypt格式密码，密码为123456

-- 删除可能存在的错误admin账户
DELETE FROM admin WHERE admin_name = 'admin';

-- 插入正确的admin账户（使用scrypt格式，密码：123456）
INSERT INTO admin (admin_name, password, last_login) VALUES 
('admin', 'scrypt:32768:8:1$qMPOCdL2hjKHfcbW$f3a306956f807dd7887ac4609d2aec3c71eca58d9f86e6f95a57e38b7ed70df7b5af9f3a766a5602138cda8f72d18cbf768fb61e755e00b4987a64d3f26018e4', NULL);

-- 同时确保其他测试账户也存在（密码都是123456）
INSERT INTO admin (admin_name, password, last_login) VALUES 
('lele', 'scrypt:32768:8:1$Sh7fpO5g0usdveeN$73fb14f3004958bfc85641af7fe6ea2de97f335cd1d0a1a12a1b3a6c3bedd04973f4ba48b98c1de2516ce8ca32288a61cc9cfb1567d9466286e01bfef4c61492', NULL),
('ling', 'scrypt:32768:8:1$s1UcREb40Colw6li$9b9335bbe8b8c7390c92a9944fb72be176e57e6995744f78530067071423986c37683933b1715bda4fa17d6d7cc2469f679a71022ee128742a6e043d71a45e54', NULL)
ON DUPLICATE KEY UPDATE 
password = VALUES(password);

-- 验证结果
SELECT admin_name, password, last_login FROM admin ORDER BY id;