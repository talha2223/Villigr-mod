package com.aivillager.mixin;

import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayNetworkHandler.class)
public class ChatListenerMixin {

    @Shadow
    public ServerPlayerEntity player;

    @Inject(method = "onChatMessage", at = @At("HEAD"))
    private void onChatMessage(net.minecraft.network.packet.c2s.play.ChatMessageC2SPacket packet, CallbackInfo ci) {
        String message = packet.chatMessage();
        if (com.aivillager.api.ConversationManager.ACTIVE_CONVERSATIONS.containsKey(player.getUuid())) {
            if (message.equalsIgnoreCase("bye") || message.equalsIgnoreCase("alvida") || message.equalsIgnoreCase("band karo")) {
                com.aivillager.api.ConversationManager.ACTIVE_CONVERSATIONS.remove(player.getUuid());
                player.sendMessage(Text.literal("\u00a7e[Villager] \u00a7fAlvida bhai! Phir milenge!"), false);
                return;
            }
            com.aivillager.api.ConversationManager.handlePlayerChat(player, message);
        }
    }
}
