# Giai đoạn 1: Build ứng dụng (Đặt tên là builder)
FROM maven:3.8.5-openjdk-17 AS builder
WORKDIR /app

COPY momopayment-lib ./momopayment-lib
RUN cd momopayment-lib && mvn clean install -DskipTests -Dgpg.skip

COPY pom.xml .
RUN mvn dependency:go-offline

COPY src ./src
RUN mvn package -DskipTests

# Giai đoạn 2: Chạy ứng dụng
FROM eclipse-temurin:17-jre-focal
WORKDIR /app

COPY --from=builder /app/target/*.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
