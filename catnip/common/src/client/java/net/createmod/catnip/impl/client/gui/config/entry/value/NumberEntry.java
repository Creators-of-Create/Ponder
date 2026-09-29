package net.createmod.catnip.impl.client.gui.config.entry.value;

import java.util.Locale;
import java.util.Optional;

import net.createmod.catnip.api.config.ConfigPath;
import net.createmod.catnip.api.config.access.ConfigAccess;
import net.createmod.catnip.api.config.attribute.ConfigAttribute;
import net.createmod.catnip.api.config.attribute.ConfigAttribute.IntDisplay;
import net.createmod.catnip.api.config.attribute.ConfigAttributes;
import net.createmod.catnip.impl.client.gui.config.entry.list.ConfigEntryList;
import net.createmod.catnip.impl.client.gui.config.entry.ValueEntry;
import net.minecraft.client.gui.components.EditBox;

import org.jspecify.annotations.Nullable;

import net.createmod.catnip.api.client.gui.UIRenderHelper;
import net.createmod.catnip.api.client.gui.element.TextStencilElement;
import net.createmod.catnip.api.client.gui.widget.AbstractSimiWidget;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

public abstract class NumberEntry<T extends Number> extends ValueEntry<T> {
	private final @Nullable BoundLabel minLabel;
	private final @Nullable BoundLabel maxLabel;
	protected final EditBox textField;

	protected NumberEntry(ConfigEntryList list, ConfigPath path, ConfigAccess.Value<T> value) {
		super(list, path, value);
		Font font = list.screen.getFont();

		// FIXME: translation
		MutableComponent narration = Component.literal("Edit box for " + this.name());
		textField = new EditBox(font, 0, 0, 200, 20, narration);
		textField.setTextColor(UIRenderHelper.COLOR_TEXT.getFirst().getRGB());
		textField.moveCursorToStart(false);
		textField.setResponder(s -> {
			try {
				T number = getParser().parse(s);
				if (value.validate(number).isPresent())
					throw new IllegalArgumentException();

				textField.setTextColor(UIRenderHelper.COLOR_TEXT.getFirst().getRGB());
				setValue(number);
			} catch (IllegalArgumentException ignored) {
				textField.setTextColor(AbstractSimiWidget.COLOR_FAIL.getFirst().getRGB());
			}
		});

		ConfigAttributes attributes = value.attributes();

		// if the value is an integer, set the initial formatting.
		// the field will still accept any format though.
		Optional<IntDisplay> intDisplay = attributes.find(IntDisplay.class);
		if (this instanceof IntegerEntry && intDisplay.isPresent()) {
			int intValue = (Integer) getValue();
			String textValue = switch (intDisplay.get()) {
				case HEX -> "#" + Integer.toHexString(intValue).toUpperCase(Locale.ROOT);
				case ZERO_X -> "0x" + Integer.toHexString(intValue).toUpperCase(Locale.ROOT);
				case ZERO_B -> "0b" + Integer.toBinaryString(intValue);
			};
			textField.setValue(textValue);
		} else {
			textField.setValue(String.valueOf(getValue()));
		}

		this.minLabel = attributes.find(ConfigAttribute.Minimum.class)
			.map(min -> min.value().doubleValue())
			.filter(min -> min > this.getTypeMin().doubleValue())
			.map(min -> {
				MutableComponent t = Component.literal(formatBound(min) + " < ");
				TextStencilElement text = new TextStencilElement(font, t).centered(true, false);
				text.withElementRenderer((ms, width, height, _) -> UIRenderHelper.angledGradient(ms, 0, 0, height / 2, height, width, UIRenderHelper.COLOR_TEXT_DARKER));
				return new BoundLabel(text, font.width(t));
			}).orElse(null);

		this.maxLabel = attributes.find(ConfigAttribute.Maximum.class)
			.map(max -> max.value().doubleValue())
			.filter(max -> max < this.getTypeMax().doubleValue())
			.map(max -> {
				MutableComponent t = Component.literal(" < " + formatBound(max));
				TextStencilElement text = new TextStencilElement(font, t).centered(true, false);
				text.withElementRenderer((ms, width, height, _) -> UIRenderHelper.angledGradient(ms, 0, 0, height / 2, height, width, UIRenderHelper.COLOR_TEXT_DARKER));
				return new BoundLabel(text, font.width(t));
			}).orElse(null);
	}

