package com.example.messagemod.client.network;

import com.example.messagemod.network.MessagePayload;
import com.example.messagemod.proto.Message;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.minecraft.client.Minecraft;

public final class ClientMessageSender {
        private ClientMessageSender() {
        }

        public static void register() {
                PayloadTypeRegistry.playC2S().register(MessagePayload.TYPE, MessagePayload.CODEC);
        }

        public static void send(String text) {
                Minecraft client = Minecraft.getInstance();
                if (client.getConnection() == null || client.player == null) {
                        return;
                }

                Message message = Message.newBuilder().setText(text).build();
                byte[] payload = message.toByteArray();

                ClientPlayNetworking.send(new MessagePayload(payload));
        }
}
