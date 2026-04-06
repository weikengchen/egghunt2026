package com.egghunt;

import static com.egghunt.EggLocation.pink;
import static com.egghunt.EggLocation.gold;

import com.mojang.blaze3d.pipeline.BlendFunction;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.platform.DepthTestFunction;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderEvents;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BeaconRenderer;
import net.minecraft.client.renderer.rendertype.RenderSetup;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

public class EggHuntClient implements ClientModInitializer {

    private static final String MOD_ID = "egghunt";
    private static final double RENDER_DISTANCE = 300.0;
    private static final double RENDER_DIST_SQ = RENDER_DISTANCE * RENDER_DISTANCE;

    public static final double GLOW_DIST_SQ = 3.0 * 3.0;

    private static final int PINK = ARGB.color(255, 255, 105, 180);
    private static final int GOLD = ARGB.color(255, 255, 215, 0);

    // Beacon beam pipelines with NO_DEPTH_TEST so beams show through blocks
    private static final RenderPipeline BEAM_OPAQUE_PIPELINE = RenderPipelines.register(
        RenderPipeline.builder(RenderPipelines.BEACON_BEAM_SNIPPET)
            .withLocation(Identifier.fromNamespaceAndPath(MOD_ID, "pipeline/beam_opaque"))
            .withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST)
            .withDepthWrite(false)
            .build()
    );

    private static final RenderPipeline BEAM_TRANSLUCENT_PIPELINE = RenderPipelines.register(
        RenderPipeline.builder(RenderPipelines.BEACON_BEAM_SNIPPET)
            .withLocation(Identifier.fromNamespaceAndPath(MOD_ID, "pipeline/beam_translucent"))
            .withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST)
            .withDepthWrite(false)
            .withBlend(BlendFunction.TRANSLUCENT)
            .build()
    );

    private static final RenderType BEAM_OPAQUE_TYPE = RenderType.create(
        "egghunt_beam_opaque",
        RenderSetup.builder(BEAM_OPAQUE_PIPELINE)
            .withTexture("Sampler0", BeaconRenderer.BEAM_LOCATION)
            .sortOnUpload()
            .createRenderSetup()
    );

    private static final RenderType BEAM_TRANSLUCENT_TYPE = RenderType.create(
        "egghunt_beam_translucent",
        RenderSetup.builder(BEAM_TRANSLUCENT_PIPELINE)
            .withTexture("Sampler0", BeaconRenderer.BEAM_LOCATION)
            .sortOnUpload()
            .createRenderSetup()
    );

    // Land display order for /checkegghunt
    public static final String[] LANDS = {
        "Esplanade", "Downtown Disney", "Mainstreet U.S.A.", "Tomorrowland",
        "Fantasyland", "Toontown", "Galaxy's Edge", "Critter Country",
        "Tom Sawyer's Island", "New Orleans Square", "Adventureland", "Frontierland",
        "Buena Vista Street", "Hollywood Land", "Avengers Campus", "Cars Land",
        "San Fransokyo", "Pixar Pier", "Paradise Gardens", "Grizzly Peak",
    };

    public static final EggLocation[] LOCATIONS = {
        // Galaxy's Edge (6)
        pink(332, 66, 928, "Galaxy's Edge", "milk", "in the bushes, to the right"),
        pink(523, 67, 875, "Galaxy's Edge", "creature", "outside the market, behind, in the bushes"),
        pink(778, 65, 816, "Galaxy's Edge", "rotr", "in the queue"),
        pink(726, 65, 842, "Galaxy's Edge", "rotr", "in the preshow, outside, before getting on the transport ship"),
        pink(662, 65, 1008, "Galaxy's Edge", "rotr", "in the first order ship, walk past the interrogation room"),
        pink(619, 61, 880, "Galaxy's Edge", "rotr", "in the grass room, behind the ride again npc"),

        // Critter Country (6)
        pink(590, 66, 449, "Critter Country", "hungry bear", "by fastpasses"),
        pink(719, 64, 522, "Critter Country", "splash", "side of pooh show building entrance; enter at around [702, 65, 499]"),
        pink(760, 64, 449, "Critter Country", "pooh corner", "outside pooh corner"),
        pink(772, 64, 437, "Critter Country", "pooh corner", "inside, past the iron door"),
        pink(707, 65, 409, "Critter Country", "splash", "by the splash queue and exit, in the bushes"),
        pink(560, 67, 460, "Critter Country", "canoe", "behind a tree"),

        // Tom Sawyer's Island (5)
        pink(433, 67, 440, "Tom Sawyer's Island", "tsi", "in a bush"),
        pink(470, 66, 504, "Tom Sawyer's Island", "tsi", "behind a tree"),
        pink(487, 75, 525, "Tom Sawyer's Island", "tsi", "on top of rock on land ship"),
        pink(543, 64, 621, "Tom Sawyer's Island", "tsi", "behind fort, in a cave entrance"),
        pink(376, 64, 448, "Tom Sawyer's Island", "tsi", "across from the mark twain, in a cave entrance"),

        // New Orleans Square (6)
        pink(570, 68, 346, "New Orleans Square", "bayou", "by the control panel for the stretching room"),
        pink(511, 69, 162, "New Orleans Square", "nosstation", "in a corner beyond the tracks"),
        pink(449, 66, 193, "New Orleans Square", "coa", "in a shop"),
        pink(392, 68, 144, "New Orleans Square", "blue bayou", "in the restaurant"),
        pink(364, 66, 198, "New Orleans Square", "blue bayou", "in the corner of a shop"),
        pink(330, 64, 264, "New Orleans Square", "potc", "behind a tree near the entrance"),

        // Adventureland (6)
        pink(430, 63, 0, "Adventureland", "indy", "in the queue"),
        pink(231, 65, 213, "Adventureland", "indy", "behind the indy vendor"),
        pink(240, 64, 216, "Adventureland", "indy", "behind the leaderboards"),
        pink(157, 65, 352, "Adventureland", "adventureland", "behind a tree in the rocks"),
        pink(125, 64, 339, "Adventureland", "adventureland", "dole whip stand"),
        gold(105, 66, 326, "Adventureland", "tiki", "up the ramp to the right from the warp"),

        // Frontierland (6)
        pink(119, 64, 413, "Frontierland", "frontierland", "in the bushes by the wall"),
        pink(184, 65, 494, "Frontierland", "shootin", "in the rancho de zocalo restaurant, in a planter"),
        pink(217, 69, 377, "Frontierland", "horseshoe", "in a building (not the golden horseshoe), up the stairs"),
        pink(245, 70, 438, "Frontierland", "mark twain", "in a tree"),
        pink(321, 64, 492, "Frontierland", "btm", "on the mark twain dock"),
        pink(278, 65, 714, "Frontierland", "ge", "behind btm"),

        // Buena Vista Street (6)
        pink(-50, 64, -367, "Buena Vista Street", "oswald", "in the alleyway next to oswald's"),
        pink(42, 68, -412, "Buena Vista Street", "camera", "above, by the conductor hats"),
        pink(84, 64, -479, "Buena Vista Street", "airfield", "in the coffee (seating) section of clarabelle's"),
        pink(22, 64, -601, "Buena Vista Street", "carthay", "towards avengers campus, on the left"),
        pink(-55, 64, -488, "Buena Vista Street", "hollywood", "behind a plant in front of the blue building"),
        pink(-58, 65, -595, "Buena Vista Street", "katie", "in the office"),

        // Hollywood Land (6)
        pink(-134, 64, -594, "Hollywood Land", "award", "in the bathroom across from award wieners"),
        pink(-165, 64, -380, "Hollywood Land", "soundstage", "outside all the way to the left, in the bushes"),
        pink(-248, 64, -392, "Hollywood Land", "soundstage", "in the bushes of the monsters inc exit"),
        pink(-269, 65, -423, "Hollywood Land", "monsters inc", "behind boo's door at the entrance"),
        pink(-328, 73, -520, "Hollywood Land", "hyperion", "up the stairs behind the facades; enter at [-325, 64, -521]"),
        pink(-366, 65, -522, "Hollywood Land", "hyperion", "in the theater"),

        // Avengers Campus (6)
        pink(-311, 64, -715, "Avengers Campus", "gotg", "in the queue; entrance to the preshow room that's not in use"),
        pink(-216, 64, -677, "Avengers Campus", "pym", "behind the counter, to the right"),
        pink(1, 64, -685, "Avengers Campus", "ac", "by the colorful wall"),
        pink(-80, 68, -767, "Avengers Campus", "suppliers", "on a bush above the pin trader"),
        pink(-124, 64, -783, "Avengers Campus", "sanctum", "in the bushes by the checkered floor"),
        pink(-192, 64, -816, "Avengers Campus", "sanctum", "in the bushes, to the left, on the way to cars land"),

        // Cars Land (6)
        pink(-51, 63, -840, "Cars Land", "cozy cone", "in the second cone"),
        pink(-30, 63, -760, "Cars Land", "mater", "in the back, behind the ride"),
        pink(64, 67, -852, "Cars Land", "sarge", "on a shelf"),
        pink(65, 65, -1065, "Cars Land", "rsr", "hidden in the queue, behind a tree"),
        pink(-56, 65, -1057, "Cars Land", "rsr", "in the exit queue, on a hill near the gates"),
        pink(119, 63, -992, "Cars Land", "ramones", "by the rocks on the way to san fransokyo"),

        // San Fransokyo (6)
        pink(144, 63, -845, "San Fransokyo", "filmore", "under the bridge and across from ghiardelli's"),
        pink(179, 63, -903, "San Fransokyo", "sfs", "near the pin trader in the orange and blue building"),
        pink(182, 63, -955, "San Fransokyo", "sfs", "near the hiro and baymax mng area"),
        pink(274, 63, -940, "San Fransokyo", "sfs", "in the seating area, to the right from the warp"),
        pink(289, 65, -852, "San Fransokyo", "sfs", "behind the entire coffee stand in paradise park"),
        pink(296, 64, -853, "San Fransokyo", "sfs", "behind the counter of the coffee stand in paradise park"),

        // Pixar Pier (6)
        pink(267, 66, -1010, "Pixar Pier", "knicks knacks", "near the adorable snowman ice cream shop"),
        pink(299, 61, -1100, "Pixar Pier", "ic", "in the bushes underneath the walkway, near the queue"),
        pink(266, 67, -1137, "Pixar Pier", "ic", "in the upper section of the queue"),
        pink(430, 61, -1123, "Pixar Pier", "midway mania", "across from senior buzz churros"),
        pink(529, 60, -1089, "Pixar Pier", "boardwalk", "in between the wall-e and heimlich games"),
        pink(516, 56, -1042, "Pixar Pier", "palaround", "in the queue"),

        // Paradise Gardens (7)
        pink(-481, 58, 747, "Paradise Gardens", "sss", "hit the button at [632, 62, -940]"),
        pink(705, 62, -937, "Paradise Gardens", "sss", "behind the counter of the restaurant across the swings"),
        pink(728, 62, -879, "Paradise Gardens", "paradise gardens", "outside the restaurant, across from jumpin jellyfish, in the bushes"),
        pink(570, 63, -788, "Paradise Gardens", "jj", "behind a tree near corndog castle"),
        gold(526, 57, -787, "Paradise Gardens", "zephyr", "enter at [520, 63, -791] near seaside souvenirs"),
        pink(491, 64, -796, "Paradise Gardens", "zephyr", "in the outside ariel queue"),
        gold(252, 66, -772, "Paradise Gardens", "paradise park", "in some bushes across from the sfs bridge"),

        // Grizzly Peak (6)
        gold(265, 71, -683, "Grizzly Peak", "eureka", "parkour that's under the bridge"),
        pink(257, 64, -642, "Grizzly Peak", "eureka", "behind a tree near the teal car; in a corner of the yellow building"),
        pink(224, 70, -573, "Grizzly Peak", "pass trail", "in the rocks/trees behind the grr queue"),
        pink(209, 72, -541, "Grizzly Peak", "pass trail", "ride dispatcher area before the big drop; enter at [242, 64, -535]"),
        pink(263, 64, -497, "Grizzly Peak", "pass trail", "overlook area for the big drop"),
        pink(204, 69, -436, "Grizzly Peak", "humphrey's", "above in a red structure near the soarin' queue"),

        // Mainstreet U.S.A. (6)
        pink(21, 71, -43, "Mainstreet U.S.A.", "dlrr", "the right side of the dlrr queue"),
        pink(69, 66, 87, "Mainstreet U.S.A.", "dapper dans", "inside the emporium"),
        pink(-24, 71, 104, "Mainstreet U.S.A.", "moe", "past the big painting, in room with the open door"),
        pink(-15, 65, 244, "Mainstreet U.S.A.", "penny arcade", "on the porch of a purply-grey and white building"),
        pink(55, 64, 213, "Mainstreet U.S.A.", "penny arcade", "behind the ice cream parlor counter"),
        pink(-85, 64, 272, "Mainstreet U.S.A.", "plaza point", "behind a tree, behind the corndog wagon"),

        // Tomorrowland (6)
        pink(-194, 63, 377, "Tomorrowland", "star tours", "behind the planters, by a wall"),
        pink(-312, 68, 414, "Tomorrowland", "peoplemover", "by the exit escalator"),
        pink(-292, 70, 312, "Tomorrowland", "starcade", "up the stairs to space"),
        pink(-291, 65, 464, "Tomorrowland", "little green men", "in the spiderman room by the galactic grill"),
        pink(-516, 65, 452, "Tomorrowland", "tomorrowland station", "in the bushes to the right"),
        pink(-547, 66, 434, "Tomorrowland", "tomorrowland station", "to the right of the platform"),

        // Fantasyland (6)
        pink(-23, 64, 573, "Fantasyland", "castle courtyard", "behind a bench"),
        pink(84, 69, 550, "Fantasyland", "faire", "enter the bushes at around [67, 67, 548]"),
        pink(-42, 65, 698, "Fantasyland", "toad", "inside, in the queue"),
        pink(-52, 64, 718, "Fantasyland", "toad", "outside, in the queue"),
        pink(143, 66, 750, "Fantasyland", "casey", "in the bushes, on the way to frontierland"),
        pink(-297, 66, 952, "Fantasyland", "iasw", "by the wall to the right, enter at [-295, 66, 949]"),

        // Toontown (5)
        pink(-70, 64, 1064, "Toontown", "centoonial park", "behind a tree, by the worm shaped things"),
        pink(-193, 65, 1048, "Toontown", "roger", "in the queue"),
        pink(-113, 64, 1138, "Toontown", "mmrr", "in the bushes by the pin trader"),
        pink(-18, 64, 1140, "Toontown", "minnie", "behind the yellow building"),
        pink(109, 70, 1112, "Toontown", "popcorn", "on a cliff"),

        // Downtown Disney (7)
        pink(232, 65, -93, "Downtown Disney", "locker", "behind the hedges"),
        pink(321, 65, -307, "Downtown Disney", "dtd", "by wod outside vendors"),
        pink(456, 64, -327, "Downtown Disney", "dtd", "by a tree at the other side of wod, by the light blue wall"),
        pink(474, 64, -341, "Downtown Disney", "dtd", "even closer to the light blue wall at the other side of wod"),
        pink(719, 65, -275, "Downtown Disney", "citizens", "in trixie's"),
        pink(758, 65, -250, "Downtown Disney", "citizens", "in moe's vault; enter at [756, 66, -246]"),
        pink(776, 65, -236, "Downtown Disney", "citizens", "in larry's"),

        // Esplanade (6)
        pink(106, 64, -213, "Esplanade", "dtd", "in a ticket booth"),
        pink(-86, 64, -78, "Esplanade", "dl", "behind a 'pardon our pixie dust' wall, to the right of the park entrance"),
        pink(-443, 65, -157, "Esplanade", "bus", "on the bus"),
        gold(-738, 64, -196, "Esplanade", "ihop", "back left corner"),
        pink(1168, 64, 782, "Esplanade", "tram", "behind the stationary tram, beyond the rideable ones"),
        pink(1077, 64, 640, "Esplanade", "tram", "beyond and to the right of the pixar pals escalator"),
    };

    @Override
    public void onInitializeClient() {
        EggHuntProgress.load();
        WorldRenderEvents.AFTER_ENTITIES.register(this::onWorldRender);

        // Auto-sprint: force sprint when moving forward
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.player != null && client.player.input.hasForwardImpulse()
                    && (client.player.getFoodData().getFoodLevel() > 6 || client.player.getAbilities().flying)
                    && !client.player.isUsingItem()) {
                client.player.setSprinting(true);
            }
        });

        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
            dispatcher.register(ClientCommandManager.literal("resetegghunt").executes(context -> {
                EggHuntProgress.reset();
                context.getSource().sendFeedback(
                    Component.literal("Egg hunt progress reset!").withStyle(ChatFormatting.GREEN)
                );
                return 1;
            }));
            dispatcher.register(ClientCommandManager.literal("checkegghunt").executes(context -> {
                int done = EggHuntProgress.finishedCount();
                int total = LOCATIONS.length;
                context.getSource().sendFeedback(
                    Component.literal("=== Egg Hunt Progress: " + done + "/" + total + " ===")
                        .withStyle(ChatFormatting.AQUA, ChatFormatting.BOLD)
                );

                for (String land : LANDS) {
                    java.util.List<EggLocation> remaining = new java.util.ArrayList<>();
                    int landTotal = 0;
                    for (EggLocation loc : LOCATIONS) {
                        if (loc.land().equals(land)) {
                            landTotal++;
                            if (!EggHuntProgress.isFinished(loc)) remaining.add(loc);
                        }
                    }
                    if (remaining.isEmpty()) {
                        context.getSource().sendFeedback(
                            Component.literal(land + " (" + landTotal + "/" + landTotal + ")")
                                .withStyle(ChatFormatting.GREEN, ChatFormatting.STRIKETHROUGH)
                        );
                    } else {
                        int landDone = landTotal - remaining.size();
                        context.getSource().sendFeedback(
                            Component.literal(land + " (" + landDone + "/" + landTotal + ")")
                                .withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD)
                        );
                        for (EggLocation loc : remaining) {
                            Component egg = Component.literal(loc.gold() ? "  \u2b50 " : "  \u2022 ")
                                .withStyle(loc.gold() ? ChatFormatting.GOLD : ChatFormatting.LIGHT_PURPLE)
                                .append(Component.literal("[" + loc.x() + ", " + loc.y() + ", " + loc.z() + "] ")
                                    .withStyle(ChatFormatting.GRAY))
                                .append(Component.literal("/w " + loc.warp() + " ")
                                    .withStyle(ChatFormatting.YELLOW))
                                .append(Component.literal(loc.hint())
                                    .withStyle(ChatFormatting.WHITE));
                            context.getSource().sendFeedback(egg);
                        }
                    }
                }
                return 1;
            }));
        });
    }

    private void onWorldRender(WorldRenderContext context) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) return;

        Vec3 camPos = mc.gameRenderer.getMainCamera().position();
        float partialTick = mc.getDeltaTracker().getGameTimeDeltaPartialTick(false);
        float animationTime = Math.floorMod(mc.level.getGameTime(), 40L) + partialTick;

        PoseStack poseStack = context.matrices();
        SubmitNodeCollector collector = context.commandQueue();

        for (EggLocation loc : LOCATIONS) {
            if (EggHuntProgress.isFinished(loc)) continue;
            double dx = loc.x() + 0.5 - camPos.x;
            double dz = loc.z() + 0.5 - camPos.z;
            if (dx * dx + dz * dz > RENDER_DIST_SQ) continue;

            int color = loc.gold() ? GOLD : PINK;

            poseStack.pushPose();
            poseStack.translate(
                loc.x() - camPos.x,
                -camPos.y,
                loc.z() - camPos.z
            );

            submitBeam(poseStack, collector, animationTime, loc.y() + 3, color, 0.2F, 0.25F);

            poseStack.popPose();
        }
    }

    // Reimplemented from BeaconRenderer.submitBeaconBeam, using our no-depth-test render types
    private static void submitBeam(
        PoseStack poseStack, SubmitNodeCollector collector,
        float animationTime, int beamStart, int color,
        float solidRadius, float glowRadius
    ) {
        int beamEnd = 320;
        int height = beamEnd - beamStart;
        poseStack.pushPose();
        poseStack.translate(0.5, 0.0, 0.5);

        float scroll = -animationTime;
        float texVOff = Mth.frac(scroll * 0.2F - Mth.floor(scroll * 0.1F));

        // Inner solid beam (rotates)
        poseStack.pushPose();
        poseStack.mulPose(Axis.YP.rotationDegrees(animationTime * 2.25F - 45.0F));
        float vv2 = -1.0F + texVOff;
        float vv1 = height * (0.5F / solidRadius) + vv2;
        collector.submitCustomGeometry(poseStack, BEAM_OPAQUE_TYPE, (pose, buffer) ->
            renderPart(pose, buffer, color, beamStart, beamEnd,
                0.0F, solidRadius, solidRadius, 0.0F,
                -solidRadius, 0.0F, 0.0F, -solidRadius,
                0.0F, 1.0F, vv1, vv2)
        );
        poseStack.popPose();

        // Outer translucent glow (static)
        float gvv2 = -1.0F + texVOff;
        float gvv1 = height + gvv2;
        collector.submitCustomGeometry(poseStack, BEAM_TRANSLUCENT_TYPE, (pose, buffer) ->
            renderPart(pose, buffer, ARGB.color(32, color), beamStart, beamEnd,
                -glowRadius, -glowRadius, glowRadius, -glowRadius,
                -glowRadius, glowRadius, glowRadius, glowRadius,
                0.0F, 1.0F, gvv1, gvv2)
        );

        poseStack.popPose();
    }

    private static void renderPart(
        PoseStack.Pose pose, VertexConsumer buffer, int color,
        int beamStart, int beamEnd,
        float wnx, float wnz, float enx, float enz,
        float wsx, float wsz, float esx, float esz,
        float uu1, float uu2, float vv1, float vv2
    ) {
        renderQuad(pose, buffer, color, beamStart, beamEnd, wnx, wnz, enx, enz, uu1, uu2, vv1, vv2);
        renderQuad(pose, buffer, color, beamStart, beamEnd, esx, esz, wsx, wsz, uu1, uu2, vv1, vv2);
        renderQuad(pose, buffer, color, beamStart, beamEnd, enx, enz, esx, esz, uu1, uu2, vv1, vv2);
        renderQuad(pose, buffer, color, beamStart, beamEnd, wsx, wsz, wnx, wnz, uu1, uu2, vv1, vv2);
    }

    private static void renderQuad(
        PoseStack.Pose pose, VertexConsumer buffer, int color,
        int beamStart, int beamEnd,
        float x1, float z1, float x2, float z2,
        float uu1, float uu2, float vv1, float vv2
    ) {
        buffer.addVertex(pose, x1, beamEnd, z1).setColor(color).setUv(uu2, vv1)
            .setOverlay(OverlayTexture.NO_OVERLAY).setLight(15728880).setNormal(pose, 0.0F, 1.0F, 0.0F);
        buffer.addVertex(pose, x1, beamStart, z1).setColor(color).setUv(uu2, vv2)
            .setOverlay(OverlayTexture.NO_OVERLAY).setLight(15728880).setNormal(pose, 0.0F, 1.0F, 0.0F);
        buffer.addVertex(pose, x2, beamStart, z2).setColor(color).setUv(uu1, vv2)
            .setOverlay(OverlayTexture.NO_OVERLAY).setLight(15728880).setNormal(pose, 0.0F, 1.0F, 0.0F);
        buffer.addVertex(pose, x2, beamEnd, z2).setColor(color).setUv(uu1, vv1)
            .setOverlay(OverlayTexture.NO_OVERLAY).setLight(15728880).setNormal(pose, 0.0F, 1.0F, 0.0F);
    }
}
