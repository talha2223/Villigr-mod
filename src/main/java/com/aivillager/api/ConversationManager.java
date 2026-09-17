package com.aivillager.api;

import net.minecraft.entity.passive.VillagerEntity;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class ConversationManager {
    public static final Map<UUID, VillagerEntity> ACTIVE_CONVERSATIONS = new ConcurrentHashMap<>();
}
