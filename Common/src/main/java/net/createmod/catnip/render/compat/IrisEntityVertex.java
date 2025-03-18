package net.createmod.catnip.render.compat;

import com.mojang.blaze3d.vertex.VertexFormat;

import net.caffeinemc.mods.sodium.api.vertex.attributes.common.ColorAttribute;
import net.caffeinemc.mods.sodium.api.vertex.attributes.common.LightAttribute;
import net.caffeinemc.mods.sodium.api.vertex.attributes.common.NormalAttribute;
import net.caffeinemc.mods.sodium.api.vertex.attributes.common.OverlayAttribute;
import net.caffeinemc.mods.sodium.api.vertex.attributes.common.PositionAttribute;
import net.caffeinemc.mods.sodium.api.vertex.attributes.common.TextureAttribute;

import org.lwjgl.system.MemoryUtil;

public class IrisEntityVertex {
	public static final VertexFormat FORMAT;
	public static final int STRIDE = 54;

	public IrisEntityVertex() {
	}

	public static void write(long ptr, float x, float y, float z, int color, float u, float v, float mid_u, float mid_v, int overlay, int light, int normal, int tangent) {
		PositionAttribute.put(ptr + 0L, x, y, z);
		ColorAttribute.set(ptr + 12L, color);
		TextureAttribute.put(ptr + 16L, u, v);
		OverlayAttribute.set(ptr + 24L, overlay);
		LightAttribute.set(ptr + 28L, light);
		NormalAttribute.set(ptr + 32L, normal);
		MemoryUtil.memPutFloat(ptr + 42, mid_u);
		MemoryUtil.memPutFloat(ptr + 46, mid_v);
		MemoryUtil.memPutInt(ptr + 50, tangent);
	}

	static {
		FORMAT = SodiumCompat.IS_SODIUM_INSTALLED && IrisCompat.IS_IRIS_INSTALLED ? IrisCompat.GetEntityVertexFormat() : null;
	}
}
