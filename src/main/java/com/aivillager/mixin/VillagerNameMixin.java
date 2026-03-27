package com.aivillager.mixin;

import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.text.LiteralText;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Random;

/**
 * Mixin that assigns random local names to villagers when they initialize.
 * Names appear as CustomName tags above the villager's head.
 */
@Mixin(VillagerEntity.class)
public class VillagerNameMixin {

    // Random Pakistani/Indian local names for villagers
    private static final String[] VILLAGER_NAMES = {
            "Khan Saab", "Chaudhry", "Ustaad", "Bashir", "Ghulam",
            "Rashid", "Farooq", "Javed", "Nawab", "Hakim",
            "Tajdar", "Rustam", "Zahoor", "Karim", "Noor Muhammad",
            "Dilawar", "Iqbal", "Latif", "Sultan", "Abdul",
            "Mehmood", "Yousuf", "Rahim", "Hanif", "Wazir",
            "Darbari", "Munshi", "Qalandar", "Peer", "Faqir"
    };

    private static final Random RANDOM = new Random();
    private boolean aivillager$nameAssigned = false;

    /**
     * Injects into the villager's tick method to assign a random name on first tick.
     * This is reliable because tick() is guaranteed to exist on all LivingEntity subclasses.
     */
    @Inject(method = "tick", at = @At("HEAD"))
    private void onTick(CallbackInfo ci) {
        if (aivillager$nameAssigned) return;

        VillagerEntity villager = (VillagerEntity) (Object) this;

        // Only assign name if the villager doesn't already have one
        if (!villager.hasCustomName()) {
            String name = VILLAGER_NAMES[RANDOM.nextInt(VILLAGER_NAMES.length)];
            villager.setCustomName(new LiteralText(name));
            villager.setCustomNameVisible(true);
        }

        aivillager$nameAssigned = true;
    }
}
