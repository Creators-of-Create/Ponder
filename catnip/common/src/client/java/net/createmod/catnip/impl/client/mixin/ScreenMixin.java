package net.createmod.catnip.impl.client.mixin;

import net.createmod.catnip.api.client.gui.CatnipScreenExtensions;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;

import net.minecraft.client.input.KeyEvent;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Screen.class)
public abstract class ScreenMixin {
	@Shadow
	@Final
	protected Minecraft minecraft;

	@Shadow
	public abstract void onClose();

	@Inject(method = "keyPressed", at = @At("HEAD"), cancellable = true)
	private void maybeCloseOnE(KeyEvent event, CallbackInfoReturnable<Boolean> cir) {
		if (!(this instanceof CatnipScreenExtensions catnipScreen))
			return;

		if (catnipScreen.shouldCloseOnE() && this.minecraft.options.keyInventory.matches(event)) {
			onClose();
			cir.setReturnValue(true);
		}
	}
}
