package net.createmod.ponder.foundation.ui;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

import javax.annotation.Nullable;

import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.vertex.PoseStack;

import net.createmod.catnip.gui.NavigatableSimiScreen;
import net.createmod.catnip.gui.ScreenOpener;
import net.createmod.catnip.gui.UIRenderHelper;
import net.createmod.catnip.gui.widget.BoxWidget;
import net.createmod.catnip.platform.CatnipServices;
import net.createmod.catnip.utility.layout.LayoutHelper;
import net.createmod.catnip.utility.layout.PaginationState;
import net.createmod.catnip.utility.theme.Theme;
import net.createmod.ponder.enums.PonderGuiTextures;
import net.createmod.ponder.foundation.PonderIndex;
import net.createmod.ponder.foundation.registration.PonderIndexExclusionHelper;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;

public class PonderIndexScreen extends AbstractPonderScreen {

	protected final List<ItemEntry> items;
	protected List<PonderButton> paginatedWidgets = new ArrayList<>();
	protected PaginationState paginationState = new PaginationState();
	protected Rect2i maxScreenArea = new Rect2i(0, 0, 0, 0);
	protected Rect2i usedArea = new Rect2i(0, 0, 0, 0);
	protected int maxItemRows;
	protected int maxItemsPerRow;
	protected int maxItemsPerPage;

	@Nullable protected PonderButton nextPage;
	@Nullable protected PonderButton prevPage;

	private ItemStack hoveredItem = ItemStack.EMPTY;

	private final List<Predicate<ItemLike>> exclusions;

	public PonderIndexScreen() {
		items = new ArrayList<>();
		// collect exclusions once at screen creation instead of every time they are needed
		exclusions = PonderIndex.streamPlugins()
				.flatMap(PonderIndexExclusionHelper::pluginToExclusions)
				.toList();
	}

	@Override
	protected void init() {
		super.init();

		items.clear();
		PonderIndex.getSceneAccess()
				.getRegisteredEntries()
				.stream()
				.map(Map.Entry::getKey)
				.distinct()
				.map(key -> new ItemEntry(CatnipServices.REGISTRIES.getItemOrBlock(key), key))
				.filter(entry -> entry.item != null)
				.filter(this::isItemIncluded)
				.forEach(items::add);

		items.sort(Comparator.comparing(ItemEntry::key));

		int centerX = width / 2;
		int centerY = height / 2;
		int targetWidth = Mth.clamp(width - 180, 250, 400);
		int targetHeight = Mth.clamp(height - 140, 150, 300);

		maxScreenArea = new Rect2i(centerX - targetWidth / 2, centerY - targetHeight / 2, targetWidth, targetHeight);

		// height/width = 28 per item + 8 spacing between
		maxItemRows = (new Rect2i(centerX - targetWidth / 2, centerY - targetHeight / 2, targetWidth, targetHeight).getHeight() + 8) / 36;
		maxItemsPerRow = (new Rect2i(centerX - targetWidth / 2, centerY - targetHeight / 2, targetWidth, targetHeight).getWidth() + 8) / 36;
		maxItemsPerPage = maxItemRows * maxItemsPerRow;

		paginationState = new PaginationState(items.size() > maxItemsPerPage, maxItemsPerPage, items.size());

		setupItemsForPage();

		if (!paginationState.usesPagination())
			return;

		addRenderableWidget(prevPage = new PonderButton(centerX - 100, new Rect2i(centerX - targetWidth / 2, centerY - targetHeight / 2, targetWidth, targetHeight).getY() + new Rect2i(centerX - targetWidth / 2, centerY - targetHeight / 2, targetWidth, targetHeight).getHeight() + 10)
				.showing(PonderGuiTextures.ICON_PONDER_LEFT)
				.withCallback(() -> {
					paginationState.previousPage();
					updateAfterPaginationChange();
				})
				.setActive(false)
		);

		addRenderableWidget(nextPage = new PonderButton(centerX + 80, new Rect2i(centerX - targetWidth / 2, centerY - targetHeight / 2, targetWidth, targetHeight).getY() + new Rect2i(centerX - targetWidth / 2, centerY - targetHeight / 2, targetWidth, targetHeight).getHeight() + 10)
				.showing(PonderGuiTextures.ICON_PONDER_RIGHT)
				.withCallback(() -> {
					paginationState.nextPage();
					updateAfterPaginationChange();
				})
				.setActive(true)
		);

		prevPage.updateColorsFromState();
		nextPage.updateColorsFromState();
	}

