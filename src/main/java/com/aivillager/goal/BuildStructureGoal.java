package com.aivillager.goal;

import net.minecraft.block.BlockState;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import java.util.EnumSet;
import java.util.List;

public class BuildStructureGoal extends Goal {
    private final VillagerEntity villager;
    private final List<BlockPos> plan;
    private final BlockState blockToPlace;
    private int currentStep = 0;
    private int buildTicks = 0;

    public BuildStructureGoal(VillagerEntity villager, List<BlockPos> plan, BlockState blockToPlace) {
        this.villager = villager;
        this.plan = plan;
        this.blockToPlace = blockToPlace;
        this.setControls(EnumSet.of(Goal.Control.MOVE, Goal.Control.LOOK));
    }

    @Override
    public boolean canStart() { return plan != null && !plan.isEmpty() && currentStep < plan.size(); }

    @Override
    public boolean shouldContinue() { return currentStep < plan.size(); }

    @Override
    public void start() { buildTicks = 0; }

    @Override
    public void tick() {
        if (currentStep >= plan.size()) return;
        BlockPos targetPos = plan.get(currentStep);
        double distance = villager.squaredDistanceTo(targetPos.toCenterPos());
        villager.getLookControl().lookAt(targetPos.getX() + 0.5, targetPos.getY() + 0.5, targetPos.getZ() + 0.5, 30.0F, 30.0F);

        if (distance > 9.0) {
            villager.getNavigation().startMovingTo(targetPos.getX(), targetPos.getY(), targetPos.getZ(), 1.0);
        } else {
            villager.getNavigation().stop();
            if (++buildTicks > 20) {
                ServerWorld world = (ServerWorld) villager.getWorld();
                if (world.getBlockState(targetPos).isAir()) {
                    world.setBlockState(targetPos, blockToPlace);
                }
                buildTicks = 0;
                currentStep++;
            }
        }
    }
}
