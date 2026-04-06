package com.egghunt;

import com.mojang.blaze3d.pipeline.BlendFunction;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.platform.DepthTestFunction;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderEvents;
import net.minecraft.client.Minecraft;
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
    private static final double RENDER_DISTANCE = 500.0;
    private static final double RENDER_DIST_SQ = RENDER_DISTANCE * RENDER_DISTANCE;

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

    // {x, y, z, color} — color: 0 = pink, 1 = gold
    private static final int[][] LOCATIONS = {
        // Galaxy's Edge (6)
        {332, 66, 928, 0}, {523, 67, 875, 0}, {778, 65, 816, 0},
        {726, 65, 842, 0}, {662, 65, 1008, 0}, {619, 61, 880, 0},

        // Critter Country (6)
        {590, 66, 449, 0}, {719, 64, 522, 0}, {760, 64, 449, 0},
        {772, 64, 437, 0}, {707, 65, 409, 0}, {560, 67, 460, 0},

        // Tom Sawyer's Island (5)
        {433, 67, 440, 0}, {470, 66, 504, 0}, {487, 75, 525, 0},
        {543, 64, 621, 0}, {376, 64, 448, 0},

        // New Orleans Square (6)
        {570, 68, 346, 0}, {511, 69, 162, 0}, {449, 66, 193, 0},
        {392, 68, 144, 0}, {364, 66, 198, 0}, {330, 64, 264, 0},

        // Adventureland (6)
        {430, 63, 0, 0}, {231, 65, 213, 0}, {240, 64, 216, 0},
        {157, 65, 352, 0}, {125, 64, 339, 0}, {105, 66, 326, 1},

        // Frontierland (6)
        {119, 64, 413, 0}, {184, 65, 494, 0}, {217, 69, 377, 0},
        {245, 70, 438, 0}, {321, 64, 492, 0}, {278, 65, 714, 0},

        // Buena Vista Street (6)
        {-50, 64, -367, 0}, {42, 68, -412, 0}, {84, 64, -479, 0},
        {22, 64, -601, 0}, {-55, 64, -488, 0}, {-58, 65, -595, 0},

        // Hollywood Land (6)
        {-134, 64, -594, 0}, {-165, 64, -380, 0}, {-248, 64, -392, 0},
        {-269, 65, -423, 0}, {-328, 73, -520, 0}, {-366, 65, -522, 0},

        // Avengers Campus (6)
        {-311, 64, -715, 0}, {-216, 64, -677, 0}, {1, 64, -685, 0},
        {-80, 68, -767, 0}, {-124, 64, -783, 0}, {-192, 64, -816, 0},

        // Cars Land (6)
        {-51, 63, -840, 0}, {-30, 63, -760, 0}, {64, 67, -852, 0},
        {65, 65, -1065, 0}, {-56, 65, -1057, 0}, {119, 63, -992, 0},

        // San Fransokyo (6)
        {144, 63, -845, 0}, {179, 63, -903, 0}, {182, 63, -955, 0},
        {274, 63, -940, 0}, {289, 65, -852, 0}, {296, 64, -853, 0},

        // Pixar Pier (6)
        {267, 66, -1010, 0}, {299, 70, -1100, 0}, {266, 67, -1137, 0},
        {430, 61, -1123, 0}, {529, 60, -1089, 0}, {516, 56, -1042, 0},

        // Paradise Gardens (7)
        {-481, 58, 747, 0}, {705, 62, -937, 0}, {728, 62, -879, 0},
        {570, 63, -788, 0}, {526, 57, -787, 1}, {491, 64, -796, 0},
        {252, 66, -772, 1},

        // Grizzly Peak (6)
        {265, 71, -683, 1}, {257, 64, -642, 0}, {224, 70, -573, 0},
        {209, 72, -541, 0}, {263, 64, -497, 0}, {204, 69, -436, 0},

        // Mainstreet U.S.A. (6)
        {21, 71, -43, 0}, {69, 66, 87, 0}, {-24, 71, 104, 0},
        {-15, 65, 244, 0}, {55, 64, 213, 0}, {-85, 64, 272, 0},

        // Tomorrowland (6)
        {-194, 63, 377, 0}, {-312, 68, 414, 0}, {-292, 70, 312, 0},
        {-291, 65, 464, 0}, {-516, 65, 452, 0}, {-547, 66, 434, 0},

        // Fantasyland (6)
        {-23, 64, 573, 0}, {84, 69, 550, 0}, {-42, 65, 698, 0},
        {-52, 64, 718, 0}, {143, 66, 750, 0}, {-297, 66, 952, 0},

        // Toontown (5)
        {-70, 64, 1064, 0}, {-193, 65, 1048, 0}, {-113, 64, 1138, 0},
        {-18, 64, 1140, 0}, {109, 70, 1112, 0},

        // Downtown Disney (7)
        {232, 65, -93, 0}, {321, 65, -307, 0}, {456, 64, -327, 0},
        {474, 64, -341, 0}, {719, 65, -275, 0}, {758, 65, -250, 0},
        {776, 65, -236, 0},

        // Esplanade (6)
        {106, 64, -213, 0}, {-86, 64, -78, 0}, {-443, 65, -157, 0},
        {-738, 64, -196, 1}, {1168, 64, 782, 0}, {1077, 64, 640, 0},
    };

    @Override
    public void onInitializeClient() {
        WorldRenderEvents.AFTER_ENTITIES.register(this::onWorldRender);
    }

    private void onWorldRender(WorldRenderContext context) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) return;

        Vec3 camPos = mc.gameRenderer.getMainCamera().position();
        float partialTick = mc.getDeltaTracker().getGameTimeDeltaPartialTick(false);
        float animationTime = Math.floorMod(mc.level.getGameTime(), 40L) + partialTick;

        PoseStack poseStack = context.matrices();
        SubmitNodeCollector collector = context.commandQueue();

        for (int[] loc : LOCATIONS) {
            double dx = loc[0] + 0.5 - camPos.x;
            double dz = loc[2] + 0.5 - camPos.z;
            if (dx * dx + dz * dz > RENDER_DIST_SQ) continue;

            int color = loc[3] == 1 ? GOLD : PINK;

            poseStack.pushPose();
            poseStack.translate(
                loc[0] - camPos.x,
                -camPos.y,
                loc[2] - camPos.z
            );

            submitBeam(poseStack, collector, animationTime, loc[1], color, 0.5F, 1.5F);

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
