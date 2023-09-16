package net.createmod.ponder.foundation;

import com.google.common.base.Strings;
import net.createmod.catnip.gui.NavigatableSimiScreen;
import net.createmod.catnip.gui.ScreenOpener;
import net.createmod.catnip.platform.CatnipClientServices;
import net.createmod.catnip.platform.CatnipServices;
import net.createmod.catnip.utility.Couple;
import net.createmod.catnip.utility.animation.LerpedFloat;
import net.createmod.catnip.utility.lang.Components;
import net.createmod.catnip.utility.theme.Color;
import net.createmod.ponder.Ponder;
import net.createmod.ponder.foundation.registration.PonderLocalization;
import net.createmod.ponder.foundation.ui.PonderUI;
import net.minecraft.ChatFormatting;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.ItemStack;

import java.util.List;
import java.util.Optional;

public class PonderTooltipHandler {

	private static final Color borderA = new Color(0x5000ff, false).setImmutable();
	private static final Color borderB = new Color(0x5555ff, false).setImmutable();
	private static final Color borderC = new Color(0xffffff, false).setImmutable();

	public static boolean enable = true;

	static LerpedFloat holdWProgress = LerpedFloat.linear().startWithValue(0);
	static ItemStack hoveredStack = ItemStack.EMPTY;
	static ItemStack trackingStack = ItemStack.EMPTY;
	static boolean subject = false;
	static boolean deferTick = false;

	public static final String HOLD_TO_PONDER = PonderLocalization.UI_PREFIX + "hold_to_ponder";
	public static final String SUBJECT = PonderLocalization.UI_PREFIX + "subject";

	public static void tick() {
		deferTick = true;
	}

	public static void deferredTick() {
		deferTick = false;
		Minecraft instance = Minecraft.getInstance();
		Screen currentScreen = instance.screen;

		if (hoveredStack.isEmpty() || trackingStack.isEmpty()) {
			trackingStack = ItemStack.EMPTY;
			holdWProgress.startWithValue(0);
			return;
		}

		float value = holdWProgress.getValue();

		if (!subject && CatnipClientServices.CLIENT_HOOKS.isKeyPressed(ponderKeybind())) {
			if (value >= 1) {
				if (currentScreen instanceof NavigatableSimiScreen)
					((NavigatableSimiScreen) currentScreen).centerScalingOnMouse();
				ScreenOpener.transitionTo(PonderUI.of(trackingStack));
				holdWProgress.startWithValue(0);
				return;
			}
			holdWProgress.setValue(Math.min(1, value + Math.max(.25f, value) * .25f));
		} else
			holdWProgress.setValue(Math.max(0, value - .05f));

		hoveredStack = ItemStack.EMPTY;
	}

	public static void addToTooltip(List<Component> toolTip, ItemStack stack) {
		if (!enable)
			return;

		updateHovered(stack);

		if (deferTick)
			deferredTick();

		if (trackingStack != stack)
			return;

		float renderPartialTicks = Minecraft.getInstance()
			.getFrameTime();
		Component component = subject ? Ponder.lang().translate(SUBJECT).component()
			.withStyle(ChatFormatting.GREEN)
			: makeProgressBar(Math.min(1, holdWProgress.getValue(renderPartialTicks) * 8 / 7f));
		if (toolTip.size() < 2)
			toolTip.add(component);
		else
			toolTip.add(1, component);
	}

	protected static void updateHovered(ItemStack stack) {
		Minecraft instance = Minecraft.getInstance();
		Screen currentScreen = instance.screen;
		boolean inPonderUI = currentScreen instanceof PonderUI;

		ItemStack prevStack = trackingStack;
		hoveredStack = ItemStack.EMPTY;
		subject = false;

		if (inPonderUI) {
			PonderUI ponderUI = (PonderUI) currentScreen;
			ItemStack uiSubject = ponderUI.getSubject();
			if (!uiSubject.isEmpty() && stack.is(uiSubject.getItem()))
				subject = true;
		}

		if (stack.isEmpty())
			return;
		if (!PonderIndex.getSceneAccess().doScenesExistForId(CatnipServices.REGISTRIES.getKeyOrThrow(stack.getItem())))
			return;

		if (prevStack.isEmpty() || !prevStack.is(stack.getItem()))
			holdWProgress.startWithValue(0);

		hoveredStack = stack;
		trackingStack = stack;
	}

	public static Optional<Couple<Color>> handleTooltipColor(ItemStack stack) {
		if (trackingStack != stack)
			return Optional.empty();

		if (holdWProgress.getValue() == 0)
			return Optional.empty();

		float renderPartialTicks = Minecraft.getInstance().getFrameTime();

		Color startC;
		Color endC;
		float progress = Math.min(1, holdWProgress.getValue(renderPartialTicks) * 8 / 7f);

		startC = getSmoothColorForProgress(progress);
		endC = getSmoothColorForProgress(progress);

		return Optional.of(Couple.create(startC, endC));

	}

	private static Color getSmoothColorForProgress(float progress) {
		if (progress < 0.5)
			return borderA.mixWith(borderB, progress * 2);
		return borderB.mixWith(borderC, (progress - .5f) * 2);
	}

	private static Component makeProgressBar(float progress) {
		MutableComponent holdW = Ponder.lang()
			.translate(HOLD_TO_PONDER,
				((MutableComponent) ponderKeybind().getTranslatedKeyMessage()).withStyle(ChatFormatting.GRAY))
				.style(ChatFormatting.DARK_GRAY)
				.component();

		Font fontRenderer = Minecraft.getInstance().font;
		float charWidth = fontRenderer.width("|");
		float tipWidth = fontRenderer.width(holdW);

		int total = (int) (tipWidth / charWidth);
		int current = (int) (progress * total);

		if (progress > 0) {
			String bars = "";
			bars += ChatFormatting.GRAY + Strings.repeat("|", current);
			if (progress < 1)
				bars += ChatFormatting.DARK_GRAY + Strings.repeat("|", total - current);
			return Components.literal(bars);
		}

		return holdW;
	}

	protected static KeyMapping ponderKeybind() {
		return Minecraft.getInstance().options.keyUp;
	}

}
