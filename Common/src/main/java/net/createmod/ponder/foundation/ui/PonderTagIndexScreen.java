package net.createmod.ponder.foundation.ui;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;

import net.createmod.catnip.gui.ScreenOpener;
import net.createmod.catnip.gui.UIRenderHelper;
import net.createmod.catnip.gui.element.BoxElement;
import net.createmod.catnip.gui.widget.BoxWidget;
import net.createmod.catnip.utility.FontHelper;
import net.createmod.catnip.utility.lang.Components;
import net.createmod.catnip.utility.layout.LayoutHelper;
import net.createmod.catnip.utility.theme.Theme;
import net.createmod.ponder.Ponder;
import net.createmod.ponder.enums.PonderGuiTextures;
import net.createmod.ponder.foundation.PonderRegistry;
import net.createmod.ponder.foundation.PonderTag;
import net.createmod.ponder.foundation.PonderTheme;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;

public class PonderTagIndexScreen extends AbstractPonderScreen {

	protected List<ModTagsEntry> currentModTagEntries = new LinkedList<>();
	protected List<Map.Entry<String, List<PonderTag>>> sortedModTags = List.of();
	protected boolean isPaginated = false;
	protected int modsPerScreen;
	protected int indexStart;

	@Nullable protected PonderButton pageNext;
	@Nullable protected PonderButton pagePrev;

	@Nullable private PonderTag hoveredItem = null;

	// The ponder entry point from the menu. May be changed to include general
	// chapters in the future
	public PonderTagIndexScreen() {}

	@Override
	protected void init() {
		super.init();

		Map<String, List<PonderTag>> tagsByModID = PonderRegistry.TAGS.getListedTags().stream().collect(Collectors.groupingBy(tag -> tag.getId().getNamespace()));
		sortedModTags = new TreeMap<>(tagsByModID).entrySet().stream().toList();

		int modCount = sortedModTags.size();
		int maxModsOnScreen = (height - 140 - 40) / 58;
		if (modCount > 1 && modCount > maxModsOnScreen) {
			//enable mod pagination
			isPaginated = true;
			indexStart = 0;
			modsPerScreen = maxModsOnScreen;
		} else {
			isPaginated = false;
			indexStart = 0;
			modsPerScreen = modCount;
		}

		setupModTagEntries();

		if (isPaginated) {
			int xOffset = (int) (width * 0.5);

			addRenderableWidget(pagePrev = new PonderButton(xOffset - 120, height - 32)
					.showing(PonderGuiTextures.ICON_PONDER_LEFT)
					.withCallback(() -> updatePagination(-1))
					.setActive(false)
			);

			pagePrev.updateColorsFromState();

			addRenderableWidget(pageNext = new PonderButton(xOffset + 100, height - 32)
					.showing(PonderGuiTextures.ICON_PONDER_RIGHT)
					.withCallback(() -> updatePagination(1))
					.setActive(true)
			);
		}
	}

	protected void setupModTagEntries() {
		removeWidgets(children().stream().filter(widget -> {
			if (!(widget instanceof PonderButton ponderButton))
				return false;

			return ponderButton.tag != null;
		}).toList());

		currentModTagEntries.clear();

		AtomicInteger yOffset = new AtomicInteger(140);
		int xOffset = (int) (width * 0.5);

		for (int i = 0; i < modsPerScreen; i++) {
			if (indexStart + i >= sortedModTags.size())
				break;

			Map.Entry<String, List<PonderTag>> entry = sortedModTags.get(indexStart + i);
			String key = entry.getKey() + ".ponder.mod_name";
			String modName = I18n.exists(key) ? I18n.get(key) : entry.getKey();
			List<PonderTag> tags = entry.getValue();

			LayoutHelper layout = LayoutHelper.centeredHorizontal(tags.size(), 1, 28, 28, 8);
			Rect2i layoutArea = layout.getArea();

			for (PonderTag tag : tags) {
				PonderButton button = new PonderButton(xOffset + layout.getX() + 4, yOffset.get() + layout.getY() + 18)
						.showingTag(tag)
						.withCallback((mouseX, mouseY) -> {
							centerScalingOn(mouseX, mouseY);
							ScreenOpener.transitionTo(new PonderTagScreen(tag));
						});
				addRenderableWidget(button);
				layout.next();
			}

			currentModTagEntries.add(new ModTagsEntry(
					modName,
					tags.size(),
					layoutArea,
					yOffset.get()
			));

			yOffset.addAndGet(58 + 10);
		}
	}

	protected void updatePagination(int diff) {
		if (diff == 1) {
			indexStart = indexStart + modsPerScreen;
		} else if (diff == -1) {
			indexStart = Math.max(indexStart - modsPerScreen, 0);
		} else if (diff == 0) {
			indexStart = 0;
		}

		setupModTagEntries();

		pagePrev.<PonderButton>setActive(indexStart >= 1).animateGradientFromState();
		pageNext.<PonderButton>setActive(indexStart + modsPerScreen < sortedModTags.size()).animateGradientFromState();


	}

	@Override
	protected void initBackTrackIcon(BoxWidget backTrack) {
		backTrack.showing(PonderGuiTextures.ICON_PONDER_IDENTIFY);
	}

