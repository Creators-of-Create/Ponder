package net.createmod.catnip.api.client.config.entries;

import java.util.Locale;

import net.createmod.catnip.api.client.gui.UIRenderHelper;
import net.createmod.catnip.api.client.gui.element.BoxElement;
import net.createmod.catnip.api.client.gui.element.DelegatedStencilElement;
import net.createmod.catnip.api.client.gui.element.TextStencilElement;
import net.createmod.catnip.api.client.gui.widget.BoxWidget;
import net.createmod.catnip.api.client.config.ConfigScreen;
import net.createmod.catnip.api.client.gui.texture.CatnipGuiTextures;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;

import net.neoforged.neoforge.common.ModConfigSpec;

public class EnumEntry extends ValueEntry<Enum<?>> {

	protected static final int cycleWidth = 34;

	protected TextStencilElement valueText;
	protected BoxWidget cycleLeft;
	protected BoxWidget cycleRight;

	public EnumEntry(String label, ModConfigSpec.ConfigValue<Enum<?>> value, ModConfigSpec.ValueSpec spec) {
		super(label, value, spec);

		valueText = new TextStencilElement(Minecraft.getInstance().font, "YEP").centered(true, true);
		valueText.withElementRenderer((ms, width, height, alpha) -> UIRenderHelper.angledGradient(ms, 0, 0, height / 2,
			height, width, UIRenderHelper.COLOR_TEXT));

		DelegatedStencilElement l = CatnipGuiTextures.ICON_CONFIG_PREV.asStencil();
		cycleLeft = new BoxWidget(0, 0, cycleWidth + 8, 16)
			.withCustomBackground(BoxElement.COLOR_BACKGROUND_FLAT)
			.showingElement(l)
			.withCallback(() -> cycleValue(-1));
		l.withElementRenderer(BoxWidget.gradientFactory.apply(cycleLeft));

		DelegatedStencilElement r = CatnipGuiTextures.ICON_CONFIG_NEXT.asStencil();
		cycleRight = new BoxWidget(0, 0, cycleWidth + 8, 16)
			.withCustomBackground(BoxElement.COLOR_BACKGROUND_FLAT)
			.showingElement(r)
			.withCallback(() -> cycleValue(1));
		r.at(cycleWidth - 8, 0);
		r.withElementRenderer(BoxWidget.gradientFactory.apply(cycleRight));

		listeners.add(cycleLeft);
		listeners.add(cycleRight);

		onReset();
	}

	protected void cycleValue(int direction) {
		Enum<?> e = getValue();
		Enum<?>[] options = e.getDeclaringClass()
			.getEnumConstants();
		e = options[Math.floorMod(e.ordinal() + direction, options.length)];
		setValue(e);
		bumpCog(direction * 15f);
	}

	@Override
	protected void setEditable(boolean b) {
		super.setEditable(b);
		cycleLeft.active = b;
		cycleLeft.animateGradientFromState();
		cycleRight.active = b;
		cycleRight.animateGradientFromState();
	}

	@Override
	public void tick() {
		super.tick();
		cycleLeft.tick();
		cycleRight.tick();
	}

	@Override
	public void renderContent(GuiGraphicsExtractor graphics, int mouseX, int mouseY, boolean isHovering, float partialTick) {
		super.renderContent(graphics, mouseX, mouseY, isHovering, partialTick);

		cycleLeft.setX(getX() + getLabelWidth(getWidth()) + 4);
		cycleLeft.setY(getY() + 10);
		cycleLeft.render(graphics, mouseX, mouseY, partialTick);

		valueText.at(cycleLeft.getX() + cycleWidth - 8, getY() + 10, 200)
			.withBounds(getWidth() - getLabelWidth(getWidth()) - 2 * cycleWidth - resetWidth - 4, 16)
			.submit(graphics);

		cycleRight.setX(getX() + getWidth() - cycleWidth * 2 - resetWidth + 10);
		cycleRight.setY(getY() + 10);
		cycleRight.render(graphics, mouseX, mouseY, partialTick);

		new BoxElement()
			.withBackground(BoxElement.COLOR_BACKGROUND_FLAT)
			.flatBorder(0x01_000000)
			.withBounds(48, 6)
			.at(cycleLeft.getX() + 22, cycleLeft.getY() + 5)
			.submit(graphics);
	}

	@Override
	public void onValueChange(Enum<?> newValue) {
		super.onValueChange(newValue);
		valueText.withText(ConfigScreen.toHumanReadable(newValue.name().toLowerCase(Locale.ROOT)));
	}
}
