package com.aivillager.api;

import com.mojang.brigadier.context.CommandContext;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;

public class CommandRegistry {
    public static void register() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            dispatcher.register(CommandManager.literal("ai")
                .then(CommandManager.literal("status").executes(CommandRegistry::executeStatus))
                .then(CommandManager.literal("stop").executes(CommandRegistry::executeStop))
            );
        });
    }

    private static int executeStatus(CommandContext<ServerCommandSource> context) {
        context.getSource().sendFeedback(() -> Text.literal("\u00a7a[AI Villager] System Online. Connected to Gemini API."), false);
        return 1;
    }

    private static int executeStop(CommandContext<ServerCommandSource> context) {
        net.minecraft.server.world.ServerWorld world = context.getSource().getWorld();
        int count = 0;
        for (net.minecraft.entity.Entity entity : world.iterateEntities()) {
            if (entity instanceof net.minecraft.entity.passive.VillagerEntity) {
                com.aivillager.goal.VillagerGoalManager.clearAllGoals((net.minecraft.entity.passive.VillagerEntity) entity);
                count++;
            }
        }
        final int finalCount = count;
        context.getSource().sendFeedback(() -> Text.literal("\u00a7c[AI Villager] Stopped AI tasks for " + finalCount + " villagers."), false);
        return 1;
    }
}