	@Override
	public void tick() {
		super.tick();
		PonderUI.ponderTicks++;

		hoveredItem = null;
		Window w = minecraft.getWindow();
		double mouseX = minecraft.mouseHandler.xpos() * w.getGuiScaledWidth() / w.getScreenWidth();
		double mouseY = minecraft.mouseHandler.ypos() * w.getGuiScaledHeight() / w.getScreenHeight();
		for (GuiEventListener child : children()) {
			if (child == backTrack)
				continue;
			if (child instanceof PonderButton button)
				if (button.isMouseOver(mouseX, mouseY))
					hoveredItem = button.getTag();
		}
	}

	@Override
	protected void renderWindow(PoseStack ms, int mouseX, int mouseY, float partialTicks) {
		ms.pushPose();
		ms.translate(width / 2d, 30, 0);

		//title, box for icon and streak
		ms.pushPose();
		ms.translate(-120, 0, 0);

		String title = Ponder.lang().translate(AbstractPonderScreen.WELCOME).string();

		new BoxElement().withBackground(PonderTheme.Key.PONDER_BACKGROUND_FLAT.c())
			.gradientBorder(PonderTheme.Key.PONDER_IDLE.p())
			.at(0, 0, 100)
			.withBounds(30, 30)
			.render(ms);

		//todo add icon inside the box

		//34 = 30 bounds + 2 padding + 2 box width
		//-3 = 2 padding + 1 pixel of the box
		ms.translate(34, -3, 0);

		int streakHeight = 36;
		UIRenderHelper.streak(ms, 0, 0, (streakHeight / 2), streakHeight, 280);

		ms.scale(2f, 2f, 2f);
		font.draw(ms, title, 3, 5, Theme.Key.TEXT.i());

		ms.popPose();
		ms.translate(0, 50, 0);
		ms.pushPose();
		//at the middle, 80px from the top now

		int maxWidth = (int) (width * .5f);
		String desc = Ponder.lang().translate(AbstractPonderScreen.DESCRIPTION).string();

		int descWidth = font.width(desc);
		if (descWidth + 2 < maxWidth)
			maxWidth = descWidth + 2;

		int descHeight = font.wordWrapHeight(desc, maxWidth);

		ms.translate(-maxWidth / 2f, 0, 0);

		new BoxElement().withBackground(PonderTheme.Key.PONDER_BACKGROUND_FLAT.c())
				.gradientBorder(PonderTheme.Key.PONDER_IDLE.p())
				.at(-3, -3, 0)
				.withBounds(maxWidth + 6, descHeight + 5)
				.render(ms);

		FontHelper.drawSplitString(ms, font, desc, 0, 0, maxWidth, Theme.Key.TEXT.i());
		ms.popPose();

		ms.translate(0, -80, 0);
		//at the middle of top edge now

		for(ModTagsEntry entry : currentModTagEntries) {
			ms.pushPose();
			renderTagsEntry(ms, entry);
			ms.popPose();
		}

		ms.popPose();

	}

	protected void renderTagsEntry(PoseStack ms, ModTagsEntry entry) {

		int layoutWidth = entry.layoutArea().getWidth();
		int layoutHeight = entry.layoutArea().getHeight();

		ms.translate(0, entry.yPos(), 0);

		String categories = Ponder.lang().translate(AbstractPonderScreen.CATEGORIES, entry.modName()).string();
		int stringWidth = font.width(categories);
		ms.pushPose();
		ms.translate(-stringWidth / 2f, -20, 0);

		new BoxElement().withBackground(PonderTheme.Key.PONDER_BACKGROUND_FLAT.c())
				.gradientBorder(PonderTheme.Key.PONDER_IDLE.p())
				.at(-3, -1, 0)
				.withBounds(stringWidth + 6, 10)
				.render(ms);

		font.draw(ms, categories, 0, 0, Theme.Key.TEXT.i());

		ms.popPose();

		int extraLength = Mth.clamp(entry.tagCount, 2, 8);

		UIRenderHelper.streak(ms,   0, 0, layoutHeight / 2, layoutHeight + 6, layoutWidth / 2 + extraLength * 15);
		UIRenderHelper.streak(ms, 180, 0, layoutHeight / 2, layoutHeight + 6, layoutWidth / 2 + extraLength * 15);

	}

	@Override
	protected void renderWindowForeground(PoseStack ms, int mouseX, int mouseY, float partialTicks) {
		RenderSystem.disableDepthTest();
		ms.pushPose();
		ms.translate(0, 0, 200);

		if (hoveredItem != null) {
			List<Component> list = FontHelper.cutStringTextComponent(hoveredItem.getDescription(),
				ChatFormatting.GRAY, ChatFormatting.GRAY);
			list.add(0, Components.literal(hoveredItem.getTitle()));
			renderComponentTooltip(ms, list, mouseX, mouseY);
		}

		ms.popPose();
		RenderSystem.enableDepthTest();
	}

	@Override
	public boolean isPauseScreen() {
		return true;
	}

	@Override
	public void removed() {
		super.removed();
		hoveredItem = null;
	}

	public record ModTagsEntry(
			String modName,
			int tagCount,
			Rect2i layoutArea,
			int yPos
	) {}

}
