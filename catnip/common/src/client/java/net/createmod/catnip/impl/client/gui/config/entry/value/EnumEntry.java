package net.createmod.catnip.impl.client.gui.config.entry.value;

import java.util.Locale;
import java.util.function.Consumer;

import net.createmod.catnip.api.client.gui.UIRenderHelper;
import net.createmod.catnip.api.client.gui.element.BoxElement;
import net.createmod.catnip.api.client.gui.element.DelegatedStencilElement;
import net.createmod.catnip.api.client.gui.element.TextStencilElement;
import net.createmod.catnip.api.client.gui.texture.CatnipGuiTextures;
import net.createmod.catnip.api.client.gui.widget.BoxWidget;
import net.createmod.catnip.impl.config.ConfigHelper;
import net.createmod.catnip.api.config.ConfigPath;
import net.createmod.catnip.api.config.access.ConfigAccess;
import net.createmod.catnip.impl.client.gui.config.entry.list.ConfigEntryList;
import net.createmod.catnip.impl.client.gui.config.entry.ValueEntry;
import net.minecraft.client.gui.GuiGraphicsExtractor;

import net.minecraft.client.gui.components.events.GuiEventListener;

public final class EnumEntry extends ValueEntry<Enum<?>> {
	private static final int cycleWidth = 34;

	private final TextStencilElement valueText;
	private final BoxWidget cycleLeft;
	private final BoxWidget cycleRight;

	public EnumEntry(ConfigEntryList list, ConfigPath path, ConfigAccess.Value<Enum<?>> value) {
		super(list, path, value);

		valueText = new TextStencilElement(list.screen.getFont(), format(getValue())).centered(true, true);
		valueText.withElementRenderer((ms, width, height, _) -> UIRenderHelper.angledGradient(ms, 0, 0, height / 2,
			height, width, UIRenderHelper.COLOR_TEXT));

		DelegatedStencilElement l = CatnipGuiTextures.ICON_CONFIG_PREV.asStencil();
		cycleLeft = new BoxWidget(0, 0, cycleWidth + 8, 16)
			.withCustomBackground(BoxElement.COLOR_BACKGROUND_FLAT)
			.showingElement(l)
			.withCallback(() -> cycleValue(-1));
		l.withElementRenderer(BoxWidget.GRADIENT_FACTORY.apply(cycleLeft));
		cycleLeft.active = list.screen.isMutable();

		DelegatedStencilElement r = CatnipGuiTextures.ICON_CONFIG_NEXT.asStencil();
		cycleRight = new BoxWidget(0, 0, cycleWidth + 8, 16)
			.withCustomBackground(BoxElement.COLOR_BACKGROUND_FLAT)
			.showingElement(r)
			.withCallback(() -> cycleValue(1));
		r.at(cycleWidth - 8, 0);
		r.withElementRenderer(BoxWidget.GRADIENT_FACTORY.apply(cycleRight));
		cycleRight.active = list.screen.isMutable();
	}

	@Override
	protected void collectChildren(Consumer<GuiEventListener> output) {
		super.collectChildren(output);
		output.accept(this.cycleLeft);
		output.accept(this.cycleRight);
	}

	private void cycleValue(int direction) {
		Enum<?> e = getValue();
		Enum<?>[] options = e.getDeclaringClass()
			.getEnumConstants();
		e = options[Math.floorMod(e.ordinal() + direction, options.length)];
		setValue(e);
		bumpCog(direction * 15f);
	}

	@Override
	public void extractContent(GuiGraphicsExtractor graphics, int mouseX, int mouseY, boolean hovered, float partialTick) {
		super.extractContent(graphics, mouseX, mouseY, hovered, partialTick);

		cycleLeft.setX(getX() + getLabelWidth(getWidth()) + 4);
		cycleLeft.setY(getY() + 10);
		cycleLeft.extractRenderState(graphics, mouseX, mouseY, partialTick);

		valueText.at(cycleLeft.getX() + cycleWidth - 8, getY() + 10, 200)
			.withBounds(getWidth() - getLabelWidth(getWidth()) - 2 * cycleWidth - resetWidth - 4, 16)
			.submit(graphics);

		cycleRight.setX(getX() + getWidth() - cycleWidth * 2 - resetWidth + 10);
		cycleRight.setY(getY() + 10);
		cycleRight.extractRenderState(graphics, mouseX, mouseY, partialTick);

		new BoxElement()
			.withBackground(BoxElement.COLOR_BACKGROUND_FLAT)
			.flatBorder(0x01_000000)
			.withBounds(48, 6)
			.at(cycleLeft.getX() + 22, cycleLeft.getY() + 5)
			.submit(graphics);
	}

	@Override
	protected void onValueChange(Enum<?> newValue, ChangeCause cause) {
		super.onValueChange(newValue, cause);
		valueText.withText(format(newValue));
	}

	private static String format(Enum<?> value) {
		return ConfigHelper.toHumanReadable(value.name().toLowerCase(Locale.ROOT));
	}
}
