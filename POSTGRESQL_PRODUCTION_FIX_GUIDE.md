# PostgreSQL 生产环境修复指南

## 🔍 问题分析

您遇到的问题是典型的**开发环境与生产环境数据库差异**导致的：

- **开发环境**: MySQL
- **生产环境**: PostgreSQL (Neon)

两个数据库在SQL语法、参数绑定、日期处理等方面存在差异。

## 🚀 解决方案

我已经为您的代码添加了**多层降级策略**，包括：

1. **JPA查询** (首选)
2. **PostgreSQL兼容的原生SQL查询** (降级方案)
3. **简单查询** (最后的保障)

## 📋 部署步骤

### 1. 代码部署

确保以下文件已更新：
- `InboundRecordRepository.java` - 添加了PostgreSQL兼容查询
- `InboundRecordService.java` - 添加了降级策略
- `application-prod.properties` - PostgreSQL配置

### 2. 数据库诊断

在部署前，先运行诊断脚本检查数据库状态：

```bash
# 在 Neon 控制台中执行
psql -f postgresql_production_diagnostic.sql
```

### 3. 关键修复点

#### A. 日期参数处理
```java
// 原来 (MySQL兼容)
@Param("startDate") LocalDate startDate

// 现在 (PostgreSQL兼容)  
@Param("startDate") String startDate
```

#### B. 字符串连接
```sql
-- MySQL语法
CONCAT('%', :productName, '%')

-- PostgreSQL语法  
'%' || :productName || '%'
```

#### C. 日期转换
```sql
-- PostgreSQL中显式转换日期
CAST(:startDate AS DATE)
```

## 🔧 测试验证

### 1. 本地测试
```bash
# 使用PostgreSQL profile启动
java -jar goods.jar --spring.profiles.active=prod
```

### 2. 生产环境测试
访问入库管理页面，测试以下功能：
- ✅ 加载入库记录列表
- ✅ 按日期筛选
- ✅ 商品名称搜索
- ✅ 新增入库记录

## 📊 监控和日志

应用启动后，查看日志中的降级信息：
```
JPA查询失败，尝试原生SQL查询: ...
PostgreSQL原生查询失败: ...
```

## 🆘 故障排除

### 问题1: 仍然报JDBC错误
**解决方案**: 检查Neon数据库表结构
```sql
\d inbound_record
\d product
```

### 问题2: 外键约束错误
**解决方案**: 检查数据完整性
```sql
SELECT i.id, i.product_id 
FROM inbound_record i 
LEFT JOIN product p ON i.product_id = p.id 
WHERE p.id IS NULL;
```

### 问题3: 权限错误
**解决方案**: 检查数据库用户权限
```sql
SELECT grantee, privilege_type 
FROM information_schema.role_table_grants 
WHERE table_name = 'inbound_record';
```

## 🔄 回滚方案

如果新版本有问题，可以临时禁用复杂查询：

1. 修改 `InboundRecordService.java`
2. 让所有查询直接使用 `findAllOrderByIdDesc`
3. 重新部署

## 📈 性能优化建议

### 1. 添加索引
```sql
CREATE INDEX IF NOT EXISTS idx_inbound_product_date 
ON inbound_record(product_id, in_date);

CREATE INDEX IF NOT EXISTS idx_product_name_lower 
ON product(LOWER(name));
```

### 2. 连接池优化
在 `application-prod.properties` 中：
```properties
spring.datasource.hikari.maximum-pool-size=10
spring.datasource.hikari.minimum-idle=2
spring.datasource.hikari.connection-timeout=30000
```

## ✅ 预期效果

修复后应该能够：
- ✅ 正常加载入库记录
- ✅ 按日期筛选入库记录  
- ✅ 搜索商品名称
- ✅ 当某日期无记录时显示"无入库记录"
- ✅ 兼容MySQL开发环境和PostgreSQL生产环境

## 📞 支持

如果仍有问题，请提供：
1. 完整的错误日志
2. `postgresql_production_diagnostic.sql` 的执行结果
3. 应用启动日志

---

**重要提示**: 部署前建议先在测试环境验证，确保所有功能正常后再部署到生产环境。