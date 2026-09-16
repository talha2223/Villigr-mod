package com.aivillager.mixin;

import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import java.util.Random;

@Mixin(VillagerEntity.class)
public class VillagerNameMixin {
    private static final String[] VILLAGER_NAMES = { "Khan Saab", "Chaudhry", "Ustaad", "Bashir", "Ghulam" };
    private static final Random RANDOM = new Random();
    private boolean aivillager$nameAssigned = false;

    @Inject(method = "tick", at = @At("HEAD"))
    private void onTick(CallbackInfo ci) {
        if (aivillager$nameAssigned) return;
        VillagerEntity villager = (VillagerEntity) (Object) this;
        if (!villager.hasCustomName()) {
            villager.setCustomName(Text.literal(VILLAGER_NAMES[RANDOM.nextInt(VILLAGER_NAMES.length)]));
            villager.setCustomNameVisible(true);
        }
        aivillager$nameAssigned = true;
    }
}
