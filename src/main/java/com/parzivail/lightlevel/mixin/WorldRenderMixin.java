package com.parzivail.lightlevel.mixin;

import com.parzivail.lightlevel.LLWorldRender;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.lang.reflect.Field;

@Mixin(WorldRenderer.class)
public class WorldRenderMixin
{
	@Inject(at = @At("TAIL"), method = "render")
	private void init(MatrixStack matrices, float tickDelta, long limitTime, boolean renderBlockOutline, Camera camera, GameRenderer gameRenderer, LightmapTextureManager lightmapTextureManager, Matrix4f matrix4f, CallbackInfo info)
	{
		LLWorldRender.render(matrices, tickDelta, limitTime, renderBlockOutline, camera, gameRenderer, lightmapTextureManager, matrix4f, getFrustum((WorldRenderer)((Object)this)));
	}

	private Frustum getFrustum(WorldRenderer worldRenderer) {
		return ((FrustumAccessor)worldRenderer).getCapturedFrustum();
	}
}
