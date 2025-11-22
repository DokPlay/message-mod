package com.example.messagemod;

import com.example.messagemod.config.MessageModConfig;
import com.example.messagemod.db.DatabaseManager;
import com.example.messagemod.network.MessagePayload;
import com.example.messagemod.proto.Message;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.level.ServerPlayer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MessageMod implements ModInitializer {
        public static final String MOD_ID = "message-mod";
        public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

        private static DatabaseManager databaseManager;

        @Override
        public void onInitialize() {
                LOGGER.info("Loading Message Mod");
                MessageModConfig config = MessageModConfig.load();
                databaseManager = new DatabaseManager(config);

                ServerLifecycleEvents.SERVER_STARTING.register(server -> {
                        try {
                                databaseManager.connect();
                        } catch (Exception e) {
                                LOGGER.error("Failed to initialize database connection", e);
                        }
                });

                ServerLifecycleEvents.SERVER_STOPPED.register(server -> {
                        if (databaseManager != null) {
                                databaseManager.close();
                        }
                });

                PayloadTypeRegistry.playC2S().register(MessagePayload.TYPE, MessagePayload.CODEC);

                ServerPlayNetworking.registerGlobalReceiver(MessagePayload.TYPE, this::handleIncomingMessage);
        }

        private void handleIncomingMessage(MessagePayload payload, ServerPlayNetworking.Context context) {
                byte[] data = payload.payload();
                ServerPlayer player = context.player();
                try {
                        Message message = Message.parseFrom(data);
                        context.server().execute(() -> {
                                try {
                                        databaseManager.saveMessage(player.getUUID(), message.getText());
                                        LOGGER.info("Stored message from {}", player.getGameProfile().getName());
                                } catch (Exception e) {
                                        LOGGER.error("Failed to store message for player {}", player.getGameProfile().getName(), e);
                                }
                        });
                } catch (Exception e) {
                        LOGGER.error("Failed to decode message payload", e);
                }
        }

        public static DatabaseManager getDatabaseManager() {
                return databaseManager;
        }
}
