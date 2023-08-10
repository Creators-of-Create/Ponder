package net.createmod.ponder.foundation.ui;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.stream.IntStream;

import javax.annotation.Nullable;

import com.mojang.blaze3d.platform.ClipboardManager;
import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Matrix4f;
import com.mojang.math.Vector3f;

import net.createmod.catnip.gui.NavigatableSimiScreen;
import net.createmod.catnip.gui.ScreenOpener;
import net.createmod.catnip.gui.UIRenderHelper;
import net.createmod.catnip.gui.element.BoxElement;
import net.createmod.catnip.gui.element.GuiGameElement;
import net.createmod.catnip.gui.widget.BoxWidget;
import net.createmod.catnip.platform.CatnipServices;
import net.createmod.catnip.render.DefaultSuperRenderTypeBufferImpl;
import net.createmod.catnip.render.SuperRenderTypeBuffer;
import net.createmod.catnip.utility.Couple;
import net.createmod.catnip.utility.FontHelper;
import net.createmod.catnip.utility.Iterate;
import net.createmod.catnip.utility.Pair;
import net.createmod.catnip.utility.Pointing;
import net.createmod.catnip.utility.animation.LerpedFloat;
import net.createmod.catnip.utility.animation.LerpedFloat.Chaser;
import net.createmod.catnip.utility.lang.Components;
import net.createmod.catnip.utility.theme.Color;
import net.createmod.catnip.utility.theme.Theme;
import net.createmod.ponder.Ponder;
import net.createmod.ponder.enums.PonderConfig;
import net.createmod.ponder.enums.PonderGuiTextures;
import net.createmod.ponder.foundation.PonderChapter;
import net.createmod.ponder.foundation.PonderRegistry;
import net.createmod.ponder.foundation.PonderScene;
import net.createmod.ponder.foundation.PonderScene.SceneTransform;
import net.createmod.ponder.foundation.PonderStoryBoardEntry;
import net.createmod.ponder.foundation.PonderTag;
import net.createmod.ponder.foundation.PonderTheme;
import net.createmod.ponder.foundation.PonderWorld;
import net.createmod.ponder.foundation.content.DebugScenes;
import net.createmod.ponder.foundation.element.TextWindowElement;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.phys.Vec3;

public class PonderUI extends AbstractPonderScreen {

	public static int ponderTicks;
	public static float ponderPartialTicksPaused;

	private final List<PonderScene> scenes;
	private final List<PonderTag> tags;
	private List<PonderButton> tagButtons = new ArrayList<>();
	private List<LerpedFloat> tagFades = new ArrayList<>();
	private final LerpedFloat fadeIn;
	ItemStack stack;
	@Nullable PonderChapter chapter = null;

	private boolean userViewMode;
	private boolean identifyMode;
	private ItemStack hoveredTooltipItem = ItemStack.EMPTY;
	@Nullable private BlockPos hoveredBlockPos;

	private final ClipboardManager clipboardHelper;
	@Nullable private BlockPos copiedBlockPos;

	private final LerpedFloat finishingFlash;
	private final LerpedFloat nextUp;
	private int finishingFlashWarmup = 0;
	private int nextUpWarmup = 0;

	private final LerpedFloat lazyIndex;
	private int index = 0;
	@Nullable private PonderTag referredToByTag;

	private PonderButton left, right, scan, chap, userMode, close, replay, slowMode;
	private int skipCooling = 0;

	private int extendedTickLength = 0;
	private int extendedTickTimer = 0;

	public static PonderUI of(ResourceLocation id) {
		return new PonderUI(PonderRegistry.compile(id));
	}

	public static PonderUI of(ItemStack item) {
		return new PonderUI(PonderRegistry.compile(CatnipServices.REGISTRIES.getKeyOrThrow(item.getItem())));
	}

	public static PonderUI of(ItemStack item, PonderTag tag) {
		PonderUI ponderUI = new PonderUI(PonderRegistry.compile(CatnipServices.REGISTRIES.getKeyOrThrow(item.getItem())));
		ponderUI.referredToByTag = tag;
		return ponderUI;
	}

	public static PonderUI of(PonderChapter chapter) {
		PonderUI ui = new PonderUI(PonderRegistry.compile(chapter));
		ui.chapter = chapter;
		return ui;
	}

