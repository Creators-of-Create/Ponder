package net.createmod.catnip.impl.client.gui.config.entry.list;

import java.util.Arrays;
import java.util.Locale;
import java.util.Optional;
import java.util.function.Consumer;

import net.createmod.catnip.api.client.gui.TickableGuiEventListener;
import net.createmod.catnip.api.client.lang.FontHelper;
import net.createmod.catnip.api.client.lang.FontHelper.Palette;
import net.createmod.catnip.api.client.platform.ModClientHooksHelper;
import net.createmod.catnip.api.config.attribute.ConfigAttribute;
import net.createmod.catnip.api.config.ConfigPath;
import net.createmod.catnip.api.config.ConfigValueId;
import net.createmod.catnip.api.config.access.ConfigAccess;

import net.createmod.catnip.api.config.access.ConfigAccess.Nested;
import net.createmod.catnip.api.config.access.ConfigAccess.Value;
import net.createmod.catnip.api.config.attribute.ConfigAttributes;
import net.createmod.catnip.impl.client.gui.config.ConfigScreen;
import net.createmod.catnip.impl.client.gui.config.entry.NestedEntry;
import net.createmod.catnip.impl.client.gui.config.entry.UnhandledEntry;
import net.createmod.catnip.impl.client.gui.config.entry.ValueEntry;
import net.createmod.catnip.impl.config.ConfigHelper;
import net.minecraft.ChatFormatting;

import org.apache.commons.lang3.mutable.MutableObject;

import net.createmod.catnip.api.animation.LerpedFloat;
import net.createmod.catnip.api.animation.LerpedFloat.Chaser;
import net.createmod.catnip.api.client.gui.UIRenderHelper;
import net.createmod.catnip.api.theme.Color;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;

public final class ConfigEntryList extends AbstractConfigEntryList<ConfigEntryList.Entry> {
	public final ConfigScreen screen;

	public ConfigEntryList(ConfigScreen screen, int width, int height, int top, int elementHeight, String filter) {
		Minecraft minecraft = ModClientHooksHelper.INSTANCE.getMinecraftFromScreen(screen);
		super(minecraft, width, height, top, elementHeight);
		this.screen = screen;

		if (this.filter().equals(filter)) {
			this.fillEntries();
		} else {
			this.updateFilter(filter);
		}
	}

	@Override
	protected void fillEntries() {
		Entry selected = this.getSelected();
		this.clearEntries();
		this.setSelected(null);
		MutableObject<Entry> toSelect = new MutableObject<>();

		this.screen.forEachElement((element, path) -> {
			if (!this.filterAccepts(element))
				return;

			Entry entry = switch (element) {
				case Nested nested -> new NestedEntry(this, path, nested);
				case Value<?> value -> {
					Optional<ValueEntry<?>> valueEntry = ValueEntry.create(this, path, value);
					yield valueEntry.isPresent() ? valueEntry.get() : new UnhandledEntry(this, path, value);
				}
			};

			this.addEntry(entry);

			// maintain the previous selection, but apply it later since setSelected will also scroll to it
			if (selected != null && entry.name().equals(element.name())) {
				toSelect.setValue(entry);
			}
		});

		if (toSelect.get() != null) {
			this.setSelected(toSelect.get());
		}
	}

	private boolean filterAccepts(ConfigAccess.Element element) {
		String filter = this.filter();
		return filter.isEmpty()
			|| element.name().toLowerCase(Locale.ROOT).contains(filter)
			|| element.comment().toLowerCase(Locale.ROOT).contains(filter);
	}

	public static abstract class Entry extends AbstractConfigEntryList.Entry<Entry> implements TickableGuiEventListener {
		public final ConfigValueId id;
		protected final ConfigEntryList list;
		private final ConfigAccess.Element element;
		private final LerpedFloat highlightAnimation;

		protected Entry(ConfigEntryList list, ConfigPath path, ConfigAccess.Element element) {
			super(ConfigHelper.toHumanReadable(element.name()));
			this.list = list;
			this.id = new ConfigValueId(list.screen.configId, path);
			this.element = element;
			this.highlightAnimation = LerpedFloat.linear().startWithValue(0);
		}

		@Override
		public void tick() {
			this.highlightAnimation.tickChaser();
		}

		@Override
		public void extractContent(GuiGraphicsExtractor graphics, int mouseX, int mouseY, boolean hovered, float partialTick) {
			float animation = highlightAnimation.getValue(partialTick);
			if (animation > .01f) {
				Color highlight = new Color(0xa0_ffffff).scaleAlpha(animation);
				UIRenderHelper.streak(graphics, 0, getX() - 10, getY() + getHeight() / 2, getHeight() - 6, 5, highlight);
				UIRenderHelper.streak(graphics, 180, getX() + getWidth(), getY() + getHeight() / 2, getHeight() - 6, 5, highlight);
				UIRenderHelper.streak(graphics, 90, getX() + getWidth() / 2 - 5, getY() + 3, getWidth() + 10, 5, highlight);
				UIRenderHelper.streak(graphics, -90, getX() + getWidth() / 2 - 5, getY() + getHeight() - 3, getWidth() + 10, 5, highlight);
			}
		}

		public String name() {
			return this.id.path().components.getLast();
		}

		public void highlight() {
			this.highlightAnimation.startWithValue(1).chase(0, 0.1f, Chaser.LINEAR);
		}

		protected final boolean isDirty() {
			return this.list.screen.isDirty(this.id.path());
		}

		@Override
		protected void buildTooltip(Consumer<Component> output) {
			output.accept(Component.literal(this.label).withStyle(ChatFormatting.WHITE));

			String comment = this.element.comment();
			if (comment.isEmpty())
				return;

			Arrays.stream(comment.split("\n"))
				.filter(Entry::isNonMetadataLine)
				.map(s -> s.equals(".") ? "" : s)
				.map(Component::literal)
				.flatMap(stc -> FontHelper.cutTextComponent(stc, Palette.ALL_GRAY).stream())
				.forEach(output);

			if (this.element instanceof ConfigAccess.Value<?> value) {
				ConfigAttributes attributes = value.attributes();
				if (attributes.has(ConfigAttribute.Flag.REQUIRES_RELOG)) {
					FontHelper.cutTextComponent(Component.translatable("catnip.ui.value_entry.relog_required"), Palette.GRAY_AND_GOLD).forEach(output);
				}

				if (attributes.has(ConfigAttribute.Flag.REQUIRES_RESTART)) {
					FontHelper.cutTextComponent(Component.translatable("catnip.ui.value_entry.restart_required"), Palette.GRAY_AND_RED).forEach(output);
				}
			}

			output.accept(Component.literal(this.id.toString()).withStyle(ChatFormatting.DARK_GRAY));
		}

		private static boolean isNonMetadataLine(String line) {
			String cleaned = line.trim().toLowerCase(Locale.ROOT);
			return !cleaned.startsWith("range:") && !cleaned.startsWith("min:") && !cleaned.startsWith("max:");
		}
	}
}
