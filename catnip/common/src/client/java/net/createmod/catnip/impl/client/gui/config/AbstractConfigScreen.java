package net.createmod.catnip.impl.client.gui.config;

import net.createmod.catnip.api.client.gui.element.StencilElement;
import net.minecraft.network.chat.Component;

import org.jetbrains.annotations.ApiStatus.Internal;
import org.joml.Matrix3x2fStack;
import org.jspecify.annotations.Nullable;

import net.createmod.catnip.api.animation.Force;
import net.createmod.catnip.api.animation.PhysicalFloat;
import net.createmod.catnip.api.client.gui.AbstractSimiScreen;
import net.createmod.catnip.api.client.gui.element.DelegatedStencilElement;
import net.createmod.catnip.api.client.gui.element.GuiGameElement;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.world.level.block.state.BlockState;

/// Common code shared between the screens used for the config system.
public abstract class AbstractConfigScreen extends AbstractSimiScreen {
	// static so it persists across screens
	private static final PhysicalFloat cogSpin = PhysicalFloat.create().withLimit(10f).withDrag(0.3).addForce(new Force.Static(.2f));

	protected final @Nullable Screen parent;
	protected final @Nullable StencilElement shadowElement;

	protected AbstractConfigScreen(Component title, @Nullable Screen parent, BlockState shadowBlock) {
		super(title);
		this.parent = parent;
		this.shadowElement = shadowBlock.isAir() ? null : new DelegatedStencilElement(
			(graphics, _, _, _) -> renderShadowBlock(graphics, shadowBlock),
			(graphics, _, _, _) -> graphics.fill(-200, -200, 200, 200, 0x60_000000)
		);
	}

	@Override
	protected void extractMenuBackground(GuiGraphicsExtractor graphics, int x, int y, int width, int height) {
		super.extractMenuBackground(graphics, x, y, width, height);

		if (this.shadowElement != null) {
			this.shadowElement
				.at(width * 0.5f, height * 0.5f, 0)
				.submit(graphics);
		}
	}

	@Override
	public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
		bumpCog((float) (-scrollY * 5));
		return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
	}

	@Override
	public boolean isPauseScreen() {
		return true;
	}

	public static void bumpCog(float force) {
		cogSpin.bump(3, force);
	}

	@Internal
	public static void tickCog() {
		cogSpin.tick();
	}

	private static void renderShadowBlock(GuiGraphicsExtractor graphics, BlockState state) {
		float partialTicks = Minecraft.getInstance().getDeltaTracker().getGameTimeDeltaPartialTick(false);
		Matrix3x2fStack poseStack = graphics.pose();
		poseStack.pushMatrix();

		poseStack.translate(-100, 100);
		poseStack.scale(200, 200);
		GuiGameElement.of(state)
			.rotateBlock(22.5, cogSpin.getValue(partialTicks), 22.5)
			.submit(graphics);

		poseStack.popMatrix();
	}
}
