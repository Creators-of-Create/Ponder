package net.createmod.catnip.impl.client.gui.config.entry;

import java.util.Optional;
import java.util.function.Consumer;

import net.createmod.catnip.api.animation.LerpedFloat;
import net.createmod.catnip.api.animation.LerpedFloat.Chaser;
import net.createmod.catnip.api.client.gui.UIRenderHelper;
import net.createmod.catnip.api.client.gui.texture.CatnipGuiTextures;
import net.createmod.catnip.api.config.ConfigPath;
import net.createmod.catnip.api.config.access.ConfigAccess;
import net.createmod.catnip.api.config.attribute.ConfigAttribute;
import net.createmod.catnip.api.config.attribute.ConfigAttribute.Units;
import net.createmod.catnip.api.config.attribute.ConfigAttributes;
import net.createmod.catnip.api.theme.Color;
import net.createmod.catnip.impl.client.gui.config.entry.list.ConfigEntryList;

import net.createmod.catnip.impl.client.gui.config.entry.value.BooleanEntry;
import net.createmod.catnip.impl.client.gui.config.entry.value.EnumEntry;
import net.createmod.catnip.impl.client.gui.config.entry.value.NumberEntry;
import net.createmod.catnip.impl.client.gui.config.entry.value.StringEntry;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.events.GuiEventListener;

import com.mojang.blaze3d.platform.InputConstants;

import net.createmod.catnip.impl.client.gui.config.AbstractConfigScreen;
import net.createmod.catnip.api.client.gui.element.DelegatedStencilElement;
import net.createmod.catnip.api.client.gui.widget.BoxWidget;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.input.MouseButtonEvent;

import org.jspecify.annotations.Nullable;

public abstract class ValueEntry<T> extends ConfigEntryList.Entry {
	protected static final int resetWidth = 28; // including 6px offset on either side

	private final ConfigAccess.Value<T> value;
	private final LerpedFloat differenceAnimation;
	private final BoxWidget resetButton;

	private final boolean requiresRestart;
	private final boolean requiresRelog;
	private final @Nullable String unit;

	public ValueEntry(ConfigEntryList list, ConfigPath path, ConfigAccess.Value<T> value) {
		super(list, path, value);
		this.value = value;
		this.differenceAnimation = LerpedFloat.linear().startWithValue(0);

		this.resetButton = new BoxWidget(0, 0, resetWidth - 12, 16)
			.showingElement(CatnipGuiTextures.ICON_CONFIG_RESET.asStencil())
			.withCallback(() -> {
				setValue(value.getDefault());
				this.onValueChange(getValue(), ChangeCause.SELF);
			});
		this.resetButton.modifyElement(e -> ((DelegatedStencilElement) e).withElementRenderer(BoxWidget.GRADIENT_FACTORY.apply(resetButton)));
		this.resetButton.active = list.screen.isMutable();

		ConfigAttributes attributes = value.attributes();
		this.requiresRestart = attributes.has(ConfigAttribute.Flag.REQUIRES_RESTART);
		this.requiresRelog = attributes.has(ConfigAttribute.Flag.REQUIRES_RELOG);
		this.unit = attributes.find(ConfigAttribute.Units.class).map(Units::value).orElse(null);
	}

	@Override
	protected void collectChildren(Consumer<GuiEventListener> output) {
		output.accept(this.resetButton);
	}

	@Override
	public void tick() {
		super.tick();
		this.differenceAnimation.tickChaser();
	}

	@Override
	public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
		if (super.mouseClicked(event, doubleClick))
			return true;

		if (isCtrlClick(event)) {
			Minecraft.getInstance().keyboardHandler.setClipboard(this.id.toString());
			this.highlight();
			return true;
		}

