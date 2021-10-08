package com.parzivail.lightlevel;

import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.loader.impl.util.log.Log;
import net.fabricmc.loader.impl.util.log.LogCategory;
import net.fabricmc.loader.impl.util.log.LogLevel;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.math.*;
import net.minecraft.world.BlockStateRaycastContext;
import net.minecraft.world.LightType;

import java.io.Console;
import java.util.logging.Logger;

public class LLWorldRender {
    private static final VertexConsumerProvider vertConsumer;

    static {
        vertConsumer = VertexConsumerProvider.immediate(new BufferBuilder(256));
    }

    public static void render(MatrixStack matrices, float tickDelta, long limitTime, boolean renderBlockOutline, Camera camera, GameRenderer gameRenderer, LightmapTextureManager lightmapTextureManager, Matrix4f matrix4f, Frustum frustum) {
        if (!LightLevel.isEnabled())
            return;

        MinecraftClient client = MinecraftClient.getInstance();
        if (client == null)
            return;

        ClientPlayerEntity player = client.player;
        ClientWorld world = client.world;
        if (player == null || world == null)
            return;

        TextRenderer f = client.textRenderer;
        if (f == null)
            return;

        Vec3d pos = camera.getPos();

        matrices.push();

        boolean showBothValues = client.options.debugEnabled;

        float s = showBothValues ? 1 / 32f : 1 / 16f;

        matrices.translate(-pos.x, -pos.y, -pos.z);

        RenderSystem.enablePolygonOffset();
        RenderSystem.polygonOffset(-1, -1);

        Vec3f rot = camera.getRotation().toEulerXyz();

        BlockPos cent = camera.getBlockPos().add(new Vec3i(Math.round(8 * Math.sin(rot.getY())),0,Math.round(8 * Math.cos(rot.getY()))));

        float deg = (Math.round((360+camera.getRotation().toEulerXyzDegrees().getY())/90)%4)*90;

        int n = Math.round(deg / 90) % 4;

        for (int x = -8; x <= 8; x++)
            for (int z = -8; z <= 8; z++)
                for (int y = -8; y <= 2; y++) {
                    BlockPos queryPos = cent.add(x, y, z);

                    if (!world.isTopSolid(queryPos.down(), player) || world.isTopSolid(queryPos, player))
                        continue;

                    RenderLightLevel(matrices, world, f, showBothValues, s, queryPos, deg, n);
                }


        matrices.pop();

        RenderSystem.disablePolygonOffset();
    }

    private static void RenderLightLevel(MatrixStack matrices, ClientWorld world, TextRenderer f, boolean showBothValues, float s, BlockPos queryPos, float deg, int n) {
        matrices.push();

        matrices.translate(queryPos.getX(), queryPos.getY(), queryPos.getZ());
        matrices.multiply(new Quaternion(Vec3f.POSITIVE_X, -90, true));

        matrices.translate(n == 0 || n == 1 ? 1 : 0, n == 0 || n == 3 ? -1 : 0, 0);
        matrices.multiply(new Quaternion(Vec3f.POSITIVE_Z, deg + 180, true));

        matrices.scale(s, -s, s);

        int blockLight = world.getLightLevel(LightType.BLOCK, queryPos);
        int skyLight = world.getLightLevel(LightType.SKY, queryPos);

        int color = 0xFFFFFF; // spawn never

        if (blockLight < 8) {
            if (skyLight < 8)
                color = 0xFF0000; // Spawn at any time
            else
                color = 0xFFFF00; // Spawn only at night
        }

        if (showBothValues) {
            drawNumber(matrices, f, "■" + blockLight, color, 8, 8);
            drawNumber(matrices, f, "☀" + skyLight, color, 19, 18);
        } else
            drawNumber(matrices, f, String.valueOf(blockLight), color, 8, 8);

        matrices.pop();
    }

    private static void drawNumber(MatrixStack matrices, TextRenderer f, String str, int color, int offsetX, int offsetY) {
        int w = f.getWidth(str);

        matrices.translate(offsetX - w / 2f, offsetY + 1 - f.fontHeight / 2f, 0);

        f.draw(matrices, str, 0, 0, color);
    }
}
