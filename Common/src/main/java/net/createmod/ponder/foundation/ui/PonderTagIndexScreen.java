package net.createmod.ponder.foundation.ui;

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
import net.createmod.catnip.utility.FontHelper;
import net.createmod.catnip.utility.layout.LayoutHelper;
import net.createmod.catnip.utility.theme.Theme;
import net.createmod.ponder.Ponder;
import net.createmod.ponder.enums.PonderGuiTextures;
import net.createmod.ponder.foundation.PonderLocalization;
import net.createmod.ponder.foundation.PonderRegistry;
import net.createmod.ponder.foundation.PonderTag;
import net.createmod.ponder.foundation.PonderTheme;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.util.Mth;

public class PonderTagIndexScreen extends NavigatableSimiScreen {

	public static final String EXIT = PonderLocalization.LANG_PREFIX + "exit";
	public static final String TITLE = PonderLocalization.LANG_PREFIX + "index_title";
	public static final String WELCOME = PonderLocalization.LANG_PREFIX + "welcome";
	public static final String CATEGORIES = PonderLocalization.LANG_PREFIX + "categories";
	public static final String DESCRIPTION = PonderLocalization.LANG_PREFIX + "index_description";

	private final double itemXmult = 0.5;
	private final double mainYmult = 0.15;
	@Nullable protected Rect2i itemArea;
	@Nullable protected Rect2i chapterArea;

	@Nullable private PonderTag hoveredItem = null;

	// The ponder entry point from the menu. May be changed to include general
	// chapters in the future
	public PonderTagIndexScreen() {}

	@Override
	protected void init() {
		super.init();

		List<PonderTag> tags = PonderRegistry.TAGS.getListedTags();
		int rowCount = Mth.clamp((int) Math.ceil(tags.size() / 11d), 1, 3);
		LayoutHelper layout = LayoutHelper.centeredHorizontal(tags.size(), rowCount, 28, 28, 8);
		itemArea = layout.getArea();
		int itemCenterX = (int) (width * itemXmult);
		int itemCenterY = getItemsY();

		for (PonderTag i : tags) {
			PonderButton b =
				new PonderButton(itemCenterX + layout.getX() + 4, itemCenterY + layout.getY() + 4).showingTag(i)
					.withCallback((mouseX, mouseY) -> {
						centerScalingOn(mouseX, mouseY);
						ScreenOpener.transitionTo(new PonderTagScreen(i));
					});
			addRenderableWidget(b);
			layout.next();
		}

		addRenderableWidget(backTrack = new PonderButton(31, height - 31 - 20).enableFade(0, 5)
			.showing(PonderGuiTextures.ICON_PONDER_CLOSE)
			.withCallback(() -> ScreenOpener.openPreviousScreen(this, null)));
		backTrack.fade(1);
	}