	protected PonderUI(List<PonderScene> scenes) {
		ResourceLocation location = scenes.get(0).getLocation();
		stack = new ItemStack(CatnipServices.REGISTRIES.getItemOrBlock(location));

		tags = new ArrayList<>(PonderRegistry.TAGS.getTags(location));
		this.scenes = scenes;
		if (scenes.isEmpty()) {
			List<PonderStoryBoardEntry> list = Collections.singletonList(new PonderStoryBoardEntry(DebugScenes::empty,
				Ponder.MOD_ID, "debug/scene_1", new ResourceLocation("minecraft", "stick")));
			scenes.addAll(PonderRegistry.compile(list));
		}
		lazyIndex = LerpedFloat.linear()
			.startWithValue(index);
		fadeIn = LerpedFloat.linear()
			.startWithValue(0)
			.chase(1, .1f, Chaser.EXP);
		clipboardHelper = new ClipboardManager();
		finishingFlash = LerpedFloat.linear()
			.startWithValue(0)
			.chase(0, .1f, Chaser.EXP);
		nextUp = LerpedFloat.linear()
			.startWithValue(0)
			.chase(0, .4f, Chaser.EXP);
	}

	@Override
	protected void init() {
		super.init();

		tagButtons = new ArrayList<>();
		tagFades = new ArrayList<>();

		tags.forEach(t -> {
			int i = tagButtons.size();
			int x = 31;
			int y = 81 + i * 30;

			PonderButton b2 = new PonderButton(x, y).showing(t)
				.withCallback((mX, mY) -> {
					centerScalingOn(mX, mY);
					ScreenOpener.transitionTo(new PonderTagScreen(t));
				});

			addRenderableWidget(b2);
			tagButtons.add(b2);

			LerpedFloat chase = LerpedFloat.linear()
				.startWithValue(0)
				.chase(0, .05f, Chaser.exp(.1));
			tagFades.add(chase);

		});

		/*
		 * if (chapter != null) { widgets.add(chap = new PonderButton(width - 31 - 24,
		 * 31, () -> { }).showing(chapter)); }
		 */

		Options bindings = minecraft.options;
		int spacing = 8;
		int bX = (width - 20) / 2 - (70 + 2 * spacing);
		int bY = height - 20 - 31;

		{
			int pX = (width / 2) - 110;
			int pY = bY + 20 + 4;
			int pW = width - 2 * pX;
			addRenderableWidget(new PonderProgressBar(this, pX, pY, pW, 1));
		}

		addRenderableWidget(scan = new PonderButton(bX, bY).withShortcut(bindings.keyDrop)
			.showing(PonderGuiTextures.ICON_PONDER_IDENTIFY)
			.enableFade(0, 5)
			.withCallback(() -> {
				identifyMode = !identifyMode;
				if (!identifyMode)
					scenes.get(index)
						.deselect();
				else
					ponderPartialTicksPaused = minecraft.getFrameTime();
			}));
		scan.atZLevel(600);

		addRenderableWidget(slowMode = new PonderButton(width - 20 - 31, bY).showing(PonderGuiTextures.ICON_PONDER_SLOW_MODE)
			.enableFade(0, 5)
			.withCallback(() -> setComfyReadingEnabled(!isComfyReadingEnabled())));

		if (PonderRegistry.editingModeActive()) {
			addRenderableWidget(userMode = new PonderButton(width - 50 - 31, bY).showing(PonderGuiTextures.ICON_PONDER_USER_MODE)
				.enableFade(0, 5)
				.withCallback(() -> userViewMode = !userViewMode));
		}

		bX += 50 + spacing;
		addRenderableWidget(left = new PonderButton(bX, bY).withShortcut(bindings.keyLeft)
			.showing(PonderGuiTextures.ICON_PONDER_LEFT)
			.enableFade(0, 5)
			.withCallback(() -> this.scroll(false)));

		bX += 20 + spacing;
		addRenderableWidget(close = new PonderButton(bX, bY).withShortcut(bindings.keyInventory)
			.showing(PonderGuiTextures.ICON_PONDER_CLOSE)
			.enableFade(0, 5)
			.withCallback(this::onClose));

		bX += 20 + spacing;
		addRenderableWidget(right = new PonderButton(bX, bY).withShortcut(bindings.keyRight)
			.showing(PonderGuiTextures.ICON_PONDER_RIGHT)
			.enableFade(0, 5)
			.withCallback(() -> this.scroll(true)));

		bX += 50 + spacing;
		addRenderableWidget(replay = new PonderButton(bX, bY).withShortcut(bindings.keyDown)
			.showing(PonderGuiTextures.ICON_PONDER_REPLAY)
			.enableFade(0, 5)
			.withCallback(this::replay));
	}

	@Override
	protected void initBackTrackIcon(BoxWidget backTrack) {
		backTrack.showingElement(GuiGameElement.of(stack)
				.scale(1.5f)
				.at(-4, -4)
		);
	}

