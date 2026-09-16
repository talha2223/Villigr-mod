package com.aivillager.goal;

import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.util.math.BlockPos;

import java.util.EnumSet;

public class GuardGoal extends Goal {
    private final VillagerEntity villager;
    private final BlockPos guardPos;

    public GuardGoal(VillagerEntity villager, BlockPos guardPos) {
        this.villager = villager;
        this.guardPos = guardPos;
        this.setControls(EnumSet.of(Goal.Control.MOVE, Goal.Control.LOOK));
    }

    @Override
    public boolean canStart() { return true; }

    @Override
    public void tick() {
        double dist = villager.squaredDistanceTo(guardPos.toCenterPos());
        if (dist > 16.0) {
            villager.getNavigation().startMovingTo(guardPos.getX(), guardPos.getY(), guardPos.getZ(), 1.0);
        } else if (dist < 4.0) {
            villager.getNavigation().stop();
        }
    }
}
