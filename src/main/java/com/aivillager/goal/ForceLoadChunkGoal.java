package com.aivillager.goal;

import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.ChunkPos;

public class ForceLoadChunkGoal extends Goal {
    private final VillagerEntity villager;
    private ChunkPos lastChunk;

    public ForceLoadChunkGoal(VillagerEntity villager) {
        this.villager = villager;
    }

    @Override
    public boolean canStart() { return true; }

    @Override
    public void tick() {
        if (villager.getWorld() instanceof ServerWorld serverWorld) {
            ChunkPos currentChunk = new ChunkPos(villager.getBlockPos());
            if (lastChunk != null && !lastChunk.equals(currentChunk)) {
                serverWorld.setChunkForced(lastChunk.x, lastChunk.z, false);
            }
            serverWorld.setChunkForced(currentChunk.x, currentChunk.z, true);
            lastChunk = currentChunk;
        }
    }

    @Override
    public void stop() {
        if (villager.getWorld() instanceof ServerWorld serverWorld && lastChunk != null) {
            serverWorld.setChunkForced(lastChunk.x, lastChunk.z, false);
            lastChunk = null;
        }
    }
}
