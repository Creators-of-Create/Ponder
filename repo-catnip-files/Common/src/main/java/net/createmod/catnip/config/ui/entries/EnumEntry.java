package net.createmod.catnip.config.ui.entries;

import java.util.Locale;

import com.mojang.blaze3d.vertex.PoseStack;

import net.createmod.catnip.config.ui.ConfigScreen;
import net.createmod.catnip.enums.CatnipGuiTextures;
import net.createmod.catnip.gui.UIRenderHelper;
import net.createmod.catnip.gui.element.BoxElement;
import net.createmod.catnip.gui.element.DelegatedStencilElement;
import net.createmod.catnip.gui.element.TextStencilElement;
import net.createmod.catnip.gui.widget.BoxWidget;
import net.createmod.catnip.utility.theme.Theme;
import net.minecraft.client.Minecraft;
import net.minecraftforge.common.ForgeConfigSpec;

public class EnumEntry extends ValueEntry<Enum<?>> {

	protected static final int cycleWidth = 34;

	protected TextStencilElement valueText;
	protected BoxWidget cycleLeft;
	protected BoxWidget cycleRight;

	public EnumEntry(String label, ForgeConfigSpec.ConfigValue<Enum<?>> value, ForgeConfigSpec.ValueSpec spec) {
		super(label, value, spec);

		valueText = new TextStencilElement(Minecraft.getInstance().font, "YEP").centered(true, true);
		valueText.withElementRenderer((ms, width, height, alpha) -> UIRenderHelper.angledGradient(ms, 0, 0, height / 2,
			height, width, Theme.Key.TEXT.p()));

		DelegatedStencilElement l = CatnipGuiTextures.ICON_CONFIG_PREV.asStencil();
		cycleLeft = new BoxWidget(0, 0, cycleWidth + 8, 16)
				.withCustomBackground(Theme.Key.BOX_BACKGROUND_FLAT.c())
				.showingElement(l)
				.withCallback(() -> cycleValue(-1));
		l.withElementRenderer(BoxWidget.gradientFactory.apply(cycleLeft));

		DelegatedStencilElement r = CatnipGuiTextures.ICON_CONFIG_NEXT.asStencil();
		cycleRight = new BoxWidget(0, 0, cycleWidth + 8, 16)
				.withCustomBackground(Theme.Key.BOX_BACKGROUND_FLAT.c())
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
	public void render(PoseStack ms, int index, int y, int x, int width, int height, int mouseX, int mouseY,
		boolean p_230432_9_, float partialTicks) {
		super.render(ms, index, y, x, width, height, mouseX, mouseY, p_230432_9_, partialTicks);

		cycleLeft.x = x + getLabelWidth(width) + 4;
		cycleLeft.y = y + 10;
		cycleLeft.render(ms, mouseX, mouseY, partialTicks);

		valueText.at(cycleLeft.x + cycleWidth - 8, y + 10, 200)
				.withBounds(width - getLabelWidth(width) - 2 * cycleWidth - resetWidth - 4, 16)
				.render(ms);

		cycleRight.x = x + width - cycleWidth * 2 - resetWidth + 10;
		cycleRight.y = y + 10;
		cycleRight.render(ms, mouseX, mouseY, partialTicks);

		new BoxElement()
				.withBackground(Theme.Key.BOX_BACKGROUND_FLAT.c())
				.flatBorder(0x01_000000)
				.withBounds(48, 6)
				.at(cycleLeft.x + 22, cycleLeft.y + 5)
				.render(ms);
	}

	@Override
	public void onValueChange(Enum<?> newValue) {
		super.onValueChange(newValue);
		valueText.withText(ConfigScreen.toHumanReadable(newValue.name().toLowerCase(Locale.ROOT)));
	}
}
