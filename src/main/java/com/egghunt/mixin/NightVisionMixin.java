package com.egghunt.mixin;

import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.Holder;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public class NightVisionMixin {

    @Inject(method = "hasEffect", at = @At("RETURN"), cancellable = true)
    private void egghunt$fakeHasNightVision(Holder<MobEffect> effect, CallbackInfoReturnable<Boolean> cir) {
        if (!cir.getReturnValue() && effect.equals(MobEffects.NIGHT_VISION)) {
            if ((Object) this instanceof LocalPlayer) {
                cir.setReturnValue(true);
            }
        }
    }

    @Inject(method = "getEffect", at = @At("RETURN"), cancellable = true)
    private void egghunt$fakeGetNightVision(Holder<MobEffect> effect, CallbackInfoReturnable<MobEffectInstance> cir) {
        if (cir.getReturnValue() == null && effect.equals(MobEffects.NIGHT_VISION)) {
            if ((Object) this instanceof LocalPlayer) {
                cir.setReturnValue(new MobEffectInstance(MobEffects.NIGHT_VISION, -1));
            }
        }
    }
}