	@Override
	protected void initBackTrackIcon(BoxWidget backTrack) {
		backTrack.showing(PonderGuiTextures.ICON_PONDER_IDENTIFY);
		/*backTrack.showing(GuiGameElement.of(AllItems.WRENCH.asStack())
				.scale(1.5f)
				.at(-4, -4)
		);*/
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
	protected Component backTrackingComponent() {
		return Ponder.lang()
				.translate(EXIT)
				.component();
	}

	@Override
	protected void renderWindow(PoseStack ms, int mouseX, int mouseY, float partialTicks) {
		renderItems(ms, mouseX, mouseY, partialTicks);

		ms.pushPose();
		ms.translate(width / 2 - 120, height * mainYmult - 40, 0);

		ms.pushPose();
		// ms.translate(0, 0, 800);
		int x = 31 + 20 + 8;
		int y = 31;

		String title = Ponder.lang().translate(WELCOME).string();

		int streakHeight = 35;
		UIRenderHelper.streak(ms, 0, x - 4, y - 12 + streakHeight / 2, streakHeight, 240);
		// PonderUI.renderBox(ms, 21, 21, 30, 30, false);
		new BoxElement().withBackground(PonderTheme.Key.PONDER_BACKGROUND_FLAT.c())
			.gradientBorder(PonderTheme.Key.PONDER_IDLE.p())
			.at(21, 21, 100)
			.withBounds(30, 30)
			.render(ms);

		font.draw(ms, title, x + 8, y + 1, Theme.Key.TEXT.i());
//		y += 8;
//		x += 0;
//		ms.translate(x, y, 0);
//		ms.translate(0, 0, 5);
//		textRenderer.draw(ms, title, 0, 0, Theme.i(Theme.Key.TEXT));
		ms.popPose();

		ms.pushPose();
		ms.translate(23, 23, 10);
		ms.scale(1.66f, 1.66f, 1.66f);
		ms.translate(-4, -4, 0);
		ms.scale(1.5f, 1.5f, 1.5f);
		//TODO
		//GuiGameElement.of(AllItems.WRENCH.asStack()).render(ms);
		ms.popPose();
		ms.popPose();

		ms.pushPose();
		int w = (int) (width * .45);
		x = (width - w) / 2;
		y = getItemsY() - 10 + Math.max(itemArea.getHeight(), 48);

		String desc = Ponder.lang().translate(DESCRIPTION).string();
		int h = font.wordWrapHeight(desc, w);

		// PonderUI.renderBox(ms, x - 3, y - 3, w + 6, h + 6, false);
		new BoxElement().withBackground(PonderTheme.Key.PONDER_BACKGROUND_FLAT.c())
			.gradientBorder(PonderTheme.Key.PONDER_IDLE.p())
			.at(x - 3, y - 3, 90)
			.withBounds(w + 6, h + 6)
			.render(ms);

		ms.translate(0, 0, 100);
		FontHelper.drawSplitString(ms, font, desc, x, y, w, Theme.Key.TEXT.i());
		ms.popPose();
	}

	protected void renderItems(PoseStack ms, int mouseX, int mouseY, float partialTicks) {
		List<PonderTag> tags = PonderRegistry.TAGS.getListedTags();
		if (tags.isEmpty())
			return;

		int x = (int) (width * itemXmult);
		int y = getItemsY();

		String relatedTitle = Ponder.lang().translate(CATEGORIES).string();
		int stringWidth = font.width(relatedTitle);

		ms.pushPose();
		ms.translate(x, y, 0);
		// PonderUI.renderBox(ms, (sWidth - stringWidth) / 2 - 5, itemArea.getY() - 21,
		// stringWidth + 10, 10, false);
		new BoxElement().withBackground(PonderTheme.Key.PONDER_BACKGROUND_FLAT.c())
			.gradientBorder(PonderTheme.Key.PONDER_IDLE.p())
			.at((windowWidth - stringWidth) / 2f - 5, itemArea.getY() - 21, 100)
			.withBounds(stringWidth + 10, 10)
			.render(ms);

		ms.translate(0, 0, 200);

//		UIRenderHelper.streak(0, itemArea.getX() - 10, itemArea.getY() - 20, 20, 180, 0x101010);
		drawCenteredString(ms, font, relatedTitle, windowWidth / 2, itemArea.getY() - 20, Theme.Key.TEXT.i());

		ms.translate(0, 0, -200);

		UIRenderHelper.streak(ms, 0, 0, 0, itemArea.getHeight() + 10, itemArea.getWidth() / 2 + 75);
		UIRenderHelper.streak(ms, 180, 0, 0, itemArea.getHeight() + 10, itemArea.getWidth() / 2 + 75);

		ms.popPose();

	}

	public int getItemsY() {
		return (int) (mainYmult * height + 85);
	}

	@Override
	protected void renderWindowForeground(PoseStack ms, int mouseX, int mouseY, float partialTicks) {
		RenderSystem.disableDepthTest();
		ms.pushPose();
		ms.translate(0, 0, 200);

		if (hoveredItem != null) {
			List<Component> list = FontHelper.cutStringTextComponent(hoveredItem.getDescription(),
				ChatFormatting.GRAY, ChatFormatting.GRAY);
			list.add(0, new TextComponent(hoveredItem.getTitle()));
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

}
