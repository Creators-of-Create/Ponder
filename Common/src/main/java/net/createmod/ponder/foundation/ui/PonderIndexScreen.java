package net.createmod.ponder.foundation.ui;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
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
import net.createmod.catnip.utility.theme.Theme;
import net.createmod.ponder.enums.PonderGuiTextures;
import net.createmod.ponder.foundation.PonderChapter;
import net.createmod.ponder.foundation.PonderIndex;
import net.createmod.ponder.foundation.PonderPlugin;
import net.createmod.ponder.foundation.PonderRegistry;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;

public class PonderIndexScreen extends AbstractPonderScreen {

	protected final List<PonderChapter> chapters;
	private final double chapterXmult = 0.5;
	private final double chapterYmult = 0.3;
	@Nullable protected Rect2i chapterArea;

	protected final List<ItemEntry> items;
	private final double itemXmult = 0.5;
	private double itemYmult = 0.75;
	@Nullable protected Rect2i itemArea;

	private ItemStack hoveredItem = ItemStack.EMPTY;

	private final List<Predicate<ItemLike>> exclusions;

	public PonderIndexScreen() {
		chapters = new ArrayList<>();
		items = new ArrayList<>();
		// collect exclusions once at screen creation instead of every time they are needed
		exclusions = PonderIndex.streamPlugins()
				.flatMap(PonderPlugin::indexExclusions)
				.toList();
	}

	@Override
	protected void init() {
		super.init();

		chapters.clear();
		// chapters.addAll(PonderRegistry.CHAPTERS.getAllChapters());

		items.clear();
		PonderRegistry.ALL.keySet()
			.stream()
			.map(key -> new ItemEntry(CatnipServices.REGISTRIES.getItemOrBlock(key), key))
			.filter(entry -> entry.item != null)
			.filter(this::isItemIncluded)
			.forEach(items::add);

		boolean hasChapters = !chapters.isEmpty();

		// setup chapters
		LayoutHelper layout = LayoutHelper.centeredHorizontal(chapters.size(),
			Mth.clamp((int) Math.ceil(chapters.size() / 4f), 1, 4), 200, 38, 16);
		chapterArea = layout.getArea();
		int chapterCenterX = (int) (width * chapterXmult);
		int chapterCenterY = (int) (height * chapterYmult);

		// todo at some point pagination or horizontal scrolling may be needed for
		// chapters/items
		for (PonderChapter chapter : chapters) {
			ChapterLabel label = new ChapterLabel(chapter, chapterCenterX + layout.getX(),
				chapterCenterY + layout.getY(), (mouseX, mouseY) -> {
					centerScalingOn(mouseX, mouseY);
					ScreenOpener.transitionTo(PonderUI.of(chapter));
				});

			addRenderableWidget(label);
			layout.next();
		}

		// setup items
		if (!hasChapters) {
			itemYmult = 0.5;
		}

		int maxItemRows = hasChapters ? 4 : 7;
		layout = LayoutHelper.centeredHorizontal(items.size(),
			Mth.clamp((int) Math.ceil(items.size() / 11f), 1, maxItemRows), 28, 28, 8);
		itemArea = layout.getArea();
		int itemCenterX = (int) (width * itemXmult);
		int itemCenterY = (int) (height * itemYmult);

		items.sort(Comparator.comparing(ItemEntry::key));

		for (ItemEntry entry : items) {
			PonderButton b = new PonderButton(itemCenterX + layout.getX() + 4, itemCenterY + layout.getY() + 4)
					.showing(new ItemStack(entry.item))
					.withCallback((x, y) -> {
						if (!PonderRegistry.ALL.containsKey(entry.key))
							return;

						centerScalingOn(x, y);
						ScreenOpener.transitionTo(PonderUI.of(new ItemStack(entry.item)));
					});

			addRenderableWidget(b);
			layout.next();
		}

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
		int x = (int) (width * chapterXmult);
		int y = (int) (height * chapterYmult);

		if (!chapters.isEmpty()) {
			ms.pushPose();
			ms.translate(x, y, 0);

			UIRenderHelper.streak(ms, 0, chapterArea.getX() - 10, chapterArea.getY() - 20, 20, 220);
			font.draw(ms, "Topics to Ponder about", chapterArea.getX() - 5, chapterArea.getY() - 25, Theme.Key.TEXT.i());

			ms.popPose();
		}

		x = (int) (width * itemXmult);
		y = (int) (height * itemYmult);

		ms.pushPose();
		ms.translate(x, y, 0);

		UIRenderHelper.streak(ms, 0, itemArea.getX() - 10, itemArea.getY() - 20, 20, 220);
		font.draw(ms, "Items to inspect", itemArea.getX() - 5, itemArea.getY() - 25, Theme.Key.TEXT.i());

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
