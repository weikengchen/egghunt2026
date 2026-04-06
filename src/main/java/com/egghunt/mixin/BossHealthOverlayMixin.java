package com.egghunt.mixin;

import com.egghunt.EggHuntTracker;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.BossHealthOverlay;
import net.minecraft.client.gui.components.LerpingBossEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.world.BossEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Map;
import java.util.UUID;

@Mixin(BossHealthOverlay.class)
public class BossHealthOverlayMixin {

    @Shadow
    private Map<UUID, LerpingBossEvent> events;

    @Unique
    private static final UUID EGGHUNT_UUID = UUID.nameUUIDFromBytes("egghunt_tracker".getBytes());

    @Inject(method = "render", at = @At("HEAD"))
    private void egghunt$injectBossBar(GuiGraphics graphics, CallbackInfo ci) {
        Component display = EggHuntTracker.getDisplay();
        if (display == null) {
            this.events.remove(EGGHUNT_UUID);
            return;
        }

        LerpingBossEvent event = this.events.get(EGGHUNT_UUID);
        if (event == null) {
            event = new LerpingBossEvent(
                EGGHUNT_UUID, display, 1.0f,
                BossEvent.BossBarColor.PINK,
                BossEvent.BossBarOverlay.PROGRESS,
                false, false, false
            );
            this.events.put(EGGHUNT_UUID, event);
        } else {
            event.setName(display);
        }
    }
}
