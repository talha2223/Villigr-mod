package com.aivillager.api;

import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CompletableFuture;

public class ConversationManager {
    public static final Map<UUID, VillagerEntity> ACTIVE_CONVERSATIONS = new ConcurrentHashMap<>();

    public static void handlePlayerChat(ServerPlayerEntity player, String message) {
        VillagerEntity villager = ACTIVE_CONVERSATIONS.get(player.getUuid());
        if (villager == null || !villager.isAlive()) {
            ACTIVE_CONVERSATIONS.remove(player.getUuid());
            return;
        }

        final String villagerName = villager.hasCustomName() ? villager.getName().getString() : "Ghaib Villager";
        com.google.gson.JsonObject context = com.aivillager.api.WorldObserver.observe(villager, player);

        CompletableFuture.runAsync(() -> {
            try {
                String response = GeminiApiHandler.generateResponseWithContext(villagerName, message, context);
                if (response != null) {
                    final String cleanResponse = ActionParser.extractMessage(response);
                    final String action = ActionParser.extractAction(response);
                    final String target = ActionParser.extractTarget(response);
                    final java.util.List<net.minecraft.util.math.BlockPos> plan = ActionParser.extractPlan(response, villager.getBlockPos());

                    player.getServer().execute(() -> {
                        player.sendMessage(Text.literal("\u00a7e[" + villagerName + "] \u00a7f" + cleanResponse), false);
                        villager.getWorld().playSound(null, villager.getBlockPos(), net.minecraft.sound.SoundEvents.ENTITY_VILLAGER_AMBIENT, net.minecraft.sound.SoundCategory.NEUTRAL, 1.0F, 0.9F + (float)(Math.random() * 0.2));
                        if (action != null) executeAction(villager, player, action, villagerName, target, plan);
                    });
                }
            } catch (Exception e) {
                player.getServer().execute(() -> {
                    player.sendMessage(Text.literal("\u00a7c[" + villagerName + "] \u00a74Arrey yaar, kuch ghalti ho gayi!"), false);
                });
            }
        });
    }

    public static void executeAction(VillagerEntity villager, ServerPlayerEntity player, String action, String name, String target, java.util.List<net.minecraft.util.math.BlockPos> plan) {
        ToolRegistry.executeTool(action, target, plan, villager, player);
        switch (action.toUpperCase()) {
            case "FOLLOW":
                player.sendMessage(Text.literal("\u00a7a[" + name + "] \u00a72Chalo, main tumhare peechay aata hoon!"), false);
                break;
            case "PROTECT":
                player.sendMessage(Text.literal("\u00a7a[" + name + "] \u00a72Fikar na karo, main tumhe bachaunga!"), false);
                try {
                    villager.getAttributes().getCustomInstance(EntityAttributes.MAX_HEALTH).setBaseValue(40.0);
                    villager.getAttributes().getCustomInstance(EntityAttributes.ATTACK_DAMAGE).setBaseValue(8.0);
                    villager.getAttributes().getCustomInstance(EntityAttributes.ARMOR).setBaseValue(10.0);
                    villager.heal(villager.getMaxHealth());
                } catch (Exception e) {}
                break;
            case "FLEE":
                player.sendMessage(Text.literal("\u00a7c[" + name + "] \u00a74Bachao bachao! Bhagooo!"), false);
                break;
        }
    }
}
