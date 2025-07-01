package net.createmod.catnip.render.compat;

import com.mojang.blaze3d.vertex.VertexFormat;

import dev.engine_room.flywheel.lib.util.ShadersModHelper;
import net.irisshaders.iris.shadows.ShadowRenderer;

import org.joml.Matrix4f;

import java.lang.reflect.Field;

public final class IrisCompat {
	private IrisCompat() {
	}

	public static final boolean IS_IRIS_INSTALLED = ShadersModHelper.IS_IRIS_LOADED;

	static VertexFormat GetTerrainVertexFormat() {
		try {
			Class<?> irisVertexFormats = Class.forName("net.irisshaders.iris.vertices.IrisVertexFormats");
			Field field = irisVertexFormats.getDeclaredField("TERRAIN");
			return (VertexFormat) field.get(null);
		} catch (Exception e) {
			return null;
		}
	}

	static VertexFormat GetEntityVertexFormat() {
		try {
			Class<?> irisVertexFormats = Class.forName("net.irisshaders.iris.vertices.IrisVertexFormats");
			Field field = irisVertexFormats.getDeclaredField("ENTITY");
			return (VertexFormat) field.get(null);
		} catch (Exception e) {
			return null;
		}
	}

	public static boolean isShadowPass() {
		return ShadowRenderer.ACTIVE;
	}

	public static Matrix4f getShadowMV() {
		return ShadowRenderer.MODELVIEW;
	}
}
