package net.createmod.catnip.lang;

import java.util.List;

import javax.annotation.Nullable;

import joptsimple.internal.Strings;
import net.createmod.catnip.theme.Color;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;

public class LangBuilder {

	String namespace;
	@Nullable
	MutableComponent component;

	public LangBuilder(String namespace) {
		this.namespace = namespace;
	}

	public LangBuilder space() {
		return text(" ");
	}

	public LangBuilder newLine() {
		return text("\n");
	}

	/**
	 * Appends a localised component<br>
	 * To add an independently formatted localised component, use add() and a nested
	 * builder
	 *
	 * @param langKey
	 * @param args
	 * @return
	 */
	public LangBuilder translate(String langKey, Object... args) {
		Object[] args1 = resolveBuilders(args);
		return add(Component.translatable(namespace + "." + langKey, args1));
	}

	/**
	 * Appends a text component
	 *
	 * @param literalText
	 * @return
	 */
	public LangBuilder text(String literalText) {
		return add(Component.literal(literalText));
	}

	/**
	 * Appends a colored text component
	 *
	 * @param format
	 * @param literalText
	 * @return
	 */
	public LangBuilder text(ChatFormatting format, String literalText) {
		return add(Component.literal(literalText).withStyle(format));
	}

	/**
	 * Appends a colored text component
	 *
	 * @param color
	 * @param literalText
	 * @return
	 */
	public LangBuilder text(int color, String literalText) {
		return add(Component.literal(literalText).withStyle(s -> s.withColor(color)));
	}

	/**
	 * Appends the contents of another builder
	 *
	 * @param otherBuilder
	 * @return
	 */
	public LangBuilder add(LangBuilder otherBuilder) {
		return add(otherBuilder.component());
	}

	/**
	 * Appends a component
	 *
	 * @param customComponent
	 * @return
	 */
	public LangBuilder add(MutableComponent customComponent) {
		component = component == null ? customComponent : component.append(customComponent);
		return this;
	}

	/**
	 * Appends a component
	 *
	 * @param component the component to append
	 * @return this builder
	 */
	public LangBuilder add(Component component) {
		if (component instanceof MutableComponent mutableComponent)
			return add(mutableComponent);
		else
			return add(component.copy());
	}

	//

	/**
	 * Applies the format to all added components
	 *
	 * @param format
	 * @return
	 */
	public LangBuilder style(ChatFormatting format) {
		assertComponent();
		component = component.withStyle(format);
		return this;
	}

	/**
	 * Applies the color to all added components
	 */
	public LangBuilder color(int color) {
		assertComponent();
		component = component.withStyle(s -> s.withColor(color));
		return this;
	}

	/**
	 * Applies the color to all added components
	 */
	public LangBuilder color(Color color) {
		return this.color(color.getRGB());
	}

	//

	public MutableComponent component() {
		assertComponent();
		return component;
	}

	public String string() {
		return component().getString();
	}

	public String json() {
		return Component.Serializer.toJson(component());
	}

	public void sendStatus(Player player) {
		player.displayClientMessage(component(), true);
	}

	public void sendChat(Player player) {
		player.displayClientMessage(component(), false);
	}

	public void addTo(List<? super MutableComponent> tooltip) {
		tooltip.add(component());
	}

	public void forGoggles(List<? super MutableComponent> tooltip) {
		forGoggles(tooltip, 0);
	}

	public void forGoggles(List<? super MutableComponent> tooltip, int indents) {
		tooltip.add(new LangBuilder(namespace)
			.text(Strings.repeat(' ', getIndents(Minecraft.getInstance().font, 4 + indents)))
			.add(this)
			.component());
	}

	public static final float DEFAULT_SPACE_WIDTH = 4.0F; // space width in vanilla's default font
	static int getIndents(Font font, int defaultIndents) {
		int spaceWidth = font.width(" ");
		if (DEFAULT_SPACE_WIDTH == spaceWidth) {
			return defaultIndents;
		}
		return Mth.ceil(DEFAULT_SPACE_WIDTH * defaultIndents / spaceWidth);
	}

	//

	private void assertComponent() {
		if (component == null)
			throw new IllegalStateException("No components were added to builder");
	}

	//

	public static Object[] resolveBuilders(Object[] args) {
		for (int i = 0; i < args.length; i++)
			if (args[i] instanceof LangBuilder cb)
				args[i] = cb.component();
		return args;
	}

}
