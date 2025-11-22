# Message Mod

Fabric mod for Minecraft 1.21.8 that sends a protobuf payload from a simple client screen to the server and stores it in Postgres via Hibernate/JPA.

## Features
- Client keybind (**M**) opens a Message Sender screen with a text field and send button.
- Messages are encoded with Google Protobuf (`Message` message with a single `text` field) and sent to the server.
- Server decodes the payload, extracts the player UUID, and persists it to Postgres using Hibernate with a JPA repository abstraction.
- Configuration is driven by a simple properties file in the Fabric config directory, with environment variable fallbacks.

## Database Integration (Hibernate + JPA)
- Dependencies: `hibernate-core`, `hibernate-hikaricp`, `jakarta.persistence-api`, `postgresql`, and `HikariCP` are declared in `build.gradle`.
- Persistence unit: `META-INF/persistence.xml` defines the `message-mod-unit` persistence unit with defaults for a local Postgres instance and the `messages` entity mapping.
- Entity mapping: `com.example.messagemod.db.MessageEntity` maps to the `messages` table with columns `id` (SERIAL/identity), `uuid`, and `text` (varchar 256).
- Repository: `com.example.messagemod.db.MessageRepository` wraps Hibernate's `EntityManager` to persist messages in a transaction-safe way.
- Bootstrap: `DatabaseManager` wires the `EntityManagerFactory`, applies runtime overrides from `MessageModConfig`, and exposes `saveMessage(UUID, String)` for the networking handler.

## Data Flow
1. Press **M** in-game to open the Message Sender screen.
2. Enter text (max 256 chars) and click **Send**.
3. The client builds a `Message` protobuf and sends it with the registered Fabric payload `message-mod:message`.
4. The server receives the payload, decodes it with the generated protobuf class, and calls `DatabaseManager.saveMessage(playerUuid, text)`.
5. Hibernate writes a row into `messages` with the player's UUID and the message text (trimmed to 256 chars).

## Configuration
A `message-mod.properties` file is created/used in the Fabric config directory. Defaults can be overridden by environment variables (`MESSAGE_MOD_DB_URL`, `MESSAGE_MOD_DB_USER`, `MESSAGE_MOD_DB_PASSWORD`, `MESSAGE_MOD_DB_POOL`). Example file:
```
jdbcUrl=jdbc:postgresql://localhost:5432/minecraft
username=postgres
password=postgres
maxPoolSize=5
```

## Building & Running
1. Ensure Postgres is reachable with a `messages` table created via:
   ```sql
   CREATE TABLE messages (
     id   SERIAL PRIMARY KEY,
     uuid UUID NOT NULL,
     text VARCHAR(256) NOT NULL
   );
   ```
2. Build the mod:
   ```bash
   ./gradlew build
   ```
   (Requires Gradle 8.14 wrapper and access to Maven Central.)
3. Copy the produced JAR from `build/libs/` into the Minecraft `mods` folder for Fabric 1.21.8.
4. Start the server/client; on the first run, edit `config/message-mod.properties` if needed.
5. Join the game, press **M**, send a message, and verify the row appears in Postgres.

## Troubleshooting
- If networking seems inactive, ensure both client and server run the mod so the custom payload is registered.
- Database errors are logged to the console; check JDBC URL/credentials and that Postgres is reachable.
