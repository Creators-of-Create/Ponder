package net.createmod.catnip.config.ui.entries;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import javax.annotation.Nonnull;

import org.lwjgl.glfw.GLFW;

import com.mojang.blaze3d.platform.ClipboardManager;
import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.vertex.PoseStack;

import net.createmod.catnip.config.ui.ConfigAnnotations;
import net.createmod.catnip.config.ui.ConfigHelper;
import net.createmod.catnip.config.ui.ConfigScreen;
import net.createmod.catnip.config.ui.ConfigScreenList;
import net.createmod.catnip.config.ui.SubMenuConfigScreen;
import net.createmod.catnip.enums.CatnipGuiTextures;
import net.createmod.catnip.gui.element.DelegatedStencilElement;
import net.createmod.catnip.gui.widget.BoxWidget;
import net.createmod.catnip.utility.FontHelper;
import net.createmod.catnip.utility.FontHelper.Palette;
import net.createmod.catnip.utility.Pair;
import net.createmod.catnip.utility.lang.Components;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.config.ModConfig;

public class ValueEntry<T> extends ConfigScreenList.LabeledEntry {

	protected static final int resetWidth = 28;//including 6px offset on either side

	public static final ClipboardManager clipboardHelper = new ClipboardManager();

	protected ForgeConfigSpec.ConfigValue<T> value;
	protected ForgeConfigSpec.ValueSpec spec;
	protected BoxWidget resetButton;
	protected boolean editable = true;

	public ValueEntry(String label, ForgeConfigSpec.ConfigValue<T> value, ForgeConfigSpec.ValueSpec spec) {
		super(label);
		this.value = value;
		this.spec = spec;
		this.path = String.join(".", value.getPath());

		resetButton = new BoxWidget(0, 0, resetWidth - 12, 16)
				.showingElement(CatnipGuiTextures.ICON_CONFIG_RESET.asStencil())
				.withCallback(() -> {
					setValue((T) spec.getDefault());
					this.onReset();
				});
		resetButton.modifyElement(e -> ((DelegatedStencilElement) e).withElementRenderer(BoxWidget.gradientFactory.apply(resetButton)));

		listeners.add(resetButton);

		List<String> path = value.getPath();
		labelTooltip.add(Components.literal(label).withStyle(ChatFormatting.WHITE));
		String comment = spec.getComment();
		if (comment == null || comment.isEmpty())
			return;

		List<String> commentLines = new ArrayList<>(Arrays.asList(comment.split("\n")));


		Pair<String, Map<String, String>> metadata = ConfigHelper.readMetadataFromComment(commentLines);
		if (metadata.getFirst() != null) {
			unit = metadata.getFirst();
		}
		if (metadata.getSecond() != null && !metadata.getSecond().isEmpty()) {
			annotations.putAll(metadata.getSecond());
		}
		// add comment to tooltip
		labelTooltip.addAll(commentLines.stream()
				.filter(s -> !s.startsWith("Range"))
				.map(s -> s.equals(".") ? " " : s)
				.map(Components::literal)
				.flatMap(stc -> FontHelper.cutTextComponent(stc, Palette.ALL_GRAY).stream())
				.toList()
		);

		if (annotations.containsKey(ConfigAnnotations.RequiresRelog.TRUE.getName()))
			labelTooltip.addAll(FontHelper.cutTextComponent(Components.literal("Changing this value will require a _relog_ to take full effect"), Palette.GRAY_AND_GOLD));

		if (annotations.containsKey(ConfigAnnotations.RequiresRestart.CLIENT.getName()))
			labelTooltip.addAll(FontHelper.cutTextComponent(Components.literal("Changing this value will require a _restart_ to take full effect"), Palette.GRAY_AND_RED));

		labelTooltip.add(Components.literal(ConfigScreen.modID + ":" + path.get(path.size() - 1)).withStyle(ChatFormatting.DARK_GRAY));
	}

	@Override
	protected void setEditable(boolean b) {
		editable = b;
		resetButton.active = editable && !isCurrentValueDefault();
		resetButton.animateGradientFromState();
	}

	@Override
	public void tick() {
		super.tick();
		resetButton.tick();
	}

	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int button) {
		if (super.mouseClicked(mouseX, mouseY, button))
			return true;

		if (button != GLFW.GLFW_MOUSE_BUTTON_1) {
			return false;
		}

		long handle = Minecraft.getInstance().getWindow().getWindow();
		if (!InputConstants.isKeyDown(handle, GLFW.GLFW_KEY_LEFT_CONTROL)) {
			return false;
		}

		// workaround while config type isn't available here yet.
		ModConfig.Type configType = ModConfig.Type.CLIENT;
		Screen screen = Minecraft.getInstance().screen;
		if (screen instanceof SubMenuConfigScreen subMenuScreen) {
			configType = subMenuScreen.type;
		}


		// ctrl-click to copy the full path to clipboard
		this.annotations.put("highlight", ":)");
		clipboardHelper.setClipboard(handle, ConfigScreen.modID + ":" + configType.extension() + "." + path);

		return true;
	}

	@Override
	public void render(PoseStack ms, int index, int y, int x, int width, int height, int mouseX, int mouseY, boolean p_230432_9_, float partialTicks) {
		super.render(ms, index, y, x, width, height, mouseX, mouseY, p_230432_9_, partialTicks);

		resetButton.x = x + width - resetWidth + 6;
		resetButton.y = y + 10;
		resetButton.render(ms, mouseX, mouseY, partialTicks);
	}

	@Override
	protected int getLabelWidth(int totalWidth) {
		return (int) (totalWidth * labelWidthMult) + 30;
	}

	public void setValue(@Nonnull T value) {
		ConfigHelper.setValue(path, this.value, value, annotations);
		onValueChange(value);
	}

	@Nonnull
	public T getValue() {
		return ConfigHelper.getValue(path, this.value);
	}

	protected boolean isCurrentValueDefault() {
		return spec.getDefault().equals(getValue());
	}

	public void onReset() {
		onValueChange(getValue());
	}

	public void onValueChange() {
		onValueChange(getValue());
	}
	public void onValueChange(T newValue) {
		resetButton.active = editable && !isCurrentValueDefault();
		resetButton.animateGradientFromState();
	}

	protected void bumpCog() {bumpCog(10f);}
	protected void bumpCog(float force) {
		ConfigScreen.cogSpin.bump(3, force);
	}
}
