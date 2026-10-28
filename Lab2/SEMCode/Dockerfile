
FROM eclipse-temurin:17-jdk

WORKDIR /app

COPY target/seMethods.jar app.jar

CMD ["java", "-jar", "app.jar", "db:3306", "10000"]
