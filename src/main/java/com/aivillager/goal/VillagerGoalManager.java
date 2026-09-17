package com.aivillager.goal;

import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.passive.VillagerEntity;

import java.util.HashMap;
import java.util.Map;

public class VillagerGoalManager {
    private static final Map<VillagerEntity, Goal> currentActiveGoals = new HashMap<>();

    public static void setActiveGoal(VillagerEntity villager, Goal newGoal) {
        com.aivillager.mixin.VillagerGoalAccessor accessor = (com.aivillager.mixin.VillagerGoalAccessor) villager;
        if (currentActiveGoals.containsKey(villager)) {
            Goal oldGoal = currentActiveGoals.get(villager);
            accessor.getGoalSelector().remove(oldGoal);
        }

        if (newGoal != null) {
            accessor.getGoalSelector().add(0, newGoal);
            currentActiveGoals.put(villager, newGoal);
        } else {
            currentActiveGoals.remove(villager);
        }

        accessor.getGoalSelector().add(10, new ForceLoadChunkGoal(villager));
        accessor.getGoalSelector().add(11, new ManageInventoryGoal(villager));
    }

    public static void clearAllGoals(VillagerEntity villager) {
        setActiveGoal(villager, null);
    }
}
