package com.aivillager.goal;

import net.minecraft.block.BlockState;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.registry.Registries;
import net.minecraft.util.math.BlockPos;

import java.util.EnumSet;

public class MineResourceGoal extends Goal {
    private final VillagerEntity villager;
    private final String targetBlockId;
    private BlockPos targetPos;
    private int searchTicks;
    private int miningTicks;

    public MineResourceGoal(VillagerEntity villager, String targetBlockId) {
        this.villager = villager;
        this.targetBlockId = targetBlockId;
        this.setControls(EnumSet.of(Goal.Control.MOVE, Goal.Control.LOOK));
    }

    @Override
    public boolean canStart() { return targetBlockId != null && !targetBlockId.isEmpty(); }

    @Override
    public void start() {
        this.searchTicks = 0;
        this.miningTicks = 0;
        this.targetPos = null;
        findTargetBlock();
    }

    @Override
    public void stop() {
        this.targetPos = null;
        villager.getNavigation().stop();
    }

    @Override
    public void tick() {
        if (targetPos == null) {
            if (++searchTicks > 40) {
                searchTicks = 0;
                findTargetBlock();
            }
            return;
        }
        double distance = villager.squaredDistanceTo(targetPos.toCenterPos());
        villager.getLookControl().lookAt(targetPos.getX() + 0.5, targetPos.getY() + 0.5, targetPos.getZ() + 0.5, 30.0F, 30.0F);

        if (distance > 4.0) {
            villager.getNavigation().startMovingTo(targetPos.getX(), targetPos.getY(), targetPos.getZ(), 1.0);
        } else {
            villager.getNavigation().stop();
            if (++miningTicks > 60) {
                ServerWorld world = (ServerWorld) villager.getWorld();
                BlockState state = world.getBlockState(targetPos);
                if (Registries.BLOCK.getId(state.getBlock()).toString().equals(targetBlockId)) {
                    world.breakBlock(targetPos, true, villager);
                    this.targetPos = null;
                    this.miningTicks = 0;
                } else {
                    this.targetPos = null;
                }
            }
        }
    }

    private void findTargetBlock() {
        ServerWorld world = (ServerWorld) villager.getWorld();
        BlockPos center = villager.getBlockPos();
        for (int y = -10; y <= 10; y++) {
            for (int x = -10; x <= 10; x++) {
                for (int z = -10; z <= 10; z++) {
                    BlockPos p = center.add(x, y, z);
                    BlockState state = world.getBlockState(p);
                    if (Registries.BLOCK.getId(state.getBlock()).toString().equals(targetBlockId)) {
                        this.targetPos = p;
                        return;
                    }
                }
            }
        }
    }
}
