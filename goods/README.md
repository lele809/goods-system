# 货架商品管理系统后端

## 项目概述
基于Spring Boot 3.x开发的货架商品管理系统后端API，提供商品管理、出库记录、库存统计等功能。

## 技术栈
- Spring Boot 3.0.2
- Spring Data JPA
- MySQL 8.x
- Lombok
- Java 17

## 项目结构
```
src/main/java/com/shelf/
├── controller/     # REST API控制器
├── service/       # 业务逻辑层
├── repository/    # 数据访问层
├── entity/        # JPA实体类
├── dto/          # 数据传输对象
└── ShelfApplication.java  # 主启动类
```

## 数据库配置
1. 创建数据库：
```sql
CREATE DATABASE goods_system CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

2. 执行数据库初始化脚本：`database_init.sql` 或 `goods_system.sql`

3. 修改配置文件 `application.properties` 中的数据库连接信息：
```properties
spring.datasource.username=root
spring.datasource.password=123456
```

## 运行项目
1. 确保已安装Java 17和MySQL 8.x
2. 配置数据库连接
3. 运行项目：
```bash
./gradlew bootRun
```

## API接口

### 健康检查
- GET `/api/health` - 系统健康检查
- GET `/api/test` - API测试接口

### 商品管理
- GET `/api/products` - 分页查询商品列表
- GET `/api/products/all` - 获取所有商品（导出用）
- GET `/api/product/{id}` - 根据ID获取商品
- POST `/api/product` - 添加/更新商品
- DELETE `/api/product/{id}` - 删除商品
- GET `/api/products/low-stock` - 查询库存不足商品

### 出库记录
- GET `/api/outbounds` - 分页查询出库记录
- POST `/api/outbound` - 创建出库记录
- GET `/api/outbound/{id}` - 根据ID获取出库记录
- DELETE `/api/outbound/{id}` - 删除出库记录
- GET `/api/outbounds/trend` - 获取出库趋势数据
- GET `/api/outbounds/total` - 统计出库总量
- GET `/api/outbounds/export` - 导出出库记录

### 管理员
- POST `/api/admin/login` - 管理员登录
- GET `/api/admin/check/{adminName}` - 检查用户名是否存在
- GET `/api/admin/{adminName}` - 获取管理员信息

## API响应格式
```json
{
  "code": 200,
  "message": "操作成功",
  "data": {...}
}
```

## 访问地址
- 应用地址：http://localhost:8084
- API基础地址：http://localhost:8084/api
- 健康检查：http://localhost:8084/api/health 