package net.createmod.catnip.config.ui;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.util.TriConsumer;
import org.lwjgl.opengl.GL30;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;

import net.createmod.catnip.gui.AbstractSimiScreen;
import net.createmod.catnip.gui.UIRenderHelper;
import net.createmod.catnip.gui.element.DelegatedStencilElement;
import net.createmod.catnip.gui.element.GuiGameElement;
import net.createmod.catnip.utility.animation.Force;
import net.createmod.catnip.utility.animation.PhysicalFloat;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.renderer.PanoramaRenderer;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

public abstract class ConfigScreen extends AbstractSimiScreen {

	public static final List<UnaryOperator<String>> displayNameKeys = List.of(
			modID -> "catnip." + modID + ".display_name",
			modID -> "constants." + modID + ".mod_name",
			modID -> "itemGroup." + modID + ".base",
			modID -> "itemGroup." + modID + "." + modID
	);

	public static final Map<String, String> knownModDisplayNames = Map.ofEntries(
			Map.entry("forge", "Forge"),
			Map.entry("jei", "Just Enough Items"),
			Map.entry("computercraft", "ComputerCraft"),
			Map.entry("catnip", "Catnip"),
			Map.entry("ponder", "Ponder"),
			Map.entry("create", "Create"),
			Map.entry("flywheel", "Flywheel"),
			Map.entry("ae2", "Applied Energistics 2")
	);

	public static final Map<String, TriConsumer<Screen, PoseStack, Float>> backgrounds = new HashMap<>();
	public static final PhysicalFloat cogSpin = PhysicalFloat.create().withLimit(10f).withDrag(0.3).addForce(new Force.Static(.2f));
	@Nullable public static String modID = null;
	@Nullable protected final Screen parent;

	public static BlockState shadowState = Blocks.POTTED_CRIMSON_ROOTS.defaultBlockState();
	public static DelegatedStencilElement shadowElement = new DelegatedStencilElement(
			(ps, x, y, alpha) -> renderCog(ps),
			(ps, x, y, alpha) -> fill(ps, -200, -200, 200, 200, 0x60_000000)
	);

	private static final PanoramaRenderer vanillaPanorama = new PanoramaRenderer(TitleScreen.CUBE_MAP);

	public ConfigScreen(@Nullable Screen parent) {
		this.parent = parent;
	}

	@Override
	public void tick() {
		super.tick();
		cogSpin.tick();
	}

	@Override
	public void renderBackground(@Nonnull PoseStack ms) {}

	@Override
	protected void renderWindowBackground(PoseStack ms, int mouseX, int mouseY, float partialTicks) {
		if (this.minecraft != null && this.minecraft.level != null) {
			//in game
			fill(ms, 0, 0, this.width, this.height, 0xb0_282c34);
		} else {
			//in menus
			renderMenuBackground(ms, partialTicks);
		}

		shadowElement
				.at(width * 0.5f, height * 0.5f, 0)
				.render(ms);

		super.renderWindowBackground(ms, mouseX, mouseY, partialTicks);

	}

	@Override
	protected void prepareFrame() {
		UIRenderHelper.swapAndBlitColor(minecraft.getMainRenderTarget(), UIRenderHelper.framebuffer);
		RenderSystem.clear(GL30.GL_STENCIL_BUFFER_BIT | GL30.GL_DEPTH_BUFFER_BIT, Minecraft.ON_OSX);
	}

	@Override
	protected void endFrame() {
		UIRenderHelper.swapAndBlitColor(UIRenderHelper.framebuffer, minecraft.getMainRenderTarget());
	}

	@Override
	protected void renderWindow(PoseStack ms, int mouseX, int mouseY, float partialTicks) {
	}

	@Override
	public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
		cogSpin.bump(3, -delta * 5);

		return super.mouseScrolled(mouseX, mouseY, delta);
	}

	@Override
	public boolean isPauseScreen() {
		return true;
	}

	/**
	 * This method checks some language keys to see if the mod has declared a display name via lang.
	 * If none of those succeed, it checks a list of manually declared ones for compatibility.
	 */
	public static Optional<String> getModDisplayName(String modID) {
		Optional<String> displayNameFromLang = displayNameKeys
				.stream()
				.map(op -> op.apply(modID))
				.filter(I18n::exists)
				.findFirst()
				.map(I18n::get);

		if (displayNameFromLang.isPresent())
			return displayNameFromLang;

		return Optional.ofNullable(knownModDisplayNames.get(modID));
	}

	public static String toHumanReadable(String key) {
		String s = key.replaceAll("_", " ");
		s = Arrays.stream(StringUtils.splitByCharacterTypeCamelCase(s)).map(StringUtils::capitalize).collect(Collectors.joining(" "));
		s = StringUtils.normalizeSpace(s);
		return s;
	}

	/**
	 * By default, ConfigScreens will render the Vanilla Panorama as
	 * their background when not opened ingame.
	 * If your mod wants to render something else, please add to the
	 * {@code backgrounds} Map in this Class with your modID as the key.
	 */
	protected void renderMenuBackground(PoseStack ms, float partialTicks) {
		TriConsumer<Screen, PoseStack, Float> customBackground = backgrounds.get(modID);
		if (customBackground != null) {
			customBackground.accept(this, ms, partialTicks);
			return;
		}

		vanillaPanorama.render(minecraft.getDeltaFrameTime(), 1);

		fill(ms, 0, 0, this.width, this.height, 0x90_282c34);
	}

	protected static void renderCog(PoseStack ms) {
		float partialTicks = Minecraft.getInstance().getFrameTime();
		ms.pushPose();

		ms.translate(-100, 100, -100);
		ms.scale(200, 200, 1);
		GuiGameElement.of(shadowState)
				.rotateBlock(22.5, cogSpin.getValue(partialTicks), 22.5)
				.render(ms);

		ms.popPose();
	}
}
