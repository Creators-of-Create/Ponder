package net.createmod.catnip.api.client.config.entries;

import java.lang.reflect.Field;
import java.util.Locale;
import java.util.function.Function;

import org.jspecify.annotations.Nullable;

import net.createmod.catnip.api.client.gui.UIRenderHelper;
import net.createmod.catnip.api.client.gui.element.TextStencilElement;
import net.createmod.catnip.api.client.gui.widget.AbstractSimiWidget;
import net.createmod.catnip.api.client.config.ConfigTextField;
import net.createmod.catnip.api.client.config.HintableTextFieldWidget;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

import net.neoforged.neoforge.common.ModConfigSpec;

public abstract class NumberEntry<T extends Number> extends ValueEntry<T> {

	@Nullable
	protected TextStencilElement minText = null, maxText = null;
	protected int minOffset = 0, maxOffset = 0;
	protected HintableTextFieldWidget textField;

	@Nullable
	public static NumberEntry<? extends Number> create(Object type, String label, ModConfigSpec.ConfigValue<?> value, ModConfigSpec.ValueSpec spec) {
		return switch (type) {
			case Integer i -> new IntegerEntry(label, (ModConfigSpec.ConfigValue<Integer>) value, spec);
			case Float v -> new FloatEntry(label, (ModConfigSpec.ConfigValue<Float>) value, spec);
			case Double v -> new DoubleEntry(label, (ModConfigSpec.ConfigValue<Double>) value, spec);
			default -> null;
		};

	}

	public NumberEntry(String label, ModConfigSpec.ConfigValue<T> value, ModConfigSpec.ValueSpec spec) {
		super(label, value, spec);
		textField = new ConfigTextField(Minecraft.getInstance().font, 0, 0, 200, 20);
		if (this instanceof IntegerEntry && annotations.containsKey("IntDisplay")) {
			String intDisplay = annotations.get("IntDisplay");
			int intValue = (Integer) getValue();
			String textValue = switch (intDisplay) {
				case "#" -> "#" + Integer.toHexString(intValue).toUpperCase(Locale.ROOT);
				case "0x" -> "0x" + Integer.toHexString(intValue).toUpperCase(Locale.ROOT);
				case "0b" -> "0b" + Integer.toBinaryString(intValue);
				default -> String.valueOf(intValue);
			};
			textField.setValue(textValue);
		} else {
			textField.setValue(String.valueOf(getValue()));
		}
		textField.setTextColor(UIRenderHelper.COLOR_TEXT.getFirst().getRGB());

		Object range = spec.getRange();
		try {
			Field minField = range.getClass().getDeclaredField("min");
			Field maxField = range.getClass().getDeclaredField("max");
			minField.setAccessible(true);
			maxField.setAccessible(true);
			T min = (T) minField.get(range);
			T max = (T) maxField.get(range);

			Font font = Minecraft.getInstance().font;
			if (min.doubleValue() > getTypeMin().doubleValue()) {
				MutableComponent t = Component.literal(formatBound(min) + " < ");
				minText = new TextStencilElement(font, t).centered(true, false);
				minText.withElementRenderer((ms, width, height, alpha) -> UIRenderHelper.angledGradient(ms, 0, 0, height / 2, height, width, UIRenderHelper.COLOR_TEXT_DARKER));
				minOffset = font.width(t);
			}
			if (max.doubleValue() < getTypeMax().doubleValue()) {
				MutableComponent t = Component.literal(" < " + formatBound(max));
				maxText = new TextStencilElement(font, t).centered(true, false);
				maxText.withElementRenderer((ms, width, height, alpha) -> UIRenderHelper.angledGradient(ms, 0, 0, height / 2, height, width, UIRenderHelper.COLOR_TEXT_DARKER));
				maxOffset = font.width(t);
			}
		} catch (NoSuchFieldException | IllegalAccessException | ClassCastException | NullPointerException ignored) {

		}

		textField.setResponder(s -> {
			try {
				T number = getParser().apply(s);
				if (!spec.test(number))
					throw new IllegalArgumentException();

				textField.setTextColor(UIRenderHelper.COLOR_TEXT.getFirst().getRGB());
				setValue(number);

			} catch (IllegalArgumentException ignored) {
				textField.setTextColor(AbstractSimiWidget.COLOR_FAIL.getFirst().getRGB());
			}
		});

		textField.moveCursorToStart(false);
		listeners.add(textField);
		onReset();
	}

	protected String formatBound(T bound) {
		String sci = String.format("%.2E", bound.doubleValue());
		String str = String.valueOf(bound);
		return sci.length() < str.length() ? sci : str;
	}

	protected abstract T getTypeMin();

	protected abstract T getTypeMax();

	protected abstract Function<String, T> getParser();

	@Override
	protected void setEditable(boolean b) {
		super.setEditable(b);
		textField.setEditable(b);
	}

	@Override
	public void onValueChange(T newValue) {
		super.onValueChange(newValue);

		try {
			T current = getParser().apply(textField.getValue());
			if (!current.equals(newValue)) {
				textField.setValue(String.valueOf(newValue));
			}
		} catch (IllegalArgumentException ignored) {
		}
	}

	@Override
	public void tick() {
		super.tick();
	}

	@Override
	public void renderContent(GuiGraphicsExtractor graphics, int mouseX, int mouseY, boolean isHovering, float partialTick) {
		super.renderContent(graphics, mouseX, mouseY, isHovering, partialTick);

		textField.setX(getX() + getWidth() - 82 - resetWidth);
		textField.setY(getY() + 8);
		textField.setWidth(Math.min(getWidth() - getLabelWidth(getWidth()) - resetWidth - minOffset - maxOffset, 40));
		textField.setHeight(20);
		textField.extractRenderState(graphics, mouseX, mouseY, partialTick);

		if (minText != null)
			minText
				.at(textField.getX() - minOffset, textField.getY(), 0)
				.withBounds(minOffset, textField.getHeight())
				.submit(graphics);

		if (maxText != null)
			maxText
				.at(textField.getX() + textField.getWidth(), textField.getY(), 0)
				.withBounds(maxOffset, textField.getHeight())
				.submit(graphics);
	}

	public static class IntegerEntry extends NumberEntry<Integer> {
		public IntegerEntry(String label, ModConfigSpec.ConfigValue<Integer> value, ModConfigSpec.ValueSpec spec) {
			super(label, value, spec);
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
		protected Function<String, Integer> getParser() {
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

	public static class FloatEntry extends NumberEntry<Float> {

		public FloatEntry(String label, ModConfigSpec.ConfigValue<Float> value, ModConfigSpec.ValueSpec spec) {
			super(label, value, spec);
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
		protected Function<String, Float> getParser() {
			return Float::parseFloat;
		}
	}

	public static class DoubleEntry extends NumberEntry<Double> {

		public DoubleEntry(String label, ModConfigSpec.ConfigValue<Double> value, ModConfigSpec.ValueSpec spec) {
			super(label, value, spec);
		}

		@Override
		protected Double getTypeMin() {
			return (double) -Float.MAX_VALUE;
		}

		@Override
		protected Double getTypeMax() {
			return (double) Float.MAX_VALUE;
		}

		@Override
		protected Function<String, Double> getParser() {
			return Double::parseDouble;
		}
	}
}
