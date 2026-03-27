package com.aivillager.mixin;

import com.aivillager.AiVillagerMod;
import com.aivillager.api.GeminiApiHandler;
import com.aivillager.api.ActionParser;
import com.aivillager.audio.TtsBridge;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.LiteralText;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.concurrent.CompletableFuture;

/**
 * Mixin that intercepts player interaction with villagers.
 * When a player right-clicks a villager, it triggers the AI conversation.
 */
@Mixin(VillagerEntity.class)
public class VillagerInteractionMixin {

    @Inject(method = "interactMob", at = @At("HEAD"), cancellable = true)
    private void onInteract(PlayerEntity player, Hand hand, CallbackInfoReturnable<ActionResult> cir) {
        VillagerEntity villager = (VillagerEntity) (Object) this;

        // Only handle on server side and main hand
        if (villager.world.isClient || hand != Hand.MAIN_HAND) {
            return;
        }

        // Sneak + right-click = vanilla trading (so players can still trade)
        if (player.isSneaking()) {
            return;
        }

        final String villagerName = villager.hasCustomName()
                ? villager.getName().getString()
                : "Ghaib Villager";

        if (player instanceof ServerPlayerEntity) {
            final ServerPlayerEntity serverPlayer = (ServerPlayerEntity) player;

            // Send prompt message to player's chat
            serverPlayer.sendMessage(
                    new LiteralText("\u00a7e[" + villagerName + "] \u00a7fBhai, kya haal hai? Chat mein kuch bolo!"),
                    false
            );

            // For demonstration, trigger a sample conversation
            final String playerMessage = "Kya haal hai?";

            // Call Gemini API asynchronously
            CompletableFuture.runAsync(() -> {
                try {
                    String response = GeminiApiHandler.generateResponse(villagerName, playerMessage);

                    if (response != null) {
                        final String cleanResponse = ActionParser.extractMessage(response);
                        final String action = ActionParser.extractAction(response);

                        // Execute on main thread
                        serverPlayer.server.execute(() -> {
                            serverPlayer.sendMessage(
                                    new LiteralText("\u00a7e[" + villagerName + "] \u00a7f" + cleanResponse),
                                    false
                            );

                            if (action != null) {
                                executeAction(villager, serverPlayer, action, villagerName);
                            }
                        });

                        // Play villager sound
                        TtsBridge.speak(cleanResponse, villager);
                    }
                } catch (Exception e) {
                    serverPlayer.server.execute(() -> {
                        serverPlayer.sendMessage(
                                new LiteralText("\u00a7c[" + villagerName + "] \u00a74Arrey yaar, kuch ghalti ho gayi! (API Error)"),
                                false
                        );
                    });
                }
            });
        }

        // Cancel the default behavior (no trade GUI)
        cir.setReturnValue(ActionResult.CONSUME);
    }

    /**
     * Execute the parsed action from the AI response.
     */
    private void executeAction(VillagerEntity villager, ServerPlayerEntity player, String action, String name) {
        switch (action.toUpperCase()) {
            case "FOLLOW":
                player.sendMessage(
                        new LiteralText("\u00a7a[" + name + "] \u00a72Chalo, main tumhare peechay aata hoon!"),
                        false
                );
                break;

            case "PROTECT":
                player.sendMessage(
                        new LiteralText("\u00a7a[" + name + "] \u00a72Fikar na karo, main tumhe bachaunga!"),
                        false
                );
                // Boost villager's combat stats
                try {
                    villager.getAttributes()
                            .getCustomInstance(EntityAttributes.GENERIC_MAX_HEALTH)
                            .setBaseValue(40.0);
                    villager.getAttributes()
                            .getCustomInstance(EntityAttributes.GENERIC_ATTACK_DAMAGE)
                            .setBaseValue(8.0);
                    villager.getAttributes()
                            .getCustomInstance(EntityAttributes.GENERIC_ARMOR)
                            .setBaseValue(10.0);
                    villager.heal(villager.getMaxHealth());
                } catch (Exception e) {
                    AiVillagerMod.LOGGER.error("Failed to boost villager stats: " + e.getMessage());
                }
                break;

            case "TRADE":
                player.sendMessage(
                        new LiteralText("\u00a7a[" + name + "] \u00a72Aao, sauda karte hain!"),
                        false
                );
                break;

            default:
                break;
        }
    }
}
