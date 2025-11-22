package com.example.messagemod.client.gui;

import com.example.messagemod.client.network.ClientMessageSender;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class MessageScreen extends Screen {
        private EditBox messageField;
        private Button sendButton;

        public MessageScreen(Component title) {
                super(title);
        }

        @Override
        protected void init() {
                int centerX = this.width / 2;
                int centerY = this.height / 2;

                messageField = new EditBox(this.font, centerX - 100, centerY - 20, 200, 20, Component.translatable("screen.message-mod.input"));
                messageField.setMaxLength(256);
                addRenderableWidget(messageField);

                sendButton = Button.builder(Component.translatable("screen.message-mod.send"), button -> {
                        String text = messageField.getValue().trim();
                        if (!text.isEmpty()) {
                                ClientMessageSender.send(text);
                                this.minecraft.setScreen(null);
                        }
                }).bounds(centerX - 40, centerY + 10, 80, 20).build();

                addRenderableWidget(sendButton);
                setInitialFocus(messageField);
        }

        @Override
        public void tick() {
                super.tick();
                if (messageField != null) {
                        messageField.tick();
                }
        }

        @Override
        public void resize(net.minecraft.client.Minecraft minecraft, int width, int height) {
                String text = messageField != null ? messageField.getValue() : "";
                super.resize(minecraft, width, height);
                if (messageField != null) {
                        messageField.setValue(text);
                }
        }

        @Override
        public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
                if (this.messageField.keyPressed(keyCode, scanCode, modifiers) || this.messageField.canConsumeInput()) {
                        return true;
                }
                return super.keyPressed(keyCode, scanCode, modifiers);
        }

        @Override
        public boolean charTyped(char chr, int modifiers) {
                if (messageField.charTyped(chr, modifiers)) {
                        return true;
                }
                return super.charTyped(chr, modifiers);
        }
}
