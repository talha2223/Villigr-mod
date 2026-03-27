package com.aivillager.goal;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.entity.player.PlayerEntity;

import java.util.EnumSet;
import java.util.List;

/**
 * Custom AI goal that makes a villager protect a player by attacking
 * hostile mobs near them. Activated when the AI responds with [ACTION:PROTECT].
 *
 * IMPORTANT: Villagers normally have 20 HP and 0 attack damage.
 * The mixin boosts their stats when protect mode is activated.
 */
public class ProtectPlayerGoal extends Goal {

    private final VillagerEntity villager;
    private PlayerEntity playerToProtect;
    private HostileEntity currentTarget;
    private int cooldownTicks;

    public ProtectPlayerGoal(VillagerEntity villager, PlayerEntity player) {
        this.villager = villager;
        this.playerToProtect = player;
        this.cooldownTicks = 0;
        this.setControls(EnumSet.of(Goal.Control.MOVE, Goal.Control.LOOK, Goal.Control.TARGET));
    }

    @Override
    public boolean canStart() {
        if (playerToProtect == null || !playerToProtect.isAlive()) {
            return false;
        }

        // Check if there are hostile mobs near the player
        HostileEntity nearestHostile = findNearestHostile();
        return nearestHostile != null;
    }

    @Override
    public boolean shouldContinue() {
        return playerToProtect != null && playerToProtect.isAlive();
    }

    @Override
    public void start() {
        cooldownTicks = 0;
    }

    @Override
    public void stop() {
        currentTarget = null;
        villager.setTarget(null);
    }

    @Override
    public void tick() {
        if (--cooldownTicks > 0) return;
        cooldownTicks = 5; // Check every 5 ticks

        // Find the nearest hostile mob targeting the player
        HostileEntity nearestHostile = findNearestHostile();

        if (nearestHostile != null) {
            currentTarget = nearestHostile;
            villager.setTarget(nearestHostile);

            // Move towards the hostile mob
            villager.getNavigation().startMovingTo(nearestHostile, 1.2);

            // Look at the mob
            villager.getLookControl().lookAt(nearestHostile, 30.0F, 30.0F);

            // Attack if within range
            double distance = villager.squaredDistanceTo(nearestHostile);
            if (distance < 4.0) { // Within 2 blocks
                villager.tryAttack(nearestHostile);
            }
        } else {
            // No hostile mobs nearby, return to player
            if (villager.squaredDistanceTo(playerToProtect) > 16.0) {
                villager.getNavigation().startMovingTo(playerToProtect, 1.0);
            } else {
                villager.getNavigation().stop();
                villager.getLookControl().lookAt(playerToProtect, 10.0F, (float) villager.getLookPitchSpeed());
            }
        }
    }

    /**
     * Finds the nearest hostile entity that is targeting the protected player.
     */
    private HostileEntity findNearestHostile() {
        List<HostileEntity> hostiles = villager.world.getEntitiesByClass(
                HostileEntity.class,
                villager.getBoundingBox().expand(16.0),
                mob -> mob.isAlive() && (mob.getTarget() == playerToProtect || mob.squaredDistanceTo(playerToProtect) < 64.0)
        );

        HostileEntity nearest = null;
        double nearestDist = Double.MAX_VALUE;

        for (HostileEntity mob : hostiles) {
            double dist = mob.squaredDistanceTo(villager);
            if (dist < nearestDist) {
                nearestDist = dist;
                nearest = mob;
            }
        }

        return nearest;
    }

    public void setPlayerToProtect(PlayerEntity player) {
        this.playerToProtect = player;
    }
}
