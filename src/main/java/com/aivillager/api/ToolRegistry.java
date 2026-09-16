package com.aivillager.api;

import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.world.ServerWorld;
import com.aivillager.AiVillagerMod;
import com.aivillager.goal.*;

public class ToolRegistry {
    public static void executeTool(String task, String target, java.util.List<net.minecraft.util.math.BlockPos> plan, VillagerEntity villager, PlayerEntity player) {
        if (task == null || task.equalsIgnoreCase("none")) {
            VillagerGoalManager.clearAllGoals(villager);
            return;
        }

        switch (task.toLowerCase()) {
            case "follow":
                VillagerGoalManager.setActiveGoal(villager, new FollowPlayerGoal(villager, player, 1.2));
                break;
            case "protect":
                VillagerGoalManager.setActiveGoal(villager, new ProtectPlayerGoal(villager, player));
                break;
            case "mine_resource":
                if (target != null && !target.isEmpty()) {
                    VillagerGoalManager.setActiveGoal(villager, new MineResourceGoal(villager, target));
                }
                break;
            case "gather_item":
                if (target != null && !target.isEmpty()) {
                    VillagerGoalManager.setActiveGoal(villager, new GatherItemGoal(villager, target));
                }
                break;
            case "guard":
                VillagerGoalManager.setActiveGoal(villager, new GuardGoal(villager, villager.getBlockPos()));
                break;
            case "flee":
                VillagerGoalManager.setActiveGoal(villager, new FleeGoal(villager, 16.0F, 1.2, 1.5));
                break;
            case "build":
                if (plan != null && !plan.isEmpty()) {
                    net.minecraft.block.BlockState blockState = net.minecraft.block.Blocks.OAK_PLANKS.getDefaultState();
                    if (target != null && !target.isEmpty()) {
                        net.minecraft.util.Identifier id = net.minecraft.util.Identifier.tryParse(target);
                        if (id != null && net.minecraft.registry.Registries.BLOCK.containsId(id)) {
                            blockState = net.minecraft.registry.Registries.BLOCK.get(id).getDefaultState();
                        }
                    }
                    VillagerGoalManager.setActiveGoal(villager, new BuildStructureGoal(villager, plan, blockState));
                }
                break;
        }
    }
}
