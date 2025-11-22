# Message Mod

Fabric mod for Minecraft 1.21.8 that lets players send short text messages from a client screen to the server, where they are persisted to PostgreSQL through Hibernate/JPA. The project is intentionally small to demonstrate how to stitch together Fabric networking, Protobuf, and a database layer.

[Читать на русском](README.ru.md)

## What happens end-to-end
1. The player presses **M** in-game to open the Message Sender screen.
2. They enter up to 256 characters and click **Send**.
3. The client serializes the text into a Protobuf `Message` payload and sends it over a Fabric custom payload channel `message-mod:message`.
4. The server decodes the payload, captures the sender's UUID, and hands the data to `DatabaseManager.saveMessage(UUID, String)`.
5. Hibernate writes a row to the `messages` table containing the UUID and text.

## Components
- **Client UI and networking**: A keybind registers the screen and dispatches the protobuf payload when the user clicks send.
- **Protocol**: The generated Protobuf class defines a single `text` field. Messages are bounded to 256 characters to prevent oversized payloads.
- **Server handler**: Listens for `message-mod:message` payloads, validates the data, and invokes the database layer.
- **Database layer**: `DatabaseManager` configures Hibernate's `EntityManagerFactory` using settings from the config file or environment variables, and `MessageRepository` wraps persistence operations.
- **Entity mapping**: `com.example.messagemod.db.MessageEntity` maps to the `messages` table with columns `id` (BIGSERIAL/identity), `uuid`, and `text`.

## Configuration
A `message-mod.properties` file lives in the Fabric config directory. Each setting can also be overridden by an environment variable, which is useful for containerized or CI setups.

| Property key | Env override | Default | Purpose |
| --- | --- | --- | --- |
| `jdbcUrl` | `MESSAGE_MOD_DB_URL` | `jdbc:postgresql://localhost:5432/minecraft` | JDBC connection string |
| `username` | `MESSAGE_MOD_DB_USER` | `postgres` | Database user |
| `password` | `MESSAGE_MOD_DB_PASSWORD` | `postgres` | Database password |
| `maxPoolSize` | `MESSAGE_MOD_DB_POOL` | `5` | HikariCP connection pool size |

Example config file:
```properties
jdbcUrl=jdbc:postgresql://localhost:5432/minecraft
username=postgres
password=postgres
maxPoolSize=5
```

## Database prerequisites
Create the target table before running the mod:
```sql
CREATE TABLE messages (
  id   BIGSERIAL PRIMARY KEY,
  uuid UUID NOT NULL,
  text VARCHAR(256) NOT NULL
);
```
If you see `Failed to initialize pool: Connection refused` in the logs, verify Postgres is running, the host/port matches `jdbcUrl`, and credentials are correct.

### Local PostgreSQL via Docker Compose
If you want a quick local database, the included `docker-compose.yml` starts Postgres 16 on `localhost:5432` with database `minecraft` and credentials `postgres` / `postgres`.
```bash
docker compose up -d   # start
# ... run the mod ...
docker compose down    # stop and remove
```

## Building
Run the Gradle wrapper (requires Java 21+):
```bash
./gradlew build
```
Artifacts will appear in `build/libs/`. Copy the mod JAR into your Fabric `mods` folder for 1.21.8.

## Running in Minecraft
1. Start the server with the mod installed and configured.
2. Join with a client that also has the mod.
3. Press **M**, enter a message, and send it.
4. Confirm a new row appears in the `messages` table.

## Troubleshooting
- **Nothing happens when sending**: Ensure both client and server have the mod so the custom payload channel is registered.
- **Database connection errors**: Check the JDBC URL and credentials; confirm Postgres is reachable from the game host.
- **Unicode/encoding issues**: Protobuf uses UTF-8 strings; make sure your database collation supports the characters you send.
