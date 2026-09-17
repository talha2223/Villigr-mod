package com.aivillager.goal;

import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.passive.VillagerEntity;
import com.aivillager.mixin.VillagerGoalAccessor;

import java.util.HashMap;
import java.util.Map;

public class VillagerGoalManager {
    private static final Map<VillagerEntity, Goal> currentActiveGoals = new HashMap<>();

    public static void setActiveGoal(VillagerEntity villager, Goal newGoal) {
        VillagerGoalAccessor accessor = (VillagerGoalAccessor) villager;

        // Remove previous custom AI action goals if any
        if (currentActiveGoals.containsKey(villager)) {
            Goal oldGoal = currentActiveGoals.get(villager);
            if (oldGoal != null) accessor.getGoalSelector().remove(oldGoal);
        }

        if (newGoal != null) {
            accessor.getGoalSelector().add(0, newGoal); // Inject with highest priority
            currentActiveGoals.put(villager, newGoal);
        } else {
            currentActiveGoals.remove(villager);
        }

        // Ensure static maintenance goals are always present at lower priority
        try {
            accessor.getGoalSelector().add(10, new ForceLoadChunkGoal(villager));
            accessor.getGoalSelector().add(11, new ManageInventoryGoal(villager));
        } catch (Exception ignored) {
            // Failsafe in case they are already present and duplicates are rejected,
            // but GoalSelector usually handles duplicate instances safely.
        }
    }

    public static void clearAllGoals(VillagerEntity villager) {
        setActiveGoal(villager, null);
    }
}
