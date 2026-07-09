package net.createmod.catnip.api.client.gui;

import java.util.List;

import org.lwjgl.glfw.GLFW;

import com.mojang.blaze3d.platform.InputConstants;

import net.createmod.catnip.api.client.animation.AnimationTickHolder;
import net.createmod.catnip.api.theme.Color;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;

public abstract class AbstractSimiScreen extends Screen implements CatnipScreenExtensions {
	protected static final Color BACKGROUND_COLOR = new Color(0x101010, false).setImmutable();

	protected int imageWidth;
	protected int imageHeight;
	protected int windowWidth;
	protected int windowHeight;
	protected int guiLeft;
	protected int guiTop;
	protected int windowXOffset;
	protected int windowYOffset;

	protected AbstractSimiScreen() {
		this(Component.empty());
	}

	protected AbstractSimiScreen(Component title) {
		super(title);
	}

	@Override
	public boolean isPauseScreen() {
		return false;
	}

	@Override
	public boolean shouldCloseOnE() {
		return true;
	}

	protected void setWindowSize(int width, int height) {
		imageWidth = width;
		imageHeight = height;
		windowWidth = width;
		windowHeight = height;
	}

	protected void setWindowOffset(int xOffset, int yOffset) {
		windowXOffset = xOffset;
		windowYOffset = yOffset;
	}

	@Override
	protected void init() {
		super.init();
		guiLeft = (width - imageWidth) / 2 + windowXOffset;
		guiTop = (height - imageHeight) / 2 + windowYOffset;
	}

	public int getGuiLeft() {
		return guiLeft;
	}

	public int getGuiTop() {
		return guiTop;
	}

	@Override
	public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTicks) {
		partialTicks = AnimationTickHolder.getGuiPartialTicks();
		prepareFrame();
		renderWindowBackground(graphics, mouseX, mouseY, partialTicks);
		super.extractRenderState(graphics, mouseX, mouseY, partialTicks);
		renderWindow(graphics, mouseX, mouseY, partialTicks);
		renderWindowForeground(graphics, mouseX, mouseY, partialTicks);
		endFrame();
	}

	protected void prepareFrame() {
	}

	protected void endFrame() {
	}

	protected void renderWindowBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTicks) {
		renderBackground(graphics, mouseX, mouseY, partialTicks);
	}

	protected void renderWindow(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTicks) {
	}

	protected void renderWindowForeground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTicks) {
	}

	protected void renderTooltip(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
	}

	public void renderBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTicks) {
	}

	@Override
	public boolean keyPressed(KeyEvent event) {
		return keyPressed(event.key(), event.scancode(), event.modifiers()) || super.keyPressed(event);
	}

	public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
		return false;
	}

	@Override
	public boolean keyReleased(KeyEvent event) {
		return keyReleased(event.key(), event.scancode(), event.modifiers()) || super.keyReleased(event);
	}

	public boolean keyReleased(int keyCode, int scanCode, int modifiers) {
		return false;
	}

	@Override
	public boolean charTyped(CharacterEvent event) {
		return charTyped((char) event.codepoint(), 0) || super.charTyped(event);
	}

	public boolean charTyped(char codePoint, int modifiers) {
		return false;
	}

	@Override
	public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
		return mouseClicked(event.x(), event.y(), event.button()) || super.mouseClicked(event, doubleClick);
	}

	public boolean mouseClicked(double mouseX, double mouseY, int button) {
		return false;
	}

	@Override
	public boolean mouseReleased(MouseButtonEvent event) {
		return mouseReleased(event.x(), event.y(), event.button()) || super.mouseReleased(event);
	}

	public boolean mouseReleased(double mouseX, double mouseY, int button) {
		return false;
	}

	@Override
	public boolean mouseDragged(MouseButtonEvent event, double dragX, double dragY) {
		return mouseDragged(event.x(), event.y(), event.button(), dragX, dragY) || super.mouseDragged(event, dragX, dragY);
	}

	public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
		return false;
	}

	public Minecraft getMinecraft() {
		return minecraft;
	}

	public boolean hasControlDown() {
		return InputConstants.isKeyDown(minecraft.getWindow(), GLFW.GLFW_KEY_LEFT_CONTROL)
			|| InputConstants.isKeyDown(minecraft.getWindow(), GLFW.GLFW_KEY_RIGHT_CONTROL);
	}

	public boolean hasShiftDown() {
		return InputConstants.isKeyDown(minecraft.getWindow(), GLFW.GLFW_KEY_LEFT_SHIFT)
			|| InputConstants.isKeyDown(minecraft.getWindow(), GLFW.GLFW_KEY_RIGHT_SHIFT);
	}

	protected final List<Renderable> getRenderables() {
		return List.of();
	}

	protected <T extends AbstractWidget> void addRenderableWidgets(List<T> widgets) {
		widgets.forEach(this::addRenderableWidget);
	}

	protected <T extends AbstractWidget> void removeWidgets(List<T> widgets) {
		widgets.forEach(this::removeWidget);
	}
}
