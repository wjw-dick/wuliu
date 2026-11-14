# 物流货运 SaaS 后端镜像（多阶段构建）
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml .
COPY wuliu-common/pom.xml wuliu-common/
COPY wuliu-domain/pom.xml wuliu-domain/
COPY wuliu-service/pom.xml wuliu-service/
COPY wuliu-api/pom.xml wuliu-api/
RUN mvn -q -DskipTests dependency:go-offline || true
COPY . .
RUN mvn -q -DskipTests package -DskipTests

FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=build /app/wuliu-api/target/wuliu-api-*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