	protected void setupItemsForPage() {
		removeWidgets(paginatedWidgets);

		int itemCount = paginationState.getCurrentPageElementCount();
		int actualItemRows = Mth.clamp((int) Math.ceil((double) itemCount / maxItemsPerRow), 1, maxItemRows);
		LayoutHelper layoutHelper = LayoutHelper.centeredHorizontal(itemCount, actualItemRows, 28, 28, 8);
		usedArea = layoutHelper.getArea();

		int centerX = width / 2;
		int centerY = height / 2;

		paginationState.iterateForCurrentPage((iPage, iOverall) -> {
			ItemEntry entry = items.get(iOverall);
			PonderButton b = new PonderButton(centerX + layoutHelper.getX() + 4, centerY + layoutHelper.getY() + 4)
					.showing(new ItemStack(entry.item))
					.withCallback((x, y) -> {
						if (!PonderIndex.getSceneAccess().doScenesExistForId(entry.key))
							return;

						centerScalingOn(x, y);
						ScreenOpener.transitionTo(PonderUI.of(new ItemStack(entry.item)));
					});
			paginatedWidgets.add(b);
			addRenderableWidget(b);
			layoutHelper.next();

		});

	}

	protected void updateAfterPaginationChange() {
		setupItemsForPage();

		prevPage.<PonderButton>setActive(paginationState.hasPreviousPage()).animateGradientFromState();
		nextPage.<PonderButton>setActive(paginationState.hasNextPage()).animateGradientFromState();
	}

	@Override
	protected void initBackTrackIcon(BoxWidget backTrack) {
		backTrack.showing(PonderGuiTextures.ICON_PONDER_IDENTIFY);
	}

	private boolean isItemIncluded(ItemEntry entry) {
		return exclusions
				.stream()
				.noneMatch(predicate -> predicate.test(entry.item));
	}

	@Override
	public void tick() {
		super.tick();
		PonderUI.ponderTicks++;

		hoveredItem = ItemStack.EMPTY;
		Window w = minecraft.getWindow();
		double mouseX = minecraft.mouseHandler.xpos() * w.getGuiScaledWidth() / w.getScreenWidth();
		double mouseY = minecraft.mouseHandler.ypos() * w.getGuiScaledHeight() / w.getScreenHeight();
		for (GuiEventListener child : children()) {
			if (child instanceof PonderButton button) {
				if (button.isMouseOver(mouseX, mouseY)) {
					hoveredItem = button.getItem() != null ? button.getItem() : ItemStack.EMPTY;
				}
			}
		}
	}

	@Override
	protected void renderWindow(PoseStack ms, int mouseX, int mouseY, float partialTicks) {
		super.renderWindow(ms, mouseX, mouseY, partialTicks);
		int centerX = width / 2;
		int centerY = height / 2;

		ms.pushPose();
		ms.translate(centerX, centerY, 0);

		UIRenderHelper.streak(ms, 0, usedArea.getX() - 10, usedArea.getY() - 20, 20, 220);
		font.draw(ms, "Items to inspect", usedArea.getX() - 5, usedArea.getY() - 25, Theme.Key.TEXT.i());

		ms.popPose();

		if (!paginationState.usesPagination())
			return;

		ms.pushPose();
		ms.translate(centerX, maxScreenArea.getY() + maxScreenArea.getHeight() + 14, 0);
		ms.scale(1.5f, 1.5f, 1);

		String pageString = "Page " + (paginationState.getPageIndex() + 1) + "/" + paginationState.getMaxPages();
		int stringWidth = font.width(pageString);

		UIRenderHelper.streak(ms, 0, 0, 4, 14, 85);
		UIRenderHelper.streak(ms, 180, 0, 4, 14, 85);
		font.draw(ms, pageString, -stringWidth / 2f, 0, Theme.Key.TEXT.i());

		ms.popPose();
	}

	@Override
	protected void renderWindowForeground(PoseStack ms, int mouseX, int mouseY, float partialTicks) {
		if (hoveredItem.isEmpty())
			return;

		ms.pushPose();
		ms.translate(0, 0, 200);

		renderTooltip(ms, hoveredItem, mouseX, mouseY);

		ms.popPose();
	}

	@Override
	public boolean isEquivalentTo(NavigatableSimiScreen other) {
		return other instanceof PonderIndexScreen;
	}

	public ItemStack getHoveredTooltipItem() {
		return hoveredItem;
	}

	@Override
	public boolean isPauseScreen() {
		return true;
	}

	public record ItemEntry(@Nullable ItemLike item, ResourceLocation key) {}

}