		return false;
	}

	@Override
	public void extractContent(GuiGraphicsExtractor graphics, int mouseX, int mouseY, boolean hovered, float partialTick) {
		if (this.isDirty() && this.differenceAnimation.getChaseTarget() != 1) {
			this.differenceAnimation.chase(1, .5f, Chaser.EXP);
		} else if (!this.isDirty() && this.differenceAnimation.getChaseTarget() != 0) {
			this.differenceAnimation.chase(0, .6f, Chaser.EXP);
		}

		float differenceAnimation = this.differenceAnimation.getValue(partialTick);
		if (differenceAnimation > 0.1) {
			int offset = (int) (30 * (1 - differenceAnimation));

			if (this.requiresRestart) {
				UIRenderHelper.streak(graphics, 180, getX() + getWidth() + 10 + offset, getY() + getHeight() / 2, getHeight() - 6, 110, new Color(0x50_601010));
			} else if (this.requiresRelog) {
				UIRenderHelper.streak(graphics, 180, getX() + getWidth() + 10 + offset, getY() + getHeight() / 2, getHeight() - 6, 110, new Color(0x40_eefb17));
			}

			UIRenderHelper.breadcrumbArrow(graphics, getX() - 10 - offset, getY() + 6, -20, 24, -18, new Color(0x70_ffffff), Color.TRANSPARENT_BLACK);
		}

		super.extractContent(graphics, mouseX, mouseY, hovered, partialTick);

		if (this.unit != null) {
			Font font = this.list.screen.getFont();
			int unitWidth = font.width(this.unit);
			graphics.text(font, this.unit, getX() + getLabelWidth(getWidth()) - unitWidth - 5, getY() + getHeight() / 2 + 2, UIRenderHelper.COLOR_TEXT_DARKER.getFirst().getRGB());
		}

		this.resetButton.setX(getX() + getWidth() - resetWidth + 6);
		this.resetButton.setX(getY() + 10);
		this.resetButton.extractRenderState(graphics, mouseX, mouseY, partialTick);
	}

	@Override
	protected final int labelYOffset() {
		return super.labelYOffset() + (this.unit == null ? 0 : 6);
	}

	protected final void setValue(T value) {
		this.list.screen.setValue(this.id.path(), value);
		this.onValueChange(value, ChangeCause.SELF);
	}

	@SuppressWarnings("unchecked")
	protected final T getValue() {
		return (T) this.list.screen.getValue(this.id.path()).orElseThrow(
			() -> new IllegalStateException("ValueEntry with no corresponding ConfigAccess.Value: " + this.id)
		);
	}

	protected final boolean isCurrentValueDefault() {
		Optional<Object> currentValue = this.list.screen.getValue(this.id.path());
		return this.value.getDefault().equals(currentValue);
	}

	public final void notifyOfValueChange() {
		onValueChange(getValue(), ChangeCause.SCREEN);
	}

	protected void onValueChange(T newValue, ChangeCause cause) {
		resetButton.active = !isCurrentValueDefault();
		resetButton.animateGradientFromState();
	}

	protected final void bumpCog(float force) {
		AbstractConfigScreen.bumpCog(force);
	}

	@SuppressWarnings("unchecked")
	public static <T> Optional<ValueEntry<?>> create(ConfigEntryList list, ConfigPath path, ConfigAccess.Value<T> value) {
		T reference = value.getDefault();

		return Optional.ofNullable(switch (reference) {
			case Boolean _ -> new BooleanEntry(list, path, (ConfigAccess.Value<Boolean>) value);
			case Integer _ -> new NumberEntry.IntegerEntry(list, path, (ConfigAccess.Value<Integer>) value);
			case Float _ -> new NumberEntry.FloatEntry(list, path, (ConfigAccess.Value<Float>) value);
			case Double _ -> new NumberEntry.DoubleEntry(list, path, (ConfigAccess.Value<Double>) value);
			case String _ -> new StringEntry(list, path, (ConfigAccess.Value<String>) value);
			case Enum<?> _ -> new EnumEntry(list, path, (ConfigAccess.Value<Enum<?>>) value);
			default -> null;
		});
	}

	private static boolean isCtrlClick(MouseButtonEvent event) {
		return event.button() == InputConstants.MOUSE_BUTTON_LEFT && (event.modifiers() & InputConstants.MOD_CONTROL) != 0;
	}

	protected enum ChangeCause {
		SELF, SCREEN
	}
}
