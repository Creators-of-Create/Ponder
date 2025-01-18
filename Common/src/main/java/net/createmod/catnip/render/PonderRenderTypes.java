package net.createmod.catnip.render;

import java.util.function.BiFunction;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;

import net.createmod.ponder.Ponder;
import net.createmod.ponder.enums.PonderSpecialTextures;
import net.createmod.ponder.mixin.client.accessor.RenderTypeAccessor;
import net.minecraft.Util;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;

public abstract class PonderRenderTypes extends RenderType {

	private static final RenderType OUTLINE_SOLID =
		RenderTypeAccessor.catnip$create(createLayerName("outline_solid"), DefaultVertexFormat.NEW_ENTITY, VertexFormat.Mode.QUADS, 256, false, false, RenderType.CompositeState.builder()
			.setShaderState(RENDERTYPE_ENTITY_SOLID_SHADER)
			.setTextureState(new RenderStateShard.TextureStateShard(PonderSpecialTextures.BLANK.getLocation(), false, false))
			.setCullState(CULL)
			.setLightmapState(LIGHTMAP)
			.setOverlayState(OVERLAY)
			.createCompositeState(false));

	private static final BiFunction<ResourceLocation, Boolean, RenderType> OUTLINE_TRANSLUCENT = Util.memoize((texture, cull) ->
		RenderTypeAccessor.catnip$create(createLayerName("outline_translucent" + (cull ? "_cull" : "")), DefaultVertexFormat.NEW_ENTITY, VertexFormat.Mode.QUADS, 256, false, true, CompositeState.builder()
			.setShaderState(cull ? RENDERTYPE_ENTITY_TRANSLUCENT_CULL_SHADER : RENDERTYPE_ENTITY_TRANSLUCENT_SHADER)
			.setTextureState(new TextureStateShard(texture, false, false))
			.setTransparencyState(TRANSLUCENT_TRANSPARENCY)
			.setCullState(cull ? CULL : NO_CULL)
			.setLightmapState(LIGHTMAP)
			.setOverlayState(OVERLAY)
			.setWriteMaskState(COLOR_WRITE)
			.createCompositeState(false)));

	private static final RenderType FLUID =
		RenderTypeAccessor.catnip$create(createLayerName("fluid"), DefaultVertexFormat.BLOCK, VertexFormat.Mode.QUADS, 256, false, true, RenderType.CompositeState.builder()
			.setShaderState(RENDERTYPE_TRANSLUCENT_SHADER)
			.setTextureState(BLOCK_SHEET_MIPPED)
			.setTransparencyState(TRANSLUCENT_TRANSPARENCY)
			.setLightmapState(LIGHTMAP)
			//.setOverlayState(NO_OVERLAY)
			.createCompositeState(true));

	public static RenderType outlineSolid() {
		return OUTLINE_SOLID;
	}

	public static RenderType outlineTranslucent(ResourceLocation texture, boolean cull) {
		return OUTLINE_TRANSLUCENT.apply(texture, cull);
	}

	//TODO vanilla uses the translucent render type for fluids, need to investigate if this is even needed
	public static RenderType fluid() {
		return FLUID;
	}

	private static String createLayerName(String name) {
		return Ponder.MOD_ID + ":" + name;
	}

	private PonderRenderTypes(String name, VertexFormat format, VertexFormat.Mode mode, int bufferSize, boolean affectsCrumbling, boolean sortOnUpload, Runnable setupState, Runnable clearState) {
		super(name, format, mode, bufferSize, affectsCrumbling, sortOnUpload, setupState, clearState);
	}
}
