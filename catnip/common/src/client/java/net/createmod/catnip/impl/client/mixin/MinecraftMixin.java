package net.createmod.catnip.impl.client.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;

import net.createmod.catnip.api.client.gui.TickableGuiEventListener;
import net.minecraft.client.Minecraft;

import net.minecraft.client.gui.components.events.ContainerEventHandler;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(Minecraft.class)
public class MinecraftMixin {
	@WrapOperation(
		method = "tick",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/client/gui/screens/Screen;tick()V"
		)
	)
	private void wrapScreenTick(Screen screen, Operation<Void> original) {
		original.call(screen);
		tickChildren(screen);
	}

	@Unique
	private static void tickChildren(ContainerEventHandler parent) {
		for (GuiEventListener child : parent.children()) {
			if (child instanceof TickableGuiEventListener tickable) {
				tickable.tick();
			}

			if (child instanceof ContainerEventHandler eventHandler) {
				tickChildren(eventHandler);
			}
		}
	}
}
