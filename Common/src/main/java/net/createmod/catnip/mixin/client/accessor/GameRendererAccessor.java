package net.createmod.catnip.mixin.client.accessor;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import net.minecraft.client.Camera;
import net.minecraft.client.renderer.GameRenderer;

@Mixin(GameRenderer.class)
public interface GameRendererAccessor {

	@Invoker("getFov")
	double catnip$callGetFov(Camera camera, float partialTicks, boolean useFOVSetting);
}
