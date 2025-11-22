package com.example.messagemod.network;

import com.example.messagemod.MessageMod;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record MessagePayload(byte[] payload) implements CustomPacketPayload {
        public static final CustomPacketPayload.Type<MessagePayload> TYPE =
                        new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(MessageMod.MOD_ID, "message"));

        public static final StreamCodec<FriendlyByteBuf, MessagePayload> CODEC =
                        CustomPacketPayload.codec(MessagePayload::write, MessagePayload::new);

        public MessagePayload(FriendlyByteBuf buf) {
                this(buf.readByteArray());
        }

        private void write(FriendlyByteBuf buf) {
                buf.writeByteArray(payload);
        }

        @Override
        public Type<? extends CustomPacketPayload> type() {
                return TYPE;
        }
}
