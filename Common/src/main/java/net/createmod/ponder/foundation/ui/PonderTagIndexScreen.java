package net.createmod.ponder.foundation.ui;

import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.createmod.catnip.gui.ScreenOpener;
import net.createmod.catnip.gui.UIRenderHelper;
import net.createmod.catnip.gui.element.BoxElement;
import net.createmod.catnip.gui.widget.BoxWidget;
import net.createmod.catnip.utility.ClientFontHelper;
import net.createmod.catnip.utility.FontHelper;
import net.createmod.catnip.utility.FontHelper.Palette;
import net.createmod.catnip.utility.lang.Components;
import net.createmod.catnip.utility.layout.LayoutHelper;
import net.createmod.catnip.utility.layout.PaginationState;
import net.createmod.catnip.utility.theme.Theme;
import net.createmod.ponder.Ponder;
import net.createmod.ponder.enums.PonderGuiTextures;
import net.createmod.ponder.foundation.PonderIndex;
import net.createmod.ponder.foundation.PonderTag;
import net.createmod.ponder.foundation.PonderTheme;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;

import javax.annotation.Nullable;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class PonderTagIndexScreen extends AbstractPonderScreen {

	protected List<ModTagsEntry> currentModTagEntries = new LinkedList<>();
	protected List<Map.Entry<String, List<PonderTag>>> sortedModTags = List.of();
	protected PaginationState paginationState = new PaginationState();

	@Nullable protected PonderButton pageNext;
	@Nullable protected PonderButton pagePrev;

	@Nullable private PonderTag hoveredItem = null;

	// The main ponder entry point from menus.
	public PonderTagIndexScreen() {}

	@Override
	protected void init() {
		super.init();

		Map<String, List<PonderTag>> tagsByModID = PonderIndex.getTagAccess().getListedTags().stream().collect(Collectors.groupingBy(tag -> tag.getId().getNamespace()));
		sortedModTags = new TreeMap<>(tagsByModID).entrySet().stream().toList();

		int modCount = sortedModTags.size();
		int maxModsOnScreen = (height - 140 - 40) / 58;

		paginationState = new PaginationState(modCount > 1 && modCount > maxModsOnScreen, maxModsOnScreen, modCount);

		setupModTagEntries();

		if (!paginationState.usesPagination())
			return;

		int xOffset = (int) (width * 0.5);

		addRenderableWidget(pagePrev = new PonderButton(xOffset - 120, height - 32)
				.showing(PonderGuiTextures.ICON_PONDER_LEFT)
				.withCallback(() -> {
					paginationState.previousPage();
					updateAfterPaginationChange();
				})
				.setActive(false)
		);

		pagePrev.updateColorsFromState();

		addRenderableWidget(pageNext = new PonderButton(xOffset + 100, height - 32)
				.showing(PonderGuiTextures.ICON_PONDER_RIGHT)
				.withCallback(() -> {
					paginationState.nextPage();
					updateAfterPaginationChange();
				})
				.setActive(true)
		);

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

		paginationState.iterateForCurrentPage((iPage, iOverall) -> {
			Map.Entry<String, List<PonderTag>> entry = sortedModTags.get(iOverall);
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
		});

		for (int i = 0; i < paginationState.getElementsPerPage(); i++) {
			if (paginationState.getStartIndex() + i >= sortedModTags.size())
				break;


		}
	}

	protected void updateAfterPaginationChange() {
		setupModTagEntries();

		pagePrev.<PonderButton>setActive(paginationState.hasPreviousPage()).animateGradientFromState();
		pageNext.<PonderButton>setActive(paginationState.hasNextPage()).animateGradientFromState();
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
	protected void renderWindow(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
		super.renderWindow(graphics, mouseX, mouseY, partialTicks);
		PoseStack poseStack = graphics.pose();

		poseStack.pushPose();
		poseStack.translate(width / 2d, 30, 0);

		//title, box for icon and streak
		poseStack.pushPose();
		poseStack.translate(-120, 0, 0);

		String title = Ponder.lang().translate(AbstractPonderScreen.WELCOME).string();

		new BoxElement().withBackground(PonderTheme.Key.PONDER_BACKGROUND_FLAT.c())
			.gradientBorder(PonderTheme.Key.PONDER_IDLE.p())
			.at(0, 0, 100)
			.withBounds(30, 30)
			.render(graphics);

		//todo add icon inside the box

		//34 = 30 bounds + 2 padding + 2 box width
		//-3 = 2 padding + 1 pixel of the box
		poseStack.translate(34, -3, 0);

		int streakHeight = 36;
		UIRenderHelper.streak(graphics, 0, 0, (streakHeight / 2), streakHeight, 280);

		poseStack.scale(2f, 2f, 2f);
		graphics.drawString(font, title, 3, 5, Theme.Key.TEXT.i(), false);

		poseStack.popPose();
		poseStack.translate(0, 50, 0);
		poseStack.pushPose();
		//at the middle, 80px from the top now

		int maxWidth = (int) (width * .5f);
		String desc = Ponder.lang().translate(AbstractPonderScreen.DESCRIPTION).string();

		int descWidth = font.width(desc);
		if (descWidth + 2 < maxWidth)
			maxWidth = descWidth + 2;

		int descHeight = font.wordWrapHeight(desc, maxWidth);

		poseStack.translate(-maxWidth / 2f, 0, 0);

		new BoxElement().withBackground(PonderTheme.Key.PONDER_BACKGROUND_FLAT.c())
				.gradientBorder(PonderTheme.Key.PONDER_IDLE.p())
				.at(-3, -3, 0)
				.withBounds(maxWidth + 6, descHeight + 5)
				.render(graphics);

		ClientFontHelper.drawSplitString(poseStack, font, desc, 0, 0, maxWidth, Theme.Key.TEXT.i());
		poseStack.popPose();

		poseStack.translate(0, -80, 0);
		//at the middle of top edge now

		for(ModTagsEntry entry : currentModTagEntries) {
			poseStack.pushPose();
			renderTagsEntry(graphics, entry);
			poseStack.popPose();
		}

		poseStack.popPose();

	}

	protected void renderTagsEntry(GuiGraphics graphics, ModTagsEntry entry) {
		PoseStack poseStack = graphics.pose();

		int layoutWidth = entry.layoutArea().getWidth();
		int layoutHeight = entry.layoutArea().getHeight();

		poseStack.translate(0, entry.yPos(), 0);

		String categories = Ponder.lang().translate(AbstractPonderScreen.CATEGORIES, entry.modName()).string();
		int stringWidth = font.width(categories);
		poseStack.pushPose();
		poseStack.translate(-stringWidth / 2f, -20, 0);

		new BoxElement().withBackground(PonderTheme.Key.PONDER_BACKGROUND_FLAT.c())
				.gradientBorder(PonderTheme.Key.PONDER_IDLE.p())
				.at(-3, -1, 0)
				.withBounds(stringWidth + 6, 10)
				.render(graphics);

		graphics.drawString(font, categories, 0, 0, Theme.Key.TEXT.i(), false);

		poseStack.popPose();

		int extraLength = Mth.clamp(entry.tagCount, 2, 8);

		UIRenderHelper.streak(graphics,   0, 0, layoutHeight / 2, layoutHeight + 6, layoutWidth / 2 + extraLength * 15);
		UIRenderHelper.streak(graphics, 180, 0, layoutHeight / 2, layoutHeight + 6, layoutWidth / 2 + extraLength * 15);

	}

	@Override
	protected void renderWindowForeground(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
		RenderSystem.disableDepthTest();
		PoseStack poseStack = graphics.pose();
		poseStack.pushPose();
		poseStack.translate(0, 0, 200);

		if (hoveredItem != null) {
			List<Component> list = FontHelper.cutStringTextComponent(hoveredItem.getDescription(), Palette.ALL_GRAY);
			list.add(0, Components.literal(hoveredItem.getTitle()));
			graphics.renderComponentTooltip(font, list, mouseX, mouseY);
		}

		poseStack.popPose();
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
