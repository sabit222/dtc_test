# Используем образ с JDK 17
FROM eclipse-temurin:17-jdk

# Устанавливаем рабочую директорию внутри контейнера
WORKDIR /app

# Копируем JAR-файл в контейнер
COPY target/user_service-1.0-SNAPSHOT.jar user_service.jar

# Запускаем приложение при старте контейнера
ENTRYPOINT ["java", "-jar", "user_service.jar"]