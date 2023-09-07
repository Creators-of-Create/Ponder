package net.createmod.ponder.foundation.ui;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;

import net.createmod.catnip.gui.NavigatableSimiScreen;
import net.createmod.catnip.gui.ScreenOpener;
import net.createmod.catnip.gui.UIRenderHelper;
import net.createmod.catnip.gui.element.BoxElement;
import net.createmod.catnip.gui.widget.BoxWidget;
import net.createmod.catnip.platform.CatnipServices;
import net.createmod.catnip.utility.ClientFontHelper;
import net.createmod.catnip.utility.layout.LayoutHelper;
import net.createmod.catnip.utility.theme.Theme;
import net.createmod.ponder.Ponder;
import net.createmod.ponder.foundation.PonderChapter;
import net.createmod.ponder.foundation.PonderRegistry;
import net.createmod.ponder.foundation.PonderTag;
import net.createmod.ponder.foundation.PonderTheme;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;

public class PonderTagScreen extends AbstractPonderScreen {

	private final PonderTag tag;
	protected final List<ItemEntry> items = new ArrayList<>();
	private final double itemXmult = 0.5;
	@Nullable protected Rect2i itemArea;
	protected final List<PonderChapter> chapters = new ArrayList<>();
	private final double chapterXmult = 0.5;
	private final double chapterYmult = 0.75;
	@Nullable protected Rect2i chapterArea;
	private final double mainYmult = 0.15;

	private ItemStack hoveredItem = ItemStack.EMPTY;

	public PonderTagScreen(PonderTag tag) {
		this.tag = tag;
	}

	@Override
	protected void init() {
		super.init();

		// items
		items.clear();
		PonderRegistry.TAGS.getItems(tag)
			.stream()
			.map(key -> new ItemEntry(CatnipServices.REGISTRIES.getItemOrBlock(key), key))
			.filter(entry -> entry.item != null)
			.forEach(items::add);

		if (!tag.getMainItem().isEmpty())
			items.removeIf(entry -> entry.item == tag.getMainItem().getItem());

		int rowCount = Mth.clamp((int) Math.ceil(items.size() / 11d), 1, 3);
		LayoutHelper layout = LayoutHelper.centeredHorizontal(items.size(), rowCount, 28, 28, 8);
		itemArea = layout.getArea();
		int itemCenterX = (int) (width * itemXmult);
		int itemCenterY = getItemsY();

		for (ItemEntry entry : items) {
			PonderButton b = new PonderButton(itemCenterX + layout.getX() + 4, itemCenterY + layout.getY() + 4)
					.showing(new ItemStack(entry.item));

			if (PonderRegistry.ALL.containsKey(entry.key)) {
				b.withCallback((mouseX, mouseY) -> {
					centerScalingOn(mouseX, mouseY);
					ScreenOpener.transitionTo(PonderUI.of(new ItemStack(entry.item), tag));
				});
			} else {
				if (entry.key.getNamespace()
						.equals("minecraft"))
					b.withBorderColors(PonderTheme.Key.PONDER_MISSING_VANILLA.p())
							.animateColors(false);
				else
					b.withBorderColors(PonderTheme.Key.PONDER_MISSING_MODDED.p())
							.animateColors(false);
			}

			addRenderableWidget(b);
			layout.next();
		}

		if (!tag.getMainItem().isEmpty()) {
			ResourceLocation registryName = CatnipServices.REGISTRIES.getKeyOrThrow(tag.getMainItem().getItem());

			PonderButton b = new PonderButton(itemCenterX - layout.getTotalWidth() / 2 - 48, itemCenterY - 10)
					.showing(tag.getMainItem());
			//b.withCustomBackground(PonderTheme.Key.PONDER_BACKGROUND_IMPORTANT.c());

			if (PonderRegistry.ALL.containsKey(registryName)) {
				b.withCallback((mouseX, mouseY) -> {
					centerScalingOn(mouseX, mouseY);
					ScreenOpener.transitionTo(PonderUI.of(tag.getMainItem(), tag));
				});
			} else {
				if (registryName.getNamespace()
						.equals("minecraft"))
					b.withBorderColors(PonderTheme.Key.PONDER_MISSING_VANILLA.p())
							.animateColors(false);
				else
					b.withBorderColors(PonderTheme.Key.PONDER_MISSING_MODDED.p())
							.animateColors(false);
			}

			addRenderableWidget(b);
		}

		// chapters
		chapters.clear();
		chapters.addAll(PonderRegistry.TAGS.getChapters(tag));

		rowCount = Mth.clamp((int) Math.ceil(chapters.size() / 3f), 1, 3);
		layout = LayoutHelper.centeredHorizontal(chapters.size(), rowCount, 200, 38, 16);
		chapterArea = layout.getArea();
		int chapterCenterX = (int) (width * chapterXmult);
		int chapterCenterY = (int) (height * chapterYmult);

		for (PonderChapter chapter : chapters) {
			ChapterLabel label = new ChapterLabel(chapter, chapterCenterX + layout.getX(),
				chapterCenterY + layout.getY(), (mouseX, mouseY) -> {
					centerScalingOn(mouseX, mouseY);
					ScreenOpener.transitionTo(PonderUI.of(chapter));
				});

			addRenderableWidget(label);
			layout.next();
		}

	}

	@Override
	protected void initBackTrackIcon(BoxWidget backTrack) {
		backTrack.showing(tag);
	}

	@Override
	public void tick() {
		super.tick();
		PonderUI.ponderTicks++;

		hoveredItem = ItemStack.EMPTY;
		Window w = minecraft.getWindow();
		int mX = (int) (this.minecraft.mouseHandler.xpos() * (double) w.getGuiScaledWidth() / (double) w.getScreenWidth());
		int mY = (int) (this.minecraft.mouseHandler.ypos() * (double) w.getGuiScaledHeight() / (double) w.getScreenHeight());
		for (GuiEventListener child : children()) {
			if (child == backTrack)
				continue;
			if (child instanceof PonderButton button)
				if (button.isMouseOver(mX, mY)) {
					hoveredItem = button.getItem();
				}
		}
	}

	@Override
	protected void renderWindow(PoseStack ms, int mouseX, int mouseY, float partialTicks) {
		super.renderWindow(ms, mouseX, mouseY, partialTicks);
		renderItems(ms, mouseX, mouseY, partialTicks);

		renderChapters(ms, mouseX, mouseY, partialTicks);

		ms.pushPose();
		ms.translate(width / 2 - 120, height * mainYmult - 40, 0);

		ms.pushPose();
		//ms.translate(0, 0, 800);
		int x = 31 + 20 + 8;
		int y = 31;

		String title = tag.getTitle();

		int streakHeight = 35;
		UIRenderHelper.streak(ms, 0, x - 4, y - 12 + streakHeight / 2, streakHeight, 240);
		//PonderUI.renderBox(ms, 21, 21, 30, 30, false);
		new BoxElement()
				.withBackground(PonderTheme.Key.PONDER_BACKGROUND_FLAT.c())
				.gradientBorder(PonderTheme.Key.PONDER_IDLE.p())
				.at(21, 21, 100)
				.withBounds(30, 30)
				.render(ms);

		font.draw(ms, Ponder.lang().translate(AbstractPonderScreen.PONDERING).component(), x, y - 6, Theme.Key.TEXT_DARKER.i());
		y += 8;
		x += 0;
		ms.translate(x, y, 0);
		ms.translate(0, 0, 5);
		font.draw(ms, title, 0, 0, Theme.Key.TEXT.i());
		ms.popPose();

		ms.pushPose();
		ms.translate(23, 23, 10);
		ms.scale(1.66f, 1.66f, 1.66f);
		tag.render(ms, 0, 0);
		ms.popPose();
		ms.popPose();

		ms.pushPose();
		int w = (int) (width * .45);
		x = (width - w) / 2;
		y = getItemsY() - 10 + Math.max(itemArea.getHeight(), 48);

		String desc = tag.getDescription();
		int h = font.wordWrapHeight(desc, w);


		//PonderUI.renderBox(ms, x - 3, y - 3, w + 6, h + 6, false);
		new BoxElement()
				.withBackground(PonderTheme.Key.PONDER_BACKGROUND_FLAT.c())
				.gradientBorder(PonderTheme.Key.PONDER_IDLE.p())
				.at(x - 3, y - 3, 90)
				.withBounds(w + 6, h + 6)
				.render(ms);

		ms.translate(0, 0, 100);
		ClientFontHelper.drawSplitString(ms, font, desc, x, y, w, Theme.Key.TEXT.i());
		ms.popPose();
	}

