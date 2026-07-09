package net.createmod.ponder.render;

import net.neoforged.neoforge.model.data.ModelData;
import net.neoforged.neoforge.model.data.ModelProperty;

public class VirtualRenderHelper {
	public static final ModelProperty<Boolean> VIRTUAL_PROPERTY = new ModelProperty<>();
	public static final ModelData VIRTUAL_DATA = ModelData.builder()
		.with(VIRTUAL_PROPERTY, true)
		.build();

	public static boolean isVirtual(ModelData data) {
		return data != null && data.has(VIRTUAL_PROPERTY) && Boolean.TRUE.equals(data.get(VIRTUAL_PROPERTY));
	}
}
