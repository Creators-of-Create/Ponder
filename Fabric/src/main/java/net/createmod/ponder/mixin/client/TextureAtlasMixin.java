package net.createmod.ponder.mixin.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.createmod.catnip.render.StitchedSprite;
import net.minecraft.client.renderer.texture.SpriteLoader;
import net.minecraft.client.renderer.texture.TextureAtlas;

@Mixin(TextureAtlas.class)
public class TextureAtlasMixin {
	@Inject(method = "upload(Lnet/minecraft/client/renderer/texture/SpriteLoader$Preparations;)V", at = @At("TAIL"))
	private void onTailReload(SpriteLoader.Preparations preparations, CallbackInfo ci) {
		StitchedSprite.onTextureStitchPost((TextureAtlas) (Object) this);
	}
}
