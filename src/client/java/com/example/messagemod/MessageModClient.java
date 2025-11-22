package com.example.messagemod;

import com.example.messagemod.client.gui.MessageScreen;
import com.example.messagemod.client.network.ClientMessageSender;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;

public class MessageModClient implements ClientModInitializer {
        private KeyMapping openScreenKeyBinding;

        @Override
        public void onInitializeClient() {
                ClientMessageSender.register();

                openScreenKeyBinding = KeyBindingHelper.registerKeyBinding(new KeyMapping(
                        "key.message-mod.open",
                        GLFW.GLFW_KEY_M,
                        "key.categories.multiplayer"
                ));

                ClientTickEvents.END_CLIENT_TICK.register(client -> {
                        while (openScreenKeyBinding.consumeClick()) {
                                if (client.player != null && client.gameMode != null && client.level != null) {
                                        client.setScreen(new MessageScreen(Component.translatable("screen.message-mod.title")));
                                }
                        }
                });
        }
}
