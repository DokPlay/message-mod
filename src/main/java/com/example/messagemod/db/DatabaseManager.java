package com.example.messagemod.db;

import com.example.messagemod.MessageMod;
import com.example.messagemod.config.MessageModConfig;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;

public class DatabaseManager {
    private final MessageModConfig config;
    private EntityManagerFactory entityManagerFactory;
    private MessageRepository messageRepository;

    public DatabaseManager(MessageModConfig config) {
        this.config = config;
    }

    public void connect() {
        if (entityManagerFactory != null && entityManagerFactory.isOpen()) {
            return;
        }

        Map<String, Object> overrides = new HashMap<>();
        overrides.put("jakarta.persistence.jdbc.url", config.getJdbcUrl());
        overrides.put("jakarta.persistence.jdbc.user", config.getUsername());
        overrides.put("jakarta.persistence.jdbc.password", config.getPassword());
        overrides.put("hibernate.hikari.maximumPoolSize", config.getMaxPoolSize());
        overrides.put("hibernate.hikari.minimumIdle", Math.max(1, Math.min(2, config.getMaxPoolSize())));
        overrides.put("hibernate.hikari.poolName", "message-mod-pool");
        overrides.put("hibernate.show_sql", "false");
        overrides.put("hibernate.format_sql", "false");

        entityManagerFactory = Persistence.createEntityManagerFactory("message-mod-unit", overrides);
        messageRepository = new MessageRepository(entityManagerFactory);
        MessageMod.LOGGER.info("Connected to Postgres at {}", config.getJdbcUrl());
    }

    public void close() {
        if (entityManagerFactory != null && entityManagerFactory.isOpen()) {
            entityManagerFactory.close();
            MessageMod.LOGGER.info("Closed Postgres connection pool");
        }
    }

    public void saveMessage(UUID playerId, String text) {
        if (messageRepository == null) {
            throw new IllegalStateException("Database not initialized");
        }
        messageRepository.save(playerId, text);
    }

    public Optional<MessageRepository> getRepository() {
        return Optional.ofNullable(messageRepository);
    }
}
