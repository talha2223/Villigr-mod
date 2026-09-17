package com.aivillager.mixin;

import com.aivillager.api.ConversationManager;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(VillagerEntity.class)
public class VillagerInteractionMixin {

    @Inject(method = "interactMob", at = @At("HEAD"), cancellable = true)
    private void onInteract(PlayerEntity player, Hand hand, CallbackInfoReturnable<ActionResult> cir) {
        VillagerEntity villager = (VillagerEntity) (Object) this;

        if (villager.getWorld().isClient || hand != Hand.MAIN_HAND) return;
        if (player.isSneaking()) return;

        final String villagerName = villager.hasCustomName() ? villager.getName().getString() : "Ghaib Villager";

        if (player instanceof ServerPlayerEntity) {
            ServerPlayerEntity serverPlayer = (ServerPlayerEntity) player;
            ConversationManager.ACTIVE_CONVERSATIONS.put(player.getUuid(), villager);
            serverPlayer.sendMessage(Text.literal("\u00a7e[" + villagerName + "] \u00a7fBhai, kya haal hai? Chat mein kuch bolo!"), false);
            villager.getWorld().playSound(null, villager.getBlockPos(), net.minecraft.sound.SoundEvents.ENTITY_VILLAGER_AMBIENT, net.minecraft.sound.SoundCategory.NEUTRAL, 1.0F, 0.8F + (float)(Math.random() * 0.4));
        }
        cir.setReturnValue(ActionResult.CONSUME);
    }
}
