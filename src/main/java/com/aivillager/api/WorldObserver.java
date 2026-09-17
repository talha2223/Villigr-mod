package com.aivillager.api;

import net.minecraft.block.BlockState;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.registry.Registries;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.util.List;

public class WorldObserver {
    public static JsonObject observe(VillagerEntity villager, PlayerEntity player) {
        JsonObject obs = new JsonObject();
        ServerWorld world = (ServerWorld) villager.getWorld();

        obs.addProperty("time", world.getTimeOfDay());
        obs.addProperty("is_day", world.isDay());

        JsonObject vInfo = new JsonObject();
        vInfo.addProperty("health", villager.getHealth());
        vInfo.addProperty("pos_x", villager.getX());
        vInfo.addProperty("pos_y", villager.getY());
        vInfo.addProperty("pos_z", villager.getZ());
        obs.add("villager", vInfo);

        if (player != null) {
            JsonObject pInfo = new JsonObject();
            pInfo.addProperty("name", player.getName().getString());
            pInfo.addProperty("distance_to_villager", villager.distanceTo(player));
            obs.add("player", pInfo);
        }

        JsonArray entities = new JsonArray();
        Box box = villager.getBoundingBox().expand(16.0);
        List<LivingEntity> nearby = world.getEntitiesByClass(LivingEntity.class, box, e -> e != villager);
        for (LivingEntity e : nearby) {
            JsonObject eJson = new JsonObject();
            eJson.addProperty("type", Registries.ENTITY_TYPE.getId(e.getType()).toString());
            eJson.addProperty("distance", villager.distanceTo(e));
            entities.add(eJson);
        }
        obs.add("nearby_entities", entities);

        return obs;
    }
}
