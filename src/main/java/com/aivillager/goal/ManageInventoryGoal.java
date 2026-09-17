package com.aivillager.goal;

import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.inventory.SimpleInventory;

public class ManageInventoryGoal extends Goal {
    private final VillagerEntity villager;
    private int tickCounter;

    public ManageInventoryGoal(VillagerEntity villager) {
        this.villager = villager;
    }

    @Override
    public boolean canStart() { return true; }

    @Override
    public void tick() {
        if (++tickCounter > 100) {
            tickCounter = 0;
            checkInventory();
        }
    }

    private void checkInventory() {
        SimpleInventory inv = villager.getInventory();
        boolean hasPickaxe = false;
        for (int i = 0; i < inv.size(); i++) {
            ItemStack stack = inv.getStack(i);
            if (stack.isOf(Items.IRON_PICKAXE) || stack.isOf(Items.DIAMOND_PICKAXE) || stack.isOf(Items.STONE_PICKAXE)) {
                hasPickaxe = true;
                break;
            }
        }
    }
}