	protected void renderItems(PoseStack ms, int mouseX, int mouseY, float partialTicks) {
		if (items.isEmpty())
			return;

		int x = (int) (width * itemXmult);
		int y = getItemsY();

		String relatedTitle = Ponder.lang().translate(AbstractPonderScreen.ASSOCIATED).string();
		int stringWidth = font.width(relatedTitle);

		ms.pushPose();
		ms.translate(x, y, 0);
		//PonderUI.renderBox(ms, (sWidth - stringWidth) / 2 - 5, itemArea.getY() - 21, stringWidth + 10, 10, false);
		new BoxElement()
				.withBackground(PonderTheme.Key.PONDER_BACKGROUND_FLAT.c())
				.gradientBorder(PonderTheme.Key.PONDER_IDLE.p())
				.at((windowWidth - stringWidth) / 2f - 5, itemArea.getY() - 21, 100)
				.withBounds(stringWidth + 10, 10)
				.render(ms);

		ms.translate(0, 0, 200);

//		UIRenderHelper.streak(0, itemArea.getX() - 10, itemArea.getY() - 20, 20, 180, 0x101010);
		drawCenteredString(ms, font, relatedTitle, windowWidth / 2, itemArea.getY() - 20, Theme.Key.TEXT.i());

		ms.translate(0,0, -200);

		UIRenderHelper.streak(ms, 0, 0, 0, itemArea.getHeight() + 10, itemArea.getWidth() / 2 + 75);
		UIRenderHelper.streak(ms, 180, 0, 0, itemArea.getHeight() + 10, itemArea.getWidth() / 2 + 75);

		ms.popPose();

	}

	public int getItemsY() {
		return (int) (mainYmult * height + 85);
	}

	protected void renderChapters(PoseStack ms, int mouseX, int mouseY, float partialTicks) {
		if (chapters.isEmpty())
			return;

		int chapterX = (int) (width * chapterXmult);
		int chapterY = (int) (height * chapterYmult);

		ms.pushPose();
		ms.translate(chapterX, chapterY, 0);

		UIRenderHelper.streak(ms, 0, chapterArea.getX() - 10, chapterArea.getY() - 20, 20, 220);
		font.draw(ms, "More Topics to Ponder about", chapterArea.getX() - 5, chapterArea.getY() - 25, Theme.Key.TEXT_ACCENT_SLIGHT.i());

		ms.popPose();
	}

	@Override
	protected void renderWindowForeground(PoseStack ms, int mouseX, int mouseY, float partialTicks) {
		RenderSystem.disableDepthTest();
		ms.pushPose();
		ms.translate(0, 0, 200);

		if (!hoveredItem.isEmpty()) {
			renderTooltip(ms, hoveredItem, mouseX, mouseY);
		}

		ms.popPose();
		RenderSystem.enableDepthTest();
	}

	@Override
	protected String getBreadcrumbTitle() {
		return tag.getTitle();
	}

	public ItemStack getHoveredTooltipItem() {
		return hoveredItem;
	}

	@Override
	public boolean isEquivalentTo(NavigatableSimiScreen other) {
		if (other instanceof PonderTagScreen)
			return tag == ((PonderTagScreen) other).tag;
		return super.isEquivalentTo(other);
	}

	@Override
	public boolean isPauseScreen() {
		return true;
	}

	public PonderTag getTag() {
		return tag;
	}

	@Override
	public void removed() {
		super.removed();
		hoveredItem = ItemStack.EMPTY;
	}

	public record ItemEntry(@Nullable ItemLike item, ResourceLocation key) {}

}
