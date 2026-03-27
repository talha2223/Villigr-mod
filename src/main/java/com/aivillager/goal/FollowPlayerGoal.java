package com.aivillager.goal;

import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.entity.player.PlayerEntity;

import java.util.EnumSet;

/**
 * Custom AI goal that makes a villager follow a specific player.
 * Activated when the AI responds with [ACTION:FOLLOW].
 */
public class FollowPlayerGoal extends Goal {

    private final VillagerEntity villager;
    private PlayerEntity targetPlayer;
    private final double speed;
    private int updateCountdownTicks;

    public FollowPlayerGoal(VillagerEntity villager, PlayerEntity player, double speed) {
        this.villager = villager;
        this.targetPlayer = player;
        this.speed = speed;
        this.updateCountdownTicks = 0;
        this.setControls(EnumSet.of(Goal.Control.MOVE, Goal.Control.LOOK));
    }

    @Override
    public boolean canStart() {
        if (targetPlayer == null || !targetPlayer.isAlive()) {
            return false;
        }

        double distance = villager.squaredDistanceTo(targetPlayer);
        // Start following if player is more than 3 blocks away but within 32 blocks
        return distance > 9.0 && distance < 1024.0;
    }

    @Override
    public boolean shouldContinue() {
        return targetPlayer != null && targetPlayer.isAlive()
                && villager.squaredDistanceTo(targetPlayer) < 1024.0;
    }

    @Override
    public void start() {
        updateCountdownTicks = 0;
    }

    @Override
    public void stop() {
        targetPlayer = null;
        villager.getNavigation().stop();
    }

    @Override
    public void tick() {
        // Look at the player
        villager.getLookControl().lookAt(targetPlayer, 10.0F, (float) villager.getLookPitchSpeed());

        // Update path every 10 ticks
        if (--updateCountdownTicks <= 0) {
            updateCountdownTicks = 10;

            // Move towards the player
            double distance = villager.squaredDistanceTo(targetPlayer);

            if (distance > 49.0) {
                // Teleport if too far away (> 7 blocks)
                villager.getNavigation().stop();
                // Optionally teleport: villager.teleport(targetPlayer.getX(), targetPlayer.getY(), targetPlayer.getZ());
            } else if (distance > 6.0) {
                // Walk to the player if more than 2.5 blocks away
                villager.getNavigation().startMovingTo(targetPlayer, this.speed);
            } else {
                // Stop moving if close enough
                villager.getNavigation().stop();
            }
        }
    }

    public void setTargetPlayer(PlayerEntity player) {
        this.targetPlayer = player;
    }
}
