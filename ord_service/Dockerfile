FROM eclipse-temurin:17-jdk
WORKDIR /app
COPY target/ord_service-1.0-SNAPSHOT.jar ord_service.jar
ENTRYPOINT ["java", "-jar", "ord_service.jar"]
