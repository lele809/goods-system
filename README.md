# 货架商品管理系统

一个基于 Spring Boot + Vue 3 + MySQL 的现代化商品管理系统，用于管理商品库存、入库出库记录等。

## 🚀 技术栈

### 后端
- **Spring Boot 3.0.2** - Java web框架
- **Spring Data JPA** - 数据持久化
- **MySQL 8.0** - 数据库
- **Gradle** - 构建工具
- **Spring Security** - 密码加密

### 前端
- **Vue 3** - 前端框架
- **TypeScript** - 类型安全
- **Element Plus** - UI组件库
- **Vite** - 构建工具
- **Pinia** - 状态管理
- **Axios** - HTTP客户端
- **ECharts** - 数据可视化

## 📋 功能特性

- ✅ 用户登录认证
- ✅ 商品信息管理（增删改查）
- ✅ 库存管理
- ✅ 入库记录管理
- ✅ 出库记录管理
- ✅ 数据统计与报表
- ✅ 响应式设计
- ✅ 数据导出功能

## 🛠️ 快速开始

### 环境要求

- JDK 17+
- Node.js 16+
- MySQL 8.0+
- Git

### 1. 克隆项目

```bash
git clone https://github.com/your-username/货架商品管理系统.git
cd 货架商品管理系统
```

### 2. 数据库设置

1. 创建MySQL数据库：
```sql
CREATE DATABASE goods_system CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

2. 运行初始化脚本：
```bash
mysql -u root -p goods_system < database_init.sql
```

### 3. 后端设置

1. 进入后端目录：
```bash
cd goods
```

2. 复制配置文件：
```bash
cp src/main/resources/application-example.properties src/main/resources/application.properties
```

3. 修改配置文件中的数据库连接信息：
```properties
spring.datasource.username=your_username
spring.datasource.password=your_password
```

4. 运行后端应用：
```bash
./gradlew bootRun
```

后端将在 http://localhost:8084 启动

### 4. 前端设置

1. 进入前端目录：
```bash
cd goods-frontend
```

2. 安装依赖：
```bash
npm install
```

3. 启动开发服务器：
```bash
npm run dev
```

前端将在 http://localhost:5173 启动

### 5. 访问系统

打开浏览器访问 http://localhost:5173

默认管理员账号：
- 用户名：admin
- 密码：admin123

## 📁 项目结构

```
货架商品管理系统/
├── goods/                          # Spring Boot 后端
│   ├── src/main/java/com/shelf/
│   │   ├── controller/             # 控制器层
│   │   ├── service/               # 服务层
│   │   ├── repository/            # 数据访问层
│   │   ├── entity/               # 实体类
│   │   ├── dto/                  # 数据传输对象
│   │   └── config/              # 配置类
│   └── src/main/resources/
│       └── application.properties # 应用配置
├── goods-frontend/                # Vue 3 前端
│   ├── src/
│   │   ├── components/           # Vue 组件
│   │   ├── api/                 # API 接口
│   │   ├── stores/              # 状态管理
│   │   └── types/              # TypeScript 类型
│   └── package.json
├── database_init.sql             # 数据库初始化脚本
└── README.md
```

## 🔧 开发指南

### API 文档

后端提供的主要 API 端点：

- `GET /api/products` - 获取商品列表
- `POST /api/products` - 创建商品
- `PUT /api/products/{id}` - 更新商品
- `DELETE /api/products/{id}` - 删除商品
- `GET /api/inbound` - 获取入库记录
- `POST /api/inbound` - 创建入库记录
- `GET /api/outbound` - 获取出库记录
- `POST /api/outbound` - 创建出库记录

### 数据库结构

主要数据表：
- `products` - 商品信息表
- `inbound_records` - 入库记录表
- `outbound_records` - 出库记录表
- `admins` - 管理员表

## 🚀 部署

### 生产环境部署

1. **后端部署**：
```bash
cd goods
./gradlew build
java -jar build/libs/goods-0.0.1-SNAPSHOT.jar
```

2. **前端部署**：
```bash
cd goods-frontend
npm run build
# 将 dist/ 目录部署到 web 服务器
```

### Docker 部署

(如果需要，可以添加 Dockerfile 和 docker-compose.yml)

## 🤝 贡献

1. Fork 本项目
2. 创建特性分支 (`git checkout -b feature/AmazingFeature`)
3. 提交更改 (`git commit -m 'Add some AmazingFeature'`)
4. 推送到分支 (`git push origin feature/AmazingFeature`)
5. 开启 Pull Request

## 📝 许可证

本项目采用 MIT 许可证 - 查看 [LICENSE](LICENSE) 文件了解详情

## 📞 联系方式

如有问题，请通过以下方式联系：

- 创建 [Issue](https://github.com/your-username/货架商品管理系统/issues)
- 发送邮件至：your-email@example.com

## 🙏 致谢

感谢所有为这个项目做出贡献的开发者！