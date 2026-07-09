package net.createmod.catnip.api.client.render;

import net.createmod.catnip.api.Catnip;
import net.createmod.catnip.api.client.gui.texture.CatnipSpecialTextures;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.resources.Identifier;

public abstract class PonderRenderTypes {
	private static final RenderType GUI = RenderTypes.itemTranslucent(CatnipSpecialTextures.BLANK.getId());
	private static final RenderType OUTLINE_SOLID = RenderTypes.entitySolid(CatnipSpecialTextures.BLANK.getId());

	public static RenderType gui() {
		return GUI;
	}

	public static RenderType outlineSolid() {
		return OUTLINE_SOLID;
	}

	public static RenderType outlineTranslucent(Identifier texture, boolean cull) {
		return cull ? RenderTypes.entityTranslucentCullItemTarget(texture) : RenderTypes.entityTranslucent(texture);
	}

	private static String createLayerName(String name) {
		return Catnip.ID + ":" + name;
	}
}
