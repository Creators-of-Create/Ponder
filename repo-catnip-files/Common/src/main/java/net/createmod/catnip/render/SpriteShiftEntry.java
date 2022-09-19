package net.createmod.catnip.render;

import java.util.function.Function;

import javax.annotation.Nullable;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.InventoryMenu;

public class SpriteShiftEntry {
	protected final ResourceLocation originalLocation;
	protected final ResourceLocation targetLocation;
	@Nullable protected TextureAtlasSprite original;
	@Nullable protected TextureAtlasSprite target;

	public SpriteShiftEntry(ResourceLocation originalTextureLocation, ResourceLocation targetTextureLocation) {
		this.originalLocation = originalTextureLocation;
		this.targetLocation = targetTextureLocation;
	}

	public ResourceLocation getOriginalResourceLocation() {
		return originalLocation;
	}

	public ResourceLocation getTargetResourceLocation() {
		return targetLocation;
	}

	public TextureAtlasSprite getOriginal() {
		if (original == null)
			loadTextures();

		return original;
	}

	public TextureAtlasSprite getTarget() {
		if (target == null)
			loadTextures();

		return target;
	}

	public float getTargetU(float localU) {
		return getTarget().getU(getUnInterpolatedU(getOriginal(), localU));
	}

	public float getTargetV(float localV) {
		return getTarget().getV(getUnInterpolatedV(getOriginal(), localV));
	}

	protected void loadTextures() {
		Function<ResourceLocation, TextureAtlasSprite> textureMap = Minecraft.getInstance().getTextureAtlas(InventoryMenu.BLOCK_ATLAS);

		original = textureMap.apply(originalLocation);
		target = textureMap.apply(targetLocation);
	}

	public void loadTextures(TextureAtlas atlas) {
		original = atlas.getSprite(originalLocation);
		target = atlas.getSprite(targetLocation);
	}

	public static float getUnInterpolatedU(TextureAtlasSprite sprite, float u) {
		float f = sprite.getU1() - sprite.getU0();
		return (u - sprite.getU0()) / f * 16.0F;
	}

	public static float getUnInterpolatedV(TextureAtlasSprite sprite, float v) {
		float f = sprite.getV1() - sprite.getV0();
		return (v - sprite.getV0()) / f * 16.0F;
	}
}
