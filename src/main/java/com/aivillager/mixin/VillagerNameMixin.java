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

    /**
     * Injects into the villager's init method to assign a random name.
     */
    @Inject(method = "initGoals", at = @At("TAIL"))
    private void onInit(CallbackInfo ci) {
        VillagerEntity villager = (VillagerEntity) (Object) this;

        // Only assign name if the villager doesn't already have one
        if (villager.hasCustomName()) {
            return;
        }

        String name = VILLAGER_NAMES[RANDOM.nextInt(VILLAGER_NAMES.length)];
        villager.setCustomName(new LiteralText(name));
        villager.setCustomNameVisible(true);
    }
}
