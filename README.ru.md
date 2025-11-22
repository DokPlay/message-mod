# Message Mod

Fabric мод для Minecraft 1.21.8, позволяющий отправлять короткие текстовые сообщения с клиентского экрана на сервер, где они сохраняются в PostgreSQL через Hibernate/JPA. Проект небольшой и служит примером интеграции Fabric, Protobuf и базы данных.

## Как все работает
1. Игрок нажимает **M** в игре и открывает экран отправки сообщения.
2. Вводит до 256 символов и нажимает **Send**.
3. Клиент сериализует текст в protobuf `Message` и отправляет через пользовательский канал Fabric `message-mod:message`.
4. Сервер декодирует полезную нагрузку, фиксирует UUID игрока и вызывает `DatabaseManager.saveMessage(UUID, String)`.
5. Hibernate записывает строку в таблицу `messages` с UUID игрока и текстом сообщения.

## Основные части
- **Клиентский UI и сеть**: горячая клавиша открывает экран и отправляет protobuf-пакет при нажатии кнопки.
- **Протокол**: сгенерированный класс Protobuf содержит единственное поле `text`; длина ограничена 256 символами.
- **Серверный обработчик**: слушает канал `message-mod:message`, валидирует данные и передает их в базу.
- **База данных**: `DatabaseManager` настраивает `EntityManagerFactory` по конфигу или переменным окружения, а `MessageRepository` выполняет сохранение.
- **Сущность**: `com.example.messagemod.db.MessageEntity` отображается на таблицу `messages` с колонками `id` (BIGSERIAL/identity), `uuid` и `text`.

## Конфигурация
Файл `message-mod.properties` хранится в каталоге Fabric config. Любой параметр можно переопределить переменной окружения — удобно для Docker или CI.

| Ключ свойства | Переменная окружения | Значение по умолчанию | Назначение |
| --- | --- | --- | --- |
| `jdbcUrl` | `MESSAGE_MOD_DB_URL` | `jdbc:postgresql://localhost:5432/minecraft` | Строка подключения JDBC |
| `username` | `MESSAGE_MOD_DB_USER` | `postgres` | Пользователь БД |
| `password` | `MESSAGE_MOD_DB_PASSWORD` | `postgres` | Пароль БД |
| `maxPoolSize` | `MESSAGE_MOD_DB_POOL` | `5` | Размер пула HikariCP |

Пример файла:
```properties
jdbcUrl=jdbc:postgresql://localhost:5432/minecraft
username=postgres
password=postgres
maxPoolSize=5
```

## Подготовка базы
Создайте таблицу перед запуском мода:
```sql
CREATE TABLE messages (
  id   BIGSERIAL PRIMARY KEY,
  uuid UUID NOT NULL,
  text VARCHAR(256) NOT NULL
);
```
Если в логах появляется `Failed to initialize pool: Connection refused`, убедитесь, что Postgres запущен и хост/порт совпадает с `jdbcUrl`, а учетные данные корректны.

### Локальный Postgres через Docker Compose
Файл `docker-compose.yml` поднимает Postgres 16 на `localhost:5432` с базой `minecraft` и логином/паролем `postgres` / `postgres`.
```bash
docker compose up -d   # запуск
# ... используйте мод ...
docker compose down    # остановка и удаление
```

## Сборка
Запустите Gradle wrapper (нужна Java 21+):
```bash
./gradlew build
```
Собранные JAR появятся в `build/libs/`. Скопируйте файл в папку `mods` Fabric 1.21.8.

## Запуск в Minecraft
1. Запустите сервер с установленным и настроенным модом.
2. Подключитесь клиентом, на котором мод также установлен.
3. Нажмите **M**, введите сообщение и отправьте его.
4. Проверьте, что в таблице `messages` появилась новая строка.

## Частые проблемы
- **Сообщение не отправляется**: установите мод и на клиент, и на сервер, чтобы канал полезной нагрузки был зарегистрирован.
- **Ошибки подключения к базе**: проверьте JDBC URL и учетные данные; убедитесь, что Postgres доступен с машины, где запущена игра.
- **Проблемы с кодировкой**: Protobuf использует UTF-8; убедитесь, что колляция базы допускает нужные символы.
