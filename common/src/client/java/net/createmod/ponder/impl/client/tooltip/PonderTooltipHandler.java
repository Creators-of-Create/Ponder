package net.createmod.ponder.impl.client.tooltip;

import java.util.ArrayList;
import java.util.List;

import com.mojang.blaze3d.systems.RenderSystem;

import net.createmod.catnip.api.animation.LerpedFloat;
import net.createmod.catnip.api.client.animation.AnimationTickHolder;
import net.createmod.catnip.api.client.event.ClientTickCallback;
import net.createmod.catnip.api.client.gui.NavigatableSimiScreen;
import net.createmod.catnip.api.client.gui.ScreenOpener;
import net.createmod.catnip.api.registry.RegisteredObjectsHelper;
import net.createmod.ponder.api.Ponder;
import net.createmod.ponder.api.client.HoveredItemProvider;
import net.createmod.ponder.api.client.PonderIndex;
import net.createmod.ponder.api.client.event.TooltipQueryCallback;
import net.createmod.ponder.impl.client.PonderKeybinds;
import net.createmod.ponder.impl.client.gui.PonderUI;
import net.createmod.ponder.impl.client.registration.PonderLocalization;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Item.TooltipContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TooltipFlag;

public final class PonderTooltipHandler {
	public static final String HOLD_TO_PONDER = PonderLocalization.UI_PREFIX + "hold_to_ponder";
	public static final Component SUBJECT = Component.translatable("ponder.ui.subject").withStyle(ChatFormatting.GREEN);

	private static final List<HoveredItemProvider> hoveredItemProviders = new ArrayList<>();

	private static final LerpedFloat openProgress = LerpedFloat.linear().startWithValue(0);

	private PonderTooltipHandler() {}

	public static void init() {
		// lowest priority first
		FallbackHoveredItemProvider.init();
		ContainerScreenHoveredItemProvider.init();

		ClientTickCallback.EVENT.pre().subscribe(PonderTooltipHandler::beforeClientTick);
		TooltipQueryCallback.EVENT.subscribe(PonderTooltipHandler::onTooltipQuery);
	}

	// see FallbackHoveredItemProvider for why this is pre instead of post
	private static void beforeClientTick() {
		Item hoveredItem = determineHoveredItem();
		if (hoveredItem == Items.AIR || isCurrentPonderSubject(hoveredItem) || !hasPonderScenes(hoveredItem)) {
			openProgress.startWithValue(0);
			return;
		}

		float progress = openProgress.getValue();
		if (progress >= 1) {
			openPonder(hoveredItem);
			openProgress.startWithValue(0);
			return;
		}

		if (PonderKeybinds.PONDER.isDown()) {
			openProgress.setValue(Math.min(1, progress + Math.max(0.25, progress) * 0.25));
		} else {
			openProgress.setValue(Math.max(0, progress - 0.05));
		}
	}

	private static void openPonder(Item item) {
		Screen currentScreen = Minecraft.getInstance().gui.screen();
		if (currentScreen instanceof NavigatableSimiScreen simiScreen) {
			simiScreen.centerScalingOnMouse();
		}

		ScreenOpener.transitionTo(PonderUI.of(item));
	}

	// invoked whenever an item's tooltip is queried.
	// this happens in a lot of places, so do our best to filter for getting the tooltip of the hovered item.
	private static void onTooltipQuery(ItemStack stack, TooltipContext context, TooltipFlag flags, List<Component> tooltip) {
		if (!hidesPonderTooltip(context, flags) && hasPonderScenes(stack.getItem())) {
			insert(tooltip, determineTooltip(stack));
		}
	}

	private static Component determineTooltip(ItemStack stack) {
		if (!RenderSystem.isOnRenderThread()) {
			// tooltips can be computed off-thread for a number of reasons, but in vanilla the creative search
			// index is built in a keyboard input callback. in this case, just use the default message.
			return holdToPonderMessage();
		}

		if (isCurrentPonderSubject(stack.getItem())) {
			return SUBJECT;
		}

		float progress = getVisualProgress();
		return progress <= 0 ? holdToPonderMessage() : makeProgressBar(progress);
	}

	/// @return the visual Ponder opening progress, slightly scaled so visuals reach completion faster
	public static float getVisualProgress() {
		float progress = openProgress.getValue(AnimationTickHolder.getGuiPartialTicks());
		return Math.min(1, progress * 8 / 7);
	}

	// create a line of vertical bars (|) roughly equal in size to the hold message
	private static Component makeProgressBar(float progress) {
		Font font = Minecraft.getInstance().font;
		float barWidth = font.width("|");
		float targetWidth = font.width(holdToPonderMessage());

		int total = (int) (targetWidth / barWidth);
		int current = (int) (progress * total);

		MutableComponent result = Component.empty();
		result.append(Component.literal("|".repeat(current)).withStyle(ChatFormatting.GRAY));
		result.append(Component.literal("|".repeat(total - current)).withStyle(ChatFormatting.DARK_GRAY));
		return result;
	}

	public static void registerHoveredItemProvider(HoveredItemProvider provider) {
		// synchronize here to ensure thread safety on neo
		synchronized (hoveredItemProviders) {
			// add at start so ones registered later are higher priority
			hoveredItemProviders.addFirst(provider);
		}
	}

	private static Component holdToPonderMessage() {
		Component key = PonderKeybinds.PONDER.message().copy().withStyle(ChatFormatting.GRAY);
		return Ponder.lang().translate(HOLD_TO_PONDER, key).style(ChatFormatting.DARK_GRAY).component();
	}

	private static Item determineHoveredItem() {
		if (Minecraft.getInstance().gui.screen() == null)
			return Items.AIR;

		for (HoveredItemProvider provider : hoveredItemProviders) {
			Item item = provider.determineHoveredItem();
			if (item != Items.AIR) {
				return item;
			}
		}

		return Items.AIR;
	}

	private static void insert(List<Component> tooltip, Component line) {
		if (tooltip.size() < 2) {
			tooltip.add(line);
		} else {
			tooltip.add(1, line);
		}
	}

	private static boolean hasPonderScenes(Item item) {
		Identifier id = RegisteredObjectsHelper.getKeyOrThrow(item);
		return PonderIndex.getSceneAccess().doScenesExistForId(id);
	}

	// in identify mode in ponder scenes, you can hover over components to see their tooltip. this includes
	// the 'Hold [w] to ponder' message, which works, allowing you to jump to the scenes of that block.
	// when the hovered block is the current subject, we don't want to do this, since that would just
	// open the same scene again. instead, replace it with a 'Subject of this scene' message.
	private static boolean isCurrentPonderSubject(Item item) {
		if (Minecraft.getInstance().gui.screen() instanceof PonderUI ponder) {
			return ponder.getSubject().getItem() == item;
		}

		return false;
	}

	private static boolean hidesPonderTooltip(TooltipContext context, TooltipFlag flag) {
		// TODO: Jade should use a custom TooltipFlag impl when querying ItemEntity
		//  tooltips, so we can identify it and avoid adding the ponder message to it.
		//  Any other situations like that?
		return false;
	}
}
