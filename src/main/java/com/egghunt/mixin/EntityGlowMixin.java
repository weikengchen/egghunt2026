package com.egghunt.mixin;

import com.egghunt.EggHuntClient;
import com.egghunt.EggHuntProgress;
import com.egghunt.EggLocation;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Entity.class)
public class EntityGlowMixin {

    @Inject(method = "isCurrentlyGlowing", at = @At("RETURN"), cancellable = true)
    private void egghunt$addGlow(CallbackInfoReturnable<Boolean> cir) {
        if (cir.getReturnValue()) return;

        Entity self = (Entity) (Object) this;
        if (!self.level().isClientSide()) return;

        double ex = self.getX();
        double ey = self.getY();
        double ez = self.getZ();

        for (EggLocation loc : EggHuntClient.LOCATIONS) {
            if (EggHuntProgress.isFinished(loc)) continue;
            double dx = loc.x() + 0.5 - ex;
            double dy = loc.y() - ey;
            double dz = loc.z() + 0.5 - ez;
            if (dx * dx + dy * dy + dz * dz <= EggHuntClient.GLOW_DIST_SQ) {
                cir.setReturnValue(true);
                return;
            }
        }
    }
}