	@Override
	public void tick() {
		super.tick();

		if (skipCooling > 0)
			skipCooling--;

		if (referredToByTag != null) {
			for (int i = 0; i < scenes.size(); i++) {
				PonderScene ponderScene = scenes.get(i);
				if (!ponderScene.getTags()
					.contains(referredToByTag))
					continue;
				if (i == index)
					break;
				scenes.get(index)
					.fadeOut();
				index = i;
				scenes.get(index)
					.begin();
				lazyIndex.chase(index, 1 / 4f, Chaser.EXP);
				identifyMode = false;
				break;
			}
			referredToByTag = null;
		}

		lazyIndex.tickChaser();
		fadeIn.tickChaser();
		finishingFlash.tickChaser();
		nextUp.tickChaser();
		PonderScene activeScene = scenes.get(index);

		extendedTickLength = 0;
		if (isComfyReadingEnabled())
			activeScene.forEachVisible(TextWindowElement.class, twe -> extendedTickLength = 2);

		if (extendedTickTimer == 0) {
			if (!identifyMode) {
				ponderTicks++;
				if (skipCooling == 0)
					activeScene.tick();
			}

			if (!identifyMode) {
				float lazyIndexValue = lazyIndex.getValue();
				if (Math.abs(lazyIndexValue - index) > 1 / 512f)
					scenes.get(lazyIndexValue < index ? index - 1 : index + 1)
						.tick();
			}
			extendedTickTimer = extendedTickLength;
		} else
			extendedTickTimer--;

		if (activeScene.getCurrentTime() == activeScene.getTotalTime() - 1) {
			finishingFlashWarmup = 30;
			nextUpWarmup = 50;
		}

		if (finishingFlashWarmup > 0) {
			finishingFlashWarmup--;
			if (finishingFlashWarmup == 0) {
				finishingFlash.setValue(1);
				finishingFlash.setValue(1);
			}
		}

		if (nextUpWarmup > 0) {
			nextUpWarmup--;
			if (nextUpWarmup == 0)
				nextUp.updateChaseTarget(1);
		}

		updateIdentifiedItem(activeScene);
	}

	public PonderScene getActiveScene() {
		return scenes.get(index);
	}

	public void seekToTime(int time) {
		if (getActiveScene().getCurrentTime() > time)
			replay();

		getActiveScene().seekToTime(time);
		if (time != 0)
			coolDownAfterSkip();
	}

	public void updateIdentifiedItem(PonderScene activeScene) {
		hoveredTooltipItem = ItemStack.EMPTY;
		hoveredBlockPos = null;
		if (!identifyMode)
			return;

		Window w = minecraft.getWindow();
		double mouseX = minecraft.mouseHandler.xpos() * w.getGuiScaledWidth() / w.getScreenWidth();
		double mouseY = minecraft.mouseHandler.ypos() * w.getGuiScaledHeight() / w.getScreenHeight();
		SceneTransform t = activeScene.getTransform();
		Vec3 vec1 = t.screenToScene(mouseX, mouseY, 1000, 0);
		Vec3 vec2 = t.screenToScene(mouseX, mouseY, -100, 0);
		Pair<ItemStack, BlockPos> pair = activeScene.rayTraceScene(vec1, vec2);
		hoveredTooltipItem = pair.getFirst();
		hoveredBlockPos = pair.getSecond();
	}

