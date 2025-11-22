package com.example.messagemod.client.network;

import com.example.messagemod.MessageMod;
import com.example.messagemod.proto.MessageProtos;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;

public final class ClientMessageSender {
        private ClientMessageSender() {
        }

        public static void register() {
                // No-op placeholder to mirror server registration
        }

        public static void send(String text) {
                Minecraft client = Minecraft.getInstance();
                if (client.getConnection() == null || client.player == null) {
                        return;
                }

                MessageProtos.Message message = MessageProtos.Message.newBuilder().setText(text).build();
                byte[] payload = message.toByteArray();

                FriendlyByteBuf buffer = PacketByteBufs.create();
                buffer.writeByteArray(payload);
                ClientPlayNetworking.send(MessageMod.MESSAGE_PACKET_ID, buffer);
        }
}
