DTC Test Project
📌 Описание проекта
DTC Test Project — это микросервисное приложение, состоящее из двух сервисов:

user_service (порт 8081) — сервис управления пользователями и аутентификации.
ord_service (порт 8082) — сервис управления заказами.
postgres (порт 5432) — база данных PostgreSQL.
Приложение использует JWT-токены для аутентификации и авторизации.

📦 Структура проекта

dtc_testproject/
│── user_service/        # Код микросервиса пользователей
│── ord_service/         # Код микросервиса заказов
│── docker-compose.yml   # Конфигурация Docker Compose
│── README.md            # Документация

🛠 Как запустить проект
1️⃣ Установи Docker и Docker Compose
2️⃣ Запусти контейнеры:
docker-compose up -d --build

3️⃣ Открытые порты:

8081 → user_service
8082 → ord_service
5432 → PostgreSQL

🔐 Аутентификация и авторизация
📌 Регистрация нового пользователя

POST /api/v1/auth/register
🔹 Пример запроса
{
  "firstname": "John",
  "lastname": "Doe",
  "email": "john.doe@example.com",
  "password": "password123",
  "roles": ["ROLE_USER"]
}
🔹 Ответ

{
  "token": "eyJhbGciOiJIUzI1NiIsIn..."
}

📌 Аутентификация (вход в систему)

POST /api/v1/auth/authenticate
🔹 Пример запроса

{
  "email": "john.doe@example.com",
  "password": "password123"
}
🔹 Ответ

{
  "token": "eyJhbGciOiJIUzI1NiIsIn...",
  "refreshToken": "eyJhbGciOiJIUz..."
}
После успешной аутентификации используйте токен в заголовке Authorization: Bearer <JWT_TOKEN>.

📌 Обновление JWT-токена

POST /api/v1/auth/refresh-token
Этот эндпоинт обновляет access_token, если refresh_token еще действителен.

📦 Работа с заказами (ord_service)
📌 Получить список заказов

GET /api/orders
Требуется роль: ROLE_ADMIN
Фильтрация по статусу и цене:
GET /api/orders?status=PENDING&minPrice=50&maxPrice=200

📌 Получить заказ по ID

GET /api/orders/{orderId}
Требуется роль: ROLE_ADMIN или владелец заказа (ROLE_USER).
Пример:
GET /api/orders/1

📌 Создать новый заказ

POST /api/orders?firstname=John
Требуется роль: ROLE_USER
Тело запроса:

{
  "status": "PENDING",
  "totalPrice": 100.00,
  "products": [
    {
      "name": "Apple",
      "price": 90.00,
      "quantity": 1
    }
  ]
}

📌 Обновить заказ

PUT /api/orders/{orderId}?firstname=John
Требуется роль: ROLE_USER или ROLE_ADMIN
Тело запроса:

{
  "status": "CONFIRMED",
  "totalPrice": 120.00,
  "products": [
    {
      "name": "Apple",
      "price": 90.00,
      "quantity": 1
    },
    {
      "name": "Banana",
      "price": 30.00,
      "quantity": 2
    }
  ]
}

📌 Удалить заказ

DELETE /api/orders/{orderId}
Требуется роль: ROLE_ADMIN
Пример:

DELETE /api/orders/1

🔎 Как проверить логины контейнеров
Посмотреть логи ord_service:

docker logs ord_service -f <>

Посмотреть логи user_service:

docker logs user_service -f

🛑 Остановка контейнеров

docker-compose down
Чтобы удалить все данные, добавьте -v:
docker-compose down -v
