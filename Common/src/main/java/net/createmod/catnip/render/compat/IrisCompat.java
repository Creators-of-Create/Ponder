package net.createmod.catnip.render.compat;

import com.mojang.blaze3d.vertex.VertexFormat;

import dev.engine_room.flywheel.lib.util.ShadersModHelper;

import java.lang.reflect.Field;

public final class IrisCompat {
	private IrisCompat() {
	}

	public static final boolean IS_IRIS_INSTALLED = ShadersModHelper.IS_IRIS_LOADED;

	static VertexFormat GetTerrainVertexFormat() {
		try {
			Class<?> irisVertexFormats = Class.forName("net.irisshaders.iris.vertices.IrisVertexFormats");
			Field field = irisVertexFormats.getDeclaredField("TERRAIN");
			field.setAccessible(true);
			return (VertexFormat) field.get(null);
		} catch (Exception e) {
			return null;
		}
	}
}
