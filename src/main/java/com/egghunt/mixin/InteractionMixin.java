package com.egghunt.mixin;

import com.egghunt.EggHuntClient;
import com.egghunt.EggHuntProgress;
import com.egghunt.EggLocation;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MultiPlayerGameMode.class)
public class InteractionMixin {

    @Inject(method = "attack", at = @At("HEAD"))
    private void egghunt$onAttack(Player player, Entity entity, CallbackInfo ci) {
        egghunt$checkEntityNearEgg(entity);
    }

    @Inject(method = "interact", at = @At("HEAD"))
    private void egghunt$onInteract(Player player, Entity entity, InteractionHand hand, CallbackInfoReturnable<InteractionResult> cir) {
        egghunt$checkEntityNearEgg(entity);
    }

    @Unique
    private static void egghunt$checkEntityNearEgg(Entity entity) {
        double ex = entity.getX();
        double ey = entity.getY();
        double ez = entity.getZ();

        for (EggLocation loc : EggHuntClient.LOCATIONS) {
            if (EggHuntProgress.isFinished(loc)) continue;

            double dx = loc.x() + 0.5 - ex;
            double dy = loc.y() - ey;
            double dz = loc.z() + 0.5 - ez;
            if (dx * dx + dy * dy + dz * dz <= EggHuntClient.GLOW_DIST_SQ) {
                if (EggHuntProgress.markFinished(loc)) {
                    Minecraft mc = Minecraft.getInstance();
                    if (mc.player != null) {
                        int done = EggHuntProgress.finishedCount();
                        int total = EggHuntClient.LOCATIONS.length;
                        mc.player.displayClientMessage(
                            Component.literal("Egg found! (" + done + "/" + total + ")")
                                .withStyle(loc.gold() ? ChatFormatting.GOLD : ChatFormatting.LIGHT_PURPLE),
                            false
                        );
                    }
                }
                return;
            }
        }
    }
}
