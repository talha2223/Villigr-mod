package com.aivillager.mixin;

import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Mixin that captures player chat messages.
 * When a player is in an active conversation with a villager,
 * their chat messages are sent to the AI.
 */
@Mixin(ServerPlayNetworkHandler.class)
public class ChatListenerMixin {

    @Shadow
    public ServerPlayerEntity player;

    /**
     * method_31286 is the Yarn intermediary name for the method
     * that processes filtered chat messages in ServerPlayNetworkHandler.
     * It receives the chat string after Mojang's text filtering.
     */
    @Inject(method = "method_31286", at = @At("HEAD"))
    private void onChatMessage(String message, CallbackInfo ci) {
        // Check if player has an active villager conversation
        if (VillagerInteractionMixin.ACTIVE_CONVERSATIONS.containsKey(player.getUuid())) {
            // Check if player wants to exit conversation
            if (message.equalsIgnoreCase("bye") || message.equalsIgnoreCase("alvida") || message.equalsIgnoreCase("band karo")) {
                VillagerInteractionMixin.ACTIVE_CONVERSATIONS.remove(player.getUuid());
                player.sendMessage(
                        Text.of("\u00a7e[Villager] \u00a7fAlvida bhai! Phir milenge!"),
                        false
                );
                return;
            }

            // Send message to AI handler
            VillagerInteractionMixin.handlePlayerChat(player, message);
        }
    }
}
