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

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CompletableFuture;

/**
 * Mixin that intercepts player interaction with villagers.
 * When a player right-clicks a villager, it sets that villager as "active"
 * and the player's next chat messages go to the AI.
 */
@Mixin(VillagerEntity.class)
public class VillagerInteractionMixin {

    // Track which player is talking to which villager
    public static final Map<UUID, VillagerEntity> ACTIVE_CONVERSATIONS = new ConcurrentHashMap<>();

    @Inject(method = "interactMob", at = @At("HEAD"), cancellable = true)
    private void onInteract(PlayerEntity player, Hand hand, CallbackInfoReturnable<ActionResult> cir) {
        VillagerEntity villager = (VillagerEntity) (Object) this;

        // Only handle on server side and main hand
        if (villager.world.isClient || hand != Hand.MAIN_HAND) {
            return;
        }

        // Sneak + right-click = vanilla trading
        if (player.isSneaking()) {
            return;
        }

        final String villagerName = villager.hasCustomName()
                ? villager.getName().getString()
                : "Ghaib Villager";

        if (player instanceof ServerPlayerEntity) {
            ServerPlayerEntity serverPlayer = (ServerPlayerEntity) player;

            // Set this villager as the active conversation for this player
            ACTIVE_CONVERSATIONS.put(player.getUuid(), villager);

            // Send prompt message
            serverPlayer.sendMessage(
                    new LiteralText("\u00a7e[" + villagerName + "] \u00a7fBhai, kya haal hai? Chat mein kuch bolo!"),
                    false
            );

            // Play villager ambient sound
            villager.world.playSound(
                    null,
                    villager.getBlockPos(),
                    net.minecraft.sound.SoundEvents.ENTITY_VILLAGER_AMBIENT,
                    net.minecraft.sound.SoundCategory.NEUTRAL,
                    1.0F,
                    0.8F + (float)(Math.random() * 0.4)
            );
        }

        // Cancel vanilla behavior
        cir.setReturnValue(ActionResult.CONSUME);
    }

    /**
     * Called from ChatListenerMixin when a player sends a chat message
     * while in an active conversation with a villager.
     */
    public static void handlePlayerChat(ServerPlayerEntity player, String message) {
        VillagerEntity villager = ACTIVE_CONVERSATIONS.get(player.getUuid());

        if (villager == null || !villager.isAlive()) {
            ACTIVE_CONVERSATIONS.remove(player.getUuid());
            return;
        }

        final String villagerName = villager.hasCustomName()
                ? villager.getName().getString()
                : "Ghaib Villager";

        // Call Gemini API asynchronously
        CompletableFuture.runAsync(() -> {
            try {
                String response = GeminiApiHandler.generateResponse(villagerName, message);

                if (response != null) {
                    final String cleanResponse = ActionParser.extractMessage(response);
                    final String action = ActionParser.extractAction(response);

                    // Execute on main thread
                    player.server.execute(() -> {
                        player.sendMessage(
                                new LiteralText("\u00a7e[" + villagerName + "] \u00a7f" + cleanResponse),
                                false
                        );

                        // Play sound
                        villager.world.playSound(
                                null,
                                villager.getBlockPos(),
                                net.minecraft.sound.SoundEvents.ENTITY_VILLAGER_AMBIENT,
                                net.minecraft.sound.SoundCategory.NEUTRAL,
                                1.0F,
                                0.9F + (float)(Math.random() * 0.2)
                        );

                        if (action != null) {
                            executeAction(villager, player, action, villagerName);
                        }
                    });

                    // TTS
                    TtsBridge.speak(cleanResponse, villager);
                }
            } catch (Exception e) {
                player.server.execute(() -> {
                    player.sendMessage(
                            new LiteralText("\u00a7c[" + villagerName + "] \u00a74Arrey yaar, kuch ghalti ho gayi!"),
                            false
                    );
                });
                AiVillagerMod.LOGGER.error("[AI Villager] Gemini error: " + e.getMessage());
            }
        });
    }

    /**
     * Execute the parsed action from the AI response.
     */
    private static void executeAction(VillagerEntity villager, ServerPlayerEntity player, String action, String name) {
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

            case "FLEE":
                player.sendMessage(
                        new LiteralText("\u00a7c[" + name + "] \u00a74Bachao bachao! Bhagooo!"),
                        false
                );
                break;

            default:
                break;
        }
    }
}