	protected abstract T getTypeMin();

	protected abstract T getTypeMax();

	protected abstract Parser<T> getParser();

	@Override
	protected void onValueChange(T newValue, ChangeCause cause) {
		super.onValueChange(newValue, cause);

		try {
			T current = getParser().parse(textField.getValue());
			if (!current.equals(newValue)) {
				textField.setValue(String.valueOf(newValue));
			}
		} catch (IllegalArgumentException ignored) {}
	}

	@Override
	public void extractContent(GuiGraphicsExtractor graphics, int mouseX, int mouseY, boolean hovered, float partialTick) {
		super.extractContent(graphics, mouseX, mouseY, hovered, partialTick);

		int minOffset = this.minLabel == null ? 0 : this.minLabel.offset;
		int maxOffset = this.maxLabel == null ? 0 : this.maxLabel.offset;
		textField.setX(getX() + getWidth() - 82 - resetWidth);
		textField.setY(getY() + 8);
		textField.setWidth(Math.min(getWidth() - getLabelWidth(getWidth()) - resetWidth - minOffset - maxOffset, 40));
		textField.setHeight(20);
		textField.extractWidgetRenderState(graphics, mouseX, mouseY, partialTick);

		if (this.minLabel != null) {
			this.minLabel.text
				.at(textField.getX() - minOffset, textField.getY(), 0)
				.withBounds(minOffset, textField.getHeight())
				.submit(graphics);
		}

		if (this.maxLabel != null) {
			this.maxLabel.text
				.at(textField.getX() + textField.getWidth(), textField.getY(), 0)
				.withBounds(maxOffset, textField.getHeight())
				.submit(graphics);
		}
	}

	private static String formatBound(double bound) {
		String sci = String.format("%.2E", bound);
		String str = String.valueOf(bound);
		return sci.length() < str.length() ? sci : str;
	}

	@FunctionalInterface
	protected interface Parser<T extends Number> {
		T parse(String input) throws IllegalArgumentException;
	}

	private record BoundLabel(TextStencilElement text, int offset) {}

	public static final class IntegerEntry extends NumberEntry<Integer> {
		public IntegerEntry(ConfigEntryList list, ConfigPath path, ConfigAccess.Value<Integer> value) {
			super(list, path, value);
		}

		@Override
		protected Integer getTypeMin() {
			return Integer.MIN_VALUE;
		}

		@Override
		protected Integer getTypeMax() {
			return Integer.MAX_VALUE;
		}

		@Override
		protected Parser<Integer> getParser() {
			return (string) -> {
				if (string.startsWith("#")) {
					return Integer.parseUnsignedInt(string.substring(1), 16);
				} else if (string.startsWith("0x")) {
					return Integer.parseUnsignedInt(string.substring(2), 16);
				} else if (string.startsWith("0b")) {
					return Integer.parseUnsignedInt(string.substring(2), 2);
				} else {
					return Integer.parseInt(string);
				}
			};
		}
	}

	public static final class FloatEntry extends NumberEntry<Float> {
		public FloatEntry(ConfigEntryList list, ConfigPath path, ConfigAccess.Value<Float> value) {
			super(list, path, value);
		}

		@Override
		protected Float getTypeMin() {
			return -Float.MAX_VALUE;
		}

		@Override
		protected Float getTypeMax() {
			return Float.MAX_VALUE;
		}

		@Override
		protected Parser<Float> getParser() {
			return Float::parseFloat;
		}
	}

	public static final class DoubleEntry extends NumberEntry<Double> {

		public DoubleEntry(ConfigEntryList list, ConfigPath path, ConfigAccess.Value<Double> value) {
			super(list, path, value);
		}

		@Override
		protected Double getTypeMin() {
			return -Double.MAX_VALUE;
		}

		@Override
		protected Double getTypeMax() {
			return Double.MAX_VALUE;
		}

		@Override
		protected Parser<Double> getParser() {
			return Double::parseDouble;
		}
	}
}