	@Override
	public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
		if (scroll(delta > 0))
			return true;
		return super.mouseScrolled(mouseX, mouseY, delta);
	}

	protected void replay() {
		identifyMode = false;
		PonderScene scene = scenes.get(index);

		if (hasShiftDown()) {
			List<PonderStoryBoardEntry> list = PonderRegistry.ALL.get(scene.getLocation());
			PonderStoryBoardEntry sb = list.get(index);
			StructureTemplate activeTemplate = PonderRegistry.loadSchematic(sb.getSchematicLocation());
			PonderWorld world = new PonderWorld(BlockPos.ZERO, Minecraft.getInstance().level);
			activeTemplate.placeInWorld(world, BlockPos.ZERO, BlockPos.ZERO, new StructurePlaceSettings(), new Random(),
				Block.UPDATE_CLIENTS);
			world.createBackup();
			scene = PonderRegistry.compileScene(index, sb, world);
			scene.begin();
			scenes.set(index, scene);
		}

		scene.begin();
	}

	protected boolean scroll(boolean forward) {
		int prevIndex = index;
		index = forward ? index + 1 : index - 1;
		index = Mth.clamp(index, 0, scenes.size() - 1);
		if (prevIndex != index) {// && Math.abs(index - lazyIndex.getValue()) < 1.5f) {
			scenes.get(prevIndex)
				.fadeOut();
			scenes.get(index)
				.begin();
			lazyIndex.chase(index, 1 / 4f, Chaser.EXP);
			identifyMode = false;
			return true;
		} else
			index = prevIndex;
		return false;
	}

	@Override
	protected void renderWindow(PoseStack ms, int mouseX, int mouseY, float partialTicks) {
		partialTicks = getPartialTicks();
		RenderSystem.enableBlend();
		renderVisibleScenes(ms, mouseX, mouseY,
			skipCooling > 0 ? 0 : identifyMode ? ponderPartialTicksPaused : partialTicks);
		renderWidgets(ms, mouseX, mouseY, identifyMode ? ponderPartialTicksPaused : partialTicks);
	}

	@Override
	public void renderBackground(PoseStack ms) {
		super.renderBackground(ms);
	}

	protected void renderVisibleScenes(PoseStack ms, int mouseX, int mouseY, float partialTicks) {
		renderScene(ms, mouseX, mouseY, index, partialTicks);
		float lazyIndexValue = lazyIndex.getValue(partialTicks);
		if (Math.abs(lazyIndexValue - index) > 1 / 512f)
			renderScene(ms, mouseX, mouseY, lazyIndexValue < index ? index - 1 : index + 1, partialTicks);
	}

	protected void renderScene(PoseStack ms, int mouseX, int mouseY, int i, float partialTicks) {
		SuperRenderTypeBuffer buffer = DefaultSuperRenderTypeBufferImpl.getInstance();
		PonderScene scene = scenes.get(i);
		double value = lazyIndex.getValue(minecraft.getFrameTime());
		double diff = i - value;
		double slide = Mth.lerp(diff * diff, 200, 600) * diff;

		RenderSystem.enableBlend();
		RenderSystem.enableDepthTest();
		RenderSystem.backupProjectionMatrix();

		// has to be outside of MS transforms, important for vertex sorting
		Matrix4f matrix4f = new Matrix4f(RenderSystem.getProjectionMatrix());
		matrix4f.multiplyWithTranslation(0, 0, 800);
		RenderSystem.setProjectionMatrix(matrix4f);

		ms.pushPose();
		ms.translate(0, 0, -800);
		scene.getTransform()
			.updateScreenParams(width, height, slide);
		scene.getTransform()
			.apply(ms, partialTicks);
		scene.getTransform()
			.updateSceneRVE(partialTicks);
		scene.renderScene(buffer, ms, partialTicks);
		buffer.draw();

		BoundingBox bounds = scene.getBounds();
		ms.pushPose();

		// kool shadow fx
		if (!scene.shouldHidePlatformShadow()) {
			RenderSystem.enableCull();
			RenderSystem.enableDepthTest();
			ms.pushPose();
			ms.translate(scene.getBasePlateOffsetX(), 0, scene.getBasePlateOffsetZ());
			UIRenderHelper.flipForGuiRender(ms);

			float flash = finishingFlash.getValue(partialTicks) * .9f;
			float alpha = flash;
			flash *= flash;
			flash = ((flash * 2) - 1);
			flash *= flash;
			flash = 1 - flash;

			for (int f = 0; f < 4; f++) {
				ms.translate(scene.getBasePlateSize(), 0, 0);
				ms.pushPose();
				ms.translate(0, 0, -1 / 1024f);
				if (flash > 0) {
					ms.pushPose();
					ms.scale(1, .5f + flash * .75f, 1);
					UIRenderHelper.drawGradientRect(ms.last().pose(), 0, 0, -1, -scene.getBasePlateSize(), 0, new Color(0x00_c6ffc9), new Color(0xaa_c6ffc9).scaleAlpha(alpha));
					ms.popPose();
				}
				ms.translate(0, 0, 2 / 1024f);
				UIRenderHelper.drawGradientRect(ms.last().pose(), 0, 0, 0, -scene.getBasePlateSize(), 4, new Color(0x66_000000), new Color(0x00_000000));
				ms.popPose();
				ms.mulPose(Vector3f.YP.rotationDegrees(-90));
			}
			ms.popPose();
			RenderSystem.disableCull();
			RenderSystem.disableDepthTest();
		}

		// coords for debug
		if (PonderRegistry.editingModeActive() && !userViewMode) {

			ms.scale(-1, -1, 1);
			ms.scale(1 / 16f, 1 / 16f, 1 / 16f);
			ms.translate(1, -8, -1 / 64f);

			// X AXIS
			ms.pushPose();
			ms.translate(4, -3, 0);
			ms.translate(0, 0, -2 / 1024f);
			for (int x = 0; x <= bounds.getXSpan(); x++) {
				ms.translate(-16, 0, 0);
				font.draw(ms, x == bounds.getXSpan() ? "x" : "" + x, 0, 0, 0xFFFFFFFF);
			}
			ms.popPose();

			// Z AXIS
			ms.pushPose();
			ms.scale(-1, 1, 1);
			ms.translate(0, -3, -4);
			ms.mulPose(Vector3f.YP.rotationDegrees(-90));
			ms.translate(-8, -2, 2 / 64f);
			for (int z = 0; z <= bounds.getZSpan(); z++) {
				ms.translate(16, 0, 0);
				font.draw(ms, z == bounds.getZSpan() ? "z" : "" + z, 0, 0, 0xFFFFFFFF);
			}
			ms.popPose();

			// DIRECTIONS
			ms.pushPose();
			ms.translate(bounds.getXSpan() * -8, 0, bounds.getZSpan() * 8);
			ms.mulPose(Vector3f.YP.rotationDegrees(-90));
			for (Direction d : Iterate.horizontalDirections) {
				ms.mulPose(Vector3f.YP.rotationDegrees(90));
				ms.pushPose();
				ms.translate(0, 0, bounds.getZSpan() * 16);
				ms.mulPose(Vector3f.XP.rotationDegrees(-90));
				font.draw(ms, d.name()
					.substring(0, 1), 0, 0, 0x66FFFFFF);
				font.draw(ms, "|", 2, 10, 0x44FFFFFF);
				font.draw(ms, ".", 2, 14, 0x22FFFFFF);
				ms.popPose();
			}
			ms.popPose();
			buffer.draw();
		}

		ms.popPose();
		ms.popPose();
		RenderSystem.restoreProjectionMatrix();
	}

	protected void renderWidgets(PoseStack ms, int mouseX, int mouseY, float partialTicks) {
		RenderSystem.disableDepthTest();

		float fade = fadeIn.getValue(partialTicks);
		float lazyIndexValue = lazyIndex.getValue(partialTicks);
		float indexDiff = Math.abs(lazyIndexValue - index);
		PonderScene activeScene = scenes.get(index);
		PonderScene nextScene = scenes.size() > index + 1 ? scenes.get(index + 1) : null;

		boolean noWidgetsHovered = true;
		for (GuiEventListener child : children())
			noWidgetsHovered &= !child.isMouseOver(mouseX, mouseY);

		int tooltipColor = Theme.Key.TEXT_DARKER.i();
		renderChapterTitle(ms, fade, indexDiff, activeScene, tooltipColor);

		if (identifyMode) {
			if (noWidgetsHovered && mouseY < height - 80) {
				ms.pushPose();
				ms.translate(mouseX, mouseY, 100);
				if (hoveredTooltipItem.isEmpty()) {

					MutableComponent text = Ponder.lang()
							.translate(AbstractPonderScreen.IDENTIFY_MODE, ((MutableComponent) minecraft.options.keyDrop.getTranslatedKeyMessage())
									.withStyle(ChatFormatting.WHITE))
							.style(ChatFormatting.GRAY)
							.component();

					renderComponentTooltip(
							ms,
							font.getSplitter()
									.splitLines(text, width / 3, Style.EMPTY)
									.stream()
									.map(t -> (Component) Components.literal(t.getString()))
									.toList(),
							0,
							0
					);
				} else
					renderTooltip(ms, hoveredTooltipItem, 0, 0);
				if (hoveredBlockPos != null && PonderRegistry.editingModeActive() && !userViewMode) {
					ms.translate(0, -15, 0);
					boolean copied = hoveredBlockPos.equals(copiedBlockPos);
					MutableComponent coords = Components.literal(
						hoveredBlockPos.getX() + ", " + hoveredBlockPos.getY() + ", " + hoveredBlockPos.getZ())
							.withStyle(copied ? ChatFormatting.GREEN : ChatFormatting.GOLD);
					renderTooltip(ms, coords, 0, 0);
				}
				ms.popPose();
			}
			scan.flash();
		} else {
			scan.dim();
		}

		if (PonderRegistry.editingModeActive()) {
			if (userViewMode)
				userMode.flash();
			else
				userMode.dim();
		}

		if (isComfyReadingEnabled())
			slowMode.flash();
		else
			slowMode.dim();

		renderSceneOverlay(ms, partialTicks, lazyIndexValue, indexDiff);

		renderNextUp(ms, partialTicks, nextScene);

		// Widgets
		getWidgets().forEach(w -> {
			if (w instanceof PonderButton button) {
				button.fade()
					.startWithValue(fade);
			}
		});

		if (index == 0 || index == 1 && lazyIndexValue < index)
			left.fade()
				.startWithValue(lazyIndexValue);
		if (index == scenes.size() - 1 || index == scenes.size() - 2 && lazyIndexValue > index)
			right.fade()
				.startWithValue(scenes.size() - lazyIndexValue - 1);

		if (activeScene.isFinished())
			right.flash();
		else {
			right.dim();
			nextUp.updateChaseTarget(0);
		}

		// Tags
		List<PonderTag> sceneTags = activeScene.getTags();
		boolean highlightAll = sceneTags.contains(PonderTag.Highlight.ALL);
		double s = Minecraft.getInstance()
			.getWindow()
			.getGuiScale();
		IntStream.range(0, tagButtons.size())
			.forEach(i -> {
				ms.pushPose();
				PonderTag tag = this.tags.get(i);
				LerpedFloat chase = tagFades.get(i);
				PonderButton button = tagButtons.get(i);
				if (button.isMouseOver(mouseX, mouseY)) {
					chase.updateChaseTarget(1);
				} else
					chase.updateChaseTarget(0);

				chase.tickChaser();

				if (highlightAll || sceneTags.contains(tag))
					button.flash();
				else
					button.dim();

				int x = button.x + button.getWidth() + 4;
				int y = button.y - 2;
				ms.translate(x, y + 5 * (1 - fade), 800);

				float fadedWidth = 200 * chase.getValue(partialTicks);
				UIRenderHelper.streak(ms, 0, 0, 12, 26, (int) fadedWidth);

				RenderSystem.enableScissor((int) (x * s), 0, (int) (fadedWidth * s), (int) (height * s));

				String tagName = tag
					.getTitle();
				font.draw(ms, tagName, 3, 8, Theme.Key.TEXT_ACCENT_SLIGHT.i());

				RenderSystem.disableScissor();

				ms.popPose();
			});

		renderHoverTooltips(ms, tooltipColor);

		RenderSystem.enableDepthTest();
	}

	private void renderHoverTooltips(PoseStack ms, int tooltipColor) {
		ms.pushPose();
		ms.translate(0, 0, 500);
		int tooltipY = height - 16;
		if (scan.isHoveredOrFocused())
			drawCenteredString(ms, font, Ponder.lang().translate(AbstractPonderScreen.IDENTIFY).component(), scan.x + 10, tooltipY, tooltipColor);
		if (index != 0 && left.isHoveredOrFocused())
			drawCenteredString(ms, font, Ponder.lang().translate(AbstractPonderScreen.PREVIOUS).component(), left.x + 10, tooltipY, tooltipColor);
		if (close.isHoveredOrFocused())
			drawCenteredString(ms, font, Ponder.lang().translate(AbstractPonderScreen.CLOSE).component(), close.x + 10, tooltipY, tooltipColor);
		if (index != scenes.size() - 1 && right.isHoveredOrFocused())
			drawCenteredString(ms, font, Ponder.lang().translate(AbstractPonderScreen.NEXT).component(), right.x + 10, tooltipY, tooltipColor);
		if (replay.isHoveredOrFocused())
			drawCenteredString(ms, font, Ponder.lang().translate(AbstractPonderScreen.REPLAY).component(), replay.x + 10, tooltipY, tooltipColor);
		if (slowMode.isHoveredOrFocused())
			drawCenteredString(ms, font, Ponder.lang().translate(AbstractPonderScreen.SLOW_TEXT).component(), slowMode.x + 5, tooltipY, tooltipColor);
		if (PonderRegistry.editingModeActive() && userMode.isHoveredOrFocused())
			drawCenteredString(ms, font, "Editor View", userMode.x + 10, tooltipY, tooltipColor);
		ms.popPose();
	}

	private void renderNextUp(PoseStack ms, float partialTicks, @Nullable PonderScene nextScene) {
		if (!getActiveScene().isFinished())
			return;

		if(nextScene == null || !nextScene.isNextUpEnabled())
			return;

		if (!(nextUp.getValue() > 1 / 16f))
			return;

		ms.pushPose();
		ms.translate(right.x + 10, right.y - 6 + nextUp.getValue(partialTicks) * 5, 400);
		MutableComponent nextUpComponent = Ponder.lang().translate(AbstractPonderScreen.NEXT_UP).component();
		int boxWidth = (Math.max(font.width(nextScene.getTitle()), font.width(nextUpComponent)) + 5);
		renderSpeechBox(ms, 0, 0, boxWidth, 20, right.isHoveredOrFocused(), Pointing.DOWN, false);
		ms.translate(0, -29, 100);
		drawCenteredString(ms, font, nextUpComponent, 0, 0, Theme.Key.TEXT_DARKER.i());
		drawCenteredString(ms, font, nextScene.getTitle(), 0, 10, Theme.Key.TEXT.i());
		ms.popPose();
	}

	private void renderSceneOverlay(PoseStack ms, float partialTicks, float lazyIndexValue, float indexDiff) {
		// Scene overlay
		float scenePT = skipCooling > 0 ? 0 : partialTicks;
		ms.pushPose();
		ms.translate(0, 0, 100);
		renderOverlay(ms, index, scenePT);
		if (indexDiff > 1 / 512f)
			renderOverlay(ms, lazyIndexValue < index ? index - 1 : index + 1, scenePT);
		ms.popPose();
	}

	private void renderChapterTitle(PoseStack ms, float fade, float indexDiff, PonderScene activeScene, int tooltipColor) {
		// Chapter title
		ms.pushPose();
		ms.translate(0, 0, 400);
		int x = 31 + 20 + 8;
		int y = 31;

		String title = activeScene.getTitle();
		int wordWrappedHeight = font.wordWrapHeight(title, left.x - 51);

		int streakHeight = 35 - 9 + wordWrappedHeight;
		UIRenderHelper.streak(ms, 0, x - 4, y - 12 + streakHeight / 2, streakHeight, (int) (150 * fade));
		UIRenderHelper.streak(ms, 180, x - 4, y - 12 + streakHeight / 2, streakHeight, (int) (30 * fade));
		new BoxElement().withBackground(PonderTheme.Key.PONDER_BACKGROUND_FLAT.c())
			.gradientBorder(PonderTheme.Key.PONDER_IDLE.p())
			.at(21, 21, 100)
			.withBounds(30, 30)
			.render(ms);

		GuiGameElement.of(stack)
			.scale(2)
			.at(x - 39, y - 11)
			.render(ms);

		font.draw(ms, Ponder.lang().translate(AbstractPonderScreen.PONDERING).component(), x, y - 6, tooltipColor);
		y += 8;
		x += 0;
		ms.translate(x, y, 0);
		ms.mulPose(Vector3f.XN.rotationDegrees(indexDiff * -75));
		ms.translate(0, 0, 5);
		FontHelper.drawSplitString(ms, font, title, 0, 0, left.x - 51, Theme.Key.TEXT.c()
			.scaleAlpha(1 - indexDiff)
			.getRGB());
		ms.popPose();

		if (chapter != null) {
			ms.pushPose();

			ms.translate(chap.x - 4 - 4, chap.y, 0);
			UIRenderHelper.streak(ms, 180, 4, 10, 26, (int) (150 * fade));

			drawRightAlignedString(font, ms, Ponder.lang().translate(AbstractPonderScreen.IN_CHAPTER).string(), 0, 0, tooltipColor);
			drawRightAlignedString(font, ms, chapter.getTitle(), 0, 12, Theme.Key.TEXT.i());

			ms.popPose();
		}

		Color c1 = Theme.Key.NAV_BACK_ARROW.c().setAlpha(0x40);
		Color c2 = Theme.Key.NAV_BACK_ARROW.c().setAlpha(0x20);
		Color c3 = Theme.Key.NAV_BACK_ARROW.c().setAlpha(0x10);
		UIRenderHelper.breadcrumbArrow(ms, width / 2 - 20, height - 51, 0, 20, 20, 5, c1, c2);
		UIRenderHelper.breadcrumbArrow(ms, width / 2 + 20, height - 51, 0, -20, 20, -5, c1, c2);
		UIRenderHelper.breadcrumbArrow(ms, width / 2 - 90, height - 51, 0, 70, 20, 5, c1, c3);
		UIRenderHelper.breadcrumbArrow(ms, width / 2 + 90, height - 51, 0, -70, 20, -5, c1, c3);
	}

	private void renderOverlay(PoseStack ms, int i, float partialTicks) {
		if (identifyMode)
			return;
		ms.pushPose();
		PonderScene story = scenes.get(i);
		story.renderOverlay(this, ms, skipCooling > 0 ? 0 : identifyMode ? ponderPartialTicksPaused : partialTicks);
		ms.popPose();
	}

	@Override
	public boolean mouseClicked(double x, double y, int button) {
		if (identifyMode && hoveredBlockPos != null && PonderRegistry.editingModeActive()) {
			long handle = minecraft.getWindow()
				.getWindow();
			if (copiedBlockPos != null && button == 1) {
				clipboardHelper.setClipboard(handle,
					"util.select.fromTo(" + copiedBlockPos.getX() + ", " + copiedBlockPos.getY() + ", "
						+ copiedBlockPos.getZ() + ", " + hoveredBlockPos.getX() + ", " + hoveredBlockPos.getY() + ", "
						+ hoveredBlockPos.getZ() + ")");
				copiedBlockPos = hoveredBlockPos;
				return true;
			}

			if (hasShiftDown())
				clipboardHelper.setClipboard(handle, "util.select.position(" + hoveredBlockPos.getX() + ", "
					+ hoveredBlockPos.getY() + ", " + hoveredBlockPos.getZ() + ")");
			else
				clipboardHelper.setClipboard(handle, "util.grid.at(" + hoveredBlockPos.getX() + ", "
					+ hoveredBlockPos.getY() + ", " + hoveredBlockPos.getZ() + ")");
			copiedBlockPos = hoveredBlockPos;
			return true;
		}

		return super.mouseClicked(x, y, button);
	}

	@Override
	protected String getBreadcrumbTitle() {
		if (chapter != null)
			return chapter.getTitle();

		return stack.getItem()
			.getDescription()
			.getString();
	}

	public Font getFontRenderer() {
		return font;
	}

	protected boolean isMouseOver(double mouseX, double mouseY, int x, int y, int w, int h) {
		boolean hovered = !(mouseX < x || mouseX > x + w);
		hovered &= !(mouseY < y || mouseY > y + h);
		return hovered;
	}

	public static void renderSpeechBox(PoseStack ms, int x, int y, int w, int h, boolean highlighted, Pointing pointing,
		boolean returnWithLocalTransform) {
		if (!returnWithLocalTransform)
			ms.pushPose();

		int boxX = x;
		int boxY = y;
		int divotX = x;
		int divotY = y;
		int divotRotation = 0;
		int divotSize = 8;
		int distance = 1;
		int divotRadius = divotSize / 2;
		Couple<Color> borderColors = (highlighted ? PonderTheme.Key.PONDER_BUTTON_HOVER : PonderTheme.Key.PONDER_IDLE).p();
		Color c;

		switch (pointing) {
		default:
		case DOWN:
			divotRotation = 0;
			boxX -= w / 2;
			boxY -= h + divotSize + 1 + distance;
			divotX -= divotRadius;
			divotY -= divotSize + distance;
			c = borderColors.getSecond();
			break;
		case LEFT:
			divotRotation = 90;
			boxX += divotSize + 1 + distance;
			boxY -= h / 2;
			divotX += distance;
			divotY -= divotRadius;
			c = Color.mixColors(borderColors, 0.5f);
			break;
		case RIGHT:
			divotRotation = 270;
			boxX -= w + divotSize + 1 + distance;
			boxY -= h / 2;
			divotX -= divotSize + distance;
			divotY -= divotRadius;
			c = Color.mixColors(borderColors, 0.5f);
			break;
		case UP:
			divotRotation = 180;
			boxX -= w / 2;
			boxY += divotSize + 1 + distance;
			divotX -= divotRadius;
			divotY += distance;
			c = borderColors.getFirst();
			break;
		}

		new BoxElement().withBackground(PonderTheme.Key.PONDER_BACKGROUND_FLAT.c())
			.gradientBorder(borderColors)
			.at(boxX, boxY, 100)
			.withBounds(w, h)
			.render(ms);

		ms.pushPose();
		ms.translate(divotX + divotRadius, divotY + divotRadius, 10);
		ms.mulPose(Vector3f.ZP.rotationDegrees(divotRotation));
		ms.translate(-divotRadius, -divotRadius, 0);
		PonderGuiTextures.SPEECH_TOOLTIP_BACKGROUND.render(ms, 0, 0);
		PonderGuiTextures.SPEECH_TOOLTIP_COLOR.render(ms, 0, 0, c);
		ms.popPose();

		if (returnWithLocalTransform) {
			ms.translate(boxX, boxY, 0);
			return;
		}

		ms.popPose();

	}

	public ItemStack getHoveredTooltipItem() {
		return hoveredTooltipItem;
	}

	public ItemStack getSubject() {
		return stack;
	}

	@Override
	public boolean isEquivalentTo(NavigatableSimiScreen other) {
		if (other instanceof PonderUI)
			return stack.sameItem(((PonderUI) other).stack);
		return super.isEquivalentTo(other);
	}

	@Override
	public void shareContextWith(NavigatableSimiScreen other) {
		if (other instanceof PonderUI ponderUI) {
			ponderUI.referredToByTag = referredToByTag;
		}
	}

	public static float getPartialTicks() {
		float renderPartialTicks = Minecraft.getInstance()
			.getFrameTime();

		if (Minecraft.getInstance().screen instanceof PonderUI ui) {
			if (ui.identifyMode)
				return ponderPartialTicksPaused;

			return (renderPartialTicks + (ui.extendedTickLength - ui.extendedTickTimer)) / (ui.extendedTickLength + 1);
		}

		return renderPartialTicks;
	}

	@Override
	public boolean isPauseScreen() {
		return true;
	}

	public void coolDownAfterSkip() {
		skipCooling = 15;
	}

	@Override
	public void removed() {
		super.removed();
		hoveredTooltipItem = ItemStack.EMPTY;
	}

	public void drawRightAlignedString(Font fontRenderer, PoseStack ms, String string, int x, int y, int color) {
		fontRenderer.draw(ms, string, (float) (x - fontRenderer.width(string)), (float) y, color);
	}

	public boolean isComfyReadingEnabled() {
		return PonderConfig.Client().comfyReading.get();
	}

	public void setComfyReadingEnabled(boolean slowTextMode) {
		PonderConfig.Client().comfyReading.set(slowTextMode);
	}

}
