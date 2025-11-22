package com.example.messagemod;

import com.example.messagemod.config.MessageModConfig;
import com.example.messagemod.db.DatabaseManager;
import com.example.messagemod.proto.MessageProtos;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MessageMod implements ModInitializer {
        public static final String MOD_ID = "message-mod";
        public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
        public static final ResourceLocation MESSAGE_PACKET_ID = new ResourceLocation(MOD_ID, "message");

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

                ServerPlayNetworking.registerGlobalReceiver(MESSAGE_PACKET_ID, this::handleIncomingMessage);
        }

        private void handleIncomingMessage(MinecraftServer server, ServerPlayer player, ServerGamePacketListenerImpl handler,
                                           FriendlyByteBuf buf, PacketSender responseSender) {
                byte[] payload = buf.readByteArray();
                try {
                        MessageProtos.Message message = MessageProtos.Message.parseFrom(payload);
                        server.execute(() -> {
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
