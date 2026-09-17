package com.aivillager.goal;

import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.util.math.Box;
import java.util.EnumSet;
import java.util.List;

public class GatherItemGoal extends Goal {
    private final VillagerEntity villager;
    private final String targetItemId;
    private ItemEntity targetItemEntity;

    public GatherItemGoal(VillagerEntity villager, String targetItemId) {
        this.villager = villager;
        this.targetItemId = targetItemId;
        this.setControls(EnumSet.of(Goal.Control.MOVE, Goal.Control.LOOK));
    }

    @Override
    public boolean canStart() { return findTargetItem(); }

    @Override
    public boolean shouldContinue() { return targetItemEntity != null && targetItemEntity.isAlive() && !villager.getInventory().isEmpty(); }

    @Override
    public void start() {
        if (targetItemEntity != null) villager.getNavigation().startMovingTo(targetItemEntity, 1.2);
    }

    @Override
    public void tick() {
        if (targetItemEntity != null) {
            villager.getLookControl().lookAt(targetItemEntity, 30.0F, 30.0F);
            villager.getNavigation().startMovingTo(targetItemEntity, 1.2);
        } else {
            findTargetItem();
        }
    }

    private boolean findTargetItem() {
        Box box = villager.getBoundingBox().expand(16.0);
        List<ItemEntity> items = villager.getWorld().getEntitiesByClass(ItemEntity.class, box, item -> {
            return net.minecraft.registry.Registries.ITEM.getId(item.getStack().getItem()).toString().equals(targetItemId) || targetItemId.equals("any");
        });
        if (!items.isEmpty()) {
            this.targetItemEntity = items.get(0);
            return true;
        }
        return false;
    }
}
