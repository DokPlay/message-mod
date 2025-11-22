package com.example.messagemod.config;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

import net.fabricmc.loader.api.FabricLoader;

public class MessageModConfig {
    private static final String CONFIG_FILE_NAME = "message-mod.properties";

    private final String jdbcUrl;
    private final String username;
    private final String password;
    private final int maxPoolSize;

    public MessageModConfig(String jdbcUrl, String username, String password, int maxPoolSize) {
        this.jdbcUrl = jdbcUrl;
        this.username = username;
        this.password = password;
        this.maxPoolSize = maxPoolSize;
    }

    public String getJdbcUrl() {
        return jdbcUrl;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public int getMaxPoolSize() {
        return maxPoolSize;
    }

    public static MessageModConfig load() {
        Path configDir = FabricLoader.getInstance().getConfigDir();
        Path configPath = configDir.resolve(CONFIG_FILE_NAME);
        Properties props = new Properties();

        String defaultUrl = System.getenv().getOrDefault("MESSAGE_MOD_DB_URL", "jdbc:postgresql://localhost:5432/minecraft");
        String defaultUser = System.getenv().getOrDefault("MESSAGE_MOD_DB_USER", "postgres");
        String defaultPassword = System.getenv().getOrDefault("MESSAGE_MOD_DB_PASSWORD", "postgres");
        String defaultPoolSize = System.getenv().getOrDefault("MESSAGE_MOD_DB_POOL", "5");

        props.setProperty("jdbcUrl", defaultUrl);
        props.setProperty("username", defaultUser);
        props.setProperty("password", defaultPassword);
        props.setProperty("maxPoolSize", defaultPoolSize);

        if (Files.exists(configPath)) {
            try (InputStream inputStream = Files.newInputStream(configPath)) {
                props.load(inputStream);
            } catch (IOException e) {
                // If we fail to load, fall back to defaults but keep going
            }
        } else {
            try {
                Files.createDirectories(configDir);
                try (OutputStream outputStream = Files.newOutputStream(configPath)) {
                    props.store(outputStream, "Message Mod database configuration");
                }
            } catch (IOException e) {
                // Ignore write issues; defaults are already in props
            }
        }

        String jdbcUrl = props.getProperty("jdbcUrl", defaultUrl);
        String username = props.getProperty("username", defaultUser);
        String password = props.getProperty("password", defaultPassword);
        int poolSize;
        try {
            poolSize = Integer.parseInt(props.getProperty("maxPoolSize", defaultPoolSize));
        } catch (NumberFormatException e) {
            poolSize = Integer.parseInt(defaultPoolSize);
        }

        return new MessageModConfig(jdbcUrl, username, password, poolSize);
    }
}
