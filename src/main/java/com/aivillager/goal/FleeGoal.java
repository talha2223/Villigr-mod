package com.aivillager.goal;

import net.minecraft.entity.ai.goal.FleeEntityGoal;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.passive.VillagerEntity;

public class FleeGoal extends FleeEntityGoal<HostileEntity> {
    public FleeGoal(VillagerEntity mob, float distance, double slowSpeed, double fastSpeed) {
        super(mob, HostileEntity.class, distance, slowSpeed, fastSpeed);
    }
}
