package com.egghunt;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.ChatFormatting;

public class EggHuntTracker {

    private static final String[] ARROWS = {"⬆", "↗", "➡", "↘", "⬇", "↙", "⬅", "↖"};

    public static Component getDisplay() {
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        if (player == null) return null;

        double px = player.getX();
        double py = player.getY();
        double pz = player.getZ();

        double bestDistSq = Double.MAX_VALUE;
        int bestIdx = -1;

        for (int i = 0; i < EggHuntClient.LOCATIONS.length; i++) {
            EggLocation loc = EggHuntClient.LOCATIONS[i];
            double dx = loc.x() + 0.5 - px;
            double dy = loc.y() - py;
            double dz = loc.z() + 0.5 - pz;
            double distSq = dx * dx + dy * dy + dz * dz;
            if (distSq < bestDistSq) {
                bestDistSq = distSq;
                bestIdx = i;
            }
        }

        if (bestIdx < 0) return null;

        EggLocation loc = EggHuntClient.LOCATIONS[bestIdx];
        double distance = Math.sqrt(bestDistSq);
        String arrow = getDirection(player, loc);

        // "Nearest Egg [12.3m] ➡ /w warpname — hint text"
        return Component.literal("Nearest Egg ")
            .withStyle(Style.EMPTY.withBold(true).withColor(loc.gold() ? ChatFormatting.GOLD : ChatFormatting.LIGHT_PURPLE))
            .append(Component.literal(String.format("[%.1fm] %s ", distance, arrow))
                .withStyle(Style.EMPTY.withBold(false).withColor(ChatFormatting.GRAY)))
            .append(Component.literal("/w " + loc.warp())
                .withStyle(Style.EMPTY.withBold(false).withColor(ChatFormatting.YELLOW)))
            .append(Component.literal(" — " + loc.hint())
                .withStyle(Style.EMPTY.withBold(false).withColor(ChatFormatting.WHITE)));
    }

    private static String getDirection(LocalPlayer player, EggLocation loc) {
        double dx = loc.x() + 0.5 - player.getX();
        double dz = loc.z() + 0.5 - player.getZ();

        double absoluteAngle = Math.atan2(-dx, dz);
        double playerYaw = Math.toRadians(player.getYRot());
        double relative = absoluteAngle - playerYaw + Math.PI;

        while (relative < -Math.PI) relative += 2 * Math.PI;
        while (relative > Math.PI) relative -= 2 * Math.PI;

        double step = 2 * Math.PI / 8;
        int index = (int) Math.round((relative + Math.PI) / step) % 8;
        return ARROWS[index];
    }
}
