package net.createmod.ponder.mixin.client;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.mojang.blaze3d.platform.Window;
import net.createmod.catnip.gui.UIRenderHelper;

import net.minecraft.client.Minecraft;

@Mixin(Minecraft.class)
public class WindowResizeMixin {

	@Shadow @Final private Window window;

	@Inject(at = @At("TAIL"), method = "resizeDisplay")
	private void catnip$updateWindowSize(CallbackInfo ci) {
		UIRenderHelper.updateWindowSize(window);
	}

}
