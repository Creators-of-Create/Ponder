package net.createmod.catnip.render.compat;

import com.mojang.blaze3d.vertex.VertexFormat;

import net.caffeinemc.mods.sodium.api.vertex.attributes.common.ColorAttribute;
import net.caffeinemc.mods.sodium.api.vertex.attributes.common.LightAttribute;
import net.caffeinemc.mods.sodium.api.vertex.attributes.common.NormalAttribute;
import net.caffeinemc.mods.sodium.api.vertex.attributes.common.PositionAttribute;
import net.caffeinemc.mods.sodium.api.vertex.attributes.common.TextureAttribute;

import net.irisshaders.iris.uniforms.CapturedRenderingState;

import org.lwjgl.system.MemoryUtil;

public class IrisTerrainVertex {
	public static final VertexFormat FORMAT;
	public static final int STRIDE = 52;

	public IrisTerrainVertex() {
	}

	public static void write(long ptr, float x, float y, float z, int color, float u, float v, float mid_u, float mid_v, int light, int normal, int tangent) {
		PositionAttribute.put(ptr + 0L, x, y, z);
		ColorAttribute.set(ptr + 12L, color);
		TextureAttribute.put(ptr + 16L, u, v);
		LightAttribute.set(ptr + 24L, light);
		NormalAttribute.set(ptr + 28L, normal);
		MemoryUtil.memPutShort(ptr + 32L, (short) CapturedRenderingState.INSTANCE.getCurrentRenderedEntity());
		MemoryUtil.memPutShort(ptr + 34L, (short) CapturedRenderingState.INSTANCE.getCurrentRenderedBlockEntity());
		MemoryUtil.memPutFloat(ptr + 36L, mid_u);
		MemoryUtil.memPutFloat(ptr + 40L, mid_v);
		MemoryUtil.memPutInt(ptr + 44L, tangent);
		MemoryUtil.memPutInt(ptr + 48L, 0);
	}

	static {
		FORMAT = SodiumCompat.IS_SODIUM_INSTALLED && IrisCompat.IS_IRIS_INSTALLED ? IrisCompat.GetTerrainVertexFormat() : null;
	}
}
