# 使用OpenJDK 17作为基础镜像
FROM openjdk:17-jdk-slim

# 设置工作目录
WORKDIR /app

# 复制Gradle配置文件
COPY build.gradle settings.gradle gradlew ./
COPY gradle gradle

# 给gradlew执行权限
RUN chmod +x ./gradlew

# 复制源代码
COPY src src

# 构建应用（跳过测试）
RUN ./gradlew build --no-daemon -x test

# 暴露端口
EXPOSE 8084

# 运行应用
CMD ["java", "-Xmx512m", "-Xms256m", "-Dspring.profiles.active=prod", "-jar", "build/libs/demo-0.0.1-SNAPSHOT.jar"]
