package net.createmod.ponder.impl.client.gui;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.joml.Matrix3x2f;
import org.joml.Matrix3x2fStack;
import org.joml.Vector3f;
import org.jspecify.annotations.Nullable;

import com.google.common.graph.ElementOrder;
import com.google.common.graph.GraphBuilder;
import com.google.common.graph.MutableGraph;
import com.mojang.blaze3d.platform.ClipboardManager;
import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.math.Constants;

import net.createmod.catnip.api.animation.LerpedFloat;
import net.createmod.catnip.api.animation.LerpedFloat.Chaser;
import net.createmod.catnip.api.client.animation.AnimationTickHolder;
import net.createmod.catnip.api.client.gui.NavigatableSimiScreen;
import net.createmod.catnip.api.client.gui.ScreenOpener;
import net.createmod.catnip.api.client.gui.UIRenderHelper;
import net.createmod.catnip.api.client.gui.element.BoxElement;
import net.createmod.catnip.api.client.gui.element.GuiGameElement;
import net.createmod.catnip.api.client.gui.texture.CatnipGuiTextures;
import net.createmod.catnip.api.client.gui.widget.BoxWidget;
import net.createmod.catnip.api.client.lang.ClientFontHelper;
import net.createmod.catnip.api.data.Couple;
import net.createmod.catnip.api.data.Pair;
import net.createmod.catnip.api.math.Pointing;
import net.createmod.catnip.api.registry.RegisteredObjectsHelper;
import net.createmod.catnip.api.theme.Color;
import net.createmod.ponder.api.Ponder;
import net.createmod.ponder.api.client.PonderIndex;
import net.createmod.ponder.api.client.PonderStoryBoardEntry;
import net.createmod.ponder.api.client.registration.PonderTag;
import net.createmod.ponder.api.client.registration.PonderTag.Highlight;
import net.createmod.ponder.api.client.registration.StoryBoardEntry;
import net.createmod.ponder.api.client.registration.StoryBoardEntry.SceneOrderingEntry;
import net.createmod.ponder.api.client.registration.StoryBoardEntry.SceneOrderingType;
import net.createmod.ponder.api.client.scene.PonderScene;
import net.createmod.ponder.api.client.scene.PonderScene.SceneTransform;
import net.createmod.ponder.impl.client.element.TextWindowElement;
import net.createmod.ponder.impl.client.gui.element.PonderButton;
import net.createmod.ponder.impl.client.gui.element.PonderProgressBar;
import net.createmod.ponder.impl.client.plugin.DebugScenes;
import net.createmod.ponder.impl.config.PonderConfig;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

public class PonderUI extends AbstractPonderScreen {
	public static int ponderTicks;
	public static float ponderPartialTicksPaused;

	public static final Color BACKGROUND_TRANSPARENT = new Color(0xdd_000000, true);
	public static final Color BACKGROUND_FLAT = new Color(0xff_000000, true);
	public static final Color BACKGROUND_IMPORTANT = new Color(0xdd_0e0e20, true);

	public static final Couple<Color> COLOR_IDLE = Couple.create(
		new Color(0x40_ffeedd, true),
		new Color(0x20_ffeedd, true)
	).map(Color::setImmutable);
	public static final Couple<Color> COLOR_HOVER = Couple.create(
		new Color(0x70_ffffff, true),
		new Color(0x30_ffffff, true)
	).map(Color::setImmutable);
	public static final Couple<Color> COLOR_HIGHLIGHT = Couple.create(
		new Color(0xf0_ffeedd, true),
		new Color(0x60_ffeedd, true)
	).map(Color::setImmutable);
	public static final Couple<Color> MISSING_VANILLA_ENTRY = Couple.create(
		new Color(0x50_5000ff, true),
		new Color(0x50_28007f, true)
	).map(Color::setImmutable);
	public static final Couple<Color> MISSING_MODDED_ENTRY = Couple.create(
		new Color(0x70_984500, true),
		new Color(0x70_692400, true)
	).map(Color::setImmutable);

	private static final Vector3f DIFFUSE_LIGHT_0 = new Vector3f(0.4F, -1.0F, 0.7F).normalize();
	private static final Vector3f DIFFUSE_LIGHT_1 = new Vector3f(-0.4F, -0.5F, 0.7F).normalize();

	private final List<PonderScene> scenes;
	private final List<PonderTag> tags;
	private List<PonderButton> tagButtons = new ArrayList<>();
	private List<LerpedFloat> tagFades = new ArrayList<>();
	private final LerpedFloat fadeIn;
	ItemStack stack;

	private boolean userViewMode;
	private boolean identifyMode;
	private ItemStack hoveredTooltipItem = ItemStack.EMPTY;
	@Nullable
	private BlockPos hoveredBlockPos;

	private final ClipboardManager clipboardHelper;
	@Nullable
	private BlockPos copiedBlockPos;

	private final LerpedFloat finishingFlash;
	private final LerpedFloat nextUp;
	private int finishingFlashWarmup = 0;
	private int nextUpWarmup = 0;

	private final LerpedFloat lazyIndex;
	private int index = 0;
	@Nullable
	private PonderTag referredToByTag;

	private PonderButton left, right, scan, userMode, close, replay, slowMode;
	private int skipCooling = 0;

	private int extendedTickLength = 0;
	private int extendedTickTimer = 0;

	public static PonderUI of(Identifier id) {
		return new PonderUI(PonderIndex.getSceneAccess().compile(id));
	}

	public static PonderUI of(Item item) {
		return new PonderUI(PonderIndex.getSceneAccess().compile(RegisteredObjectsHelper.getKeyOrThrow(item)));
	}

	public static PonderUI of(ItemStack item) {
		return of(item.getItem());
	}

	public static PonderUI of(ItemStack item, PonderTag tag) {
		PonderUI ponderUI = of(item);
		ponderUI.referredToByTag = tag;
		return ponderUI;
	}

	protected PonderUI(List<PonderScene> scenes) {
		// FIXME: title lang
		super(Component.literal("Ponder"));

		Identifier location = scenes.get(0).getIdentifier();
		stack = new ItemStack(RegisteredObjectsHelper.getItemOrBlock(location));

		tags = new ArrayList<>(PonderIndex.getTagAccess().getTags(location));

		Ponder.LOGGER.debug("Ponder Scenes before ordering: {}", Arrays.toString(scenes.stream().map(PonderScene::getSceneId).toArray()));

		List<PonderScene> orderedScenes;
		try {
			orderedScenes = orderScenes(scenes);
			Ponder.LOGGER.debug("Ponder Scenes after ordering: {}", Arrays.toString(orderedScenes.stream().map(PonderScene::getSceneId).toArray()));
		} catch (Exception e) {
			Ponder.LOGGER.warn("Unable to sort PonderScenes, using unordered List", e);
			orderedScenes = scenes;
		}
		this.scenes = orderedScenes;

		if (this.scenes.isEmpty()) {
			List<StoryBoardEntry> list = Collections.singletonList(
				new PonderStoryBoardEntry(DebugScenes::empty, Ponder.MOD_ID, "debug/scene_1",
					Identifier.withDefaultNamespace("stick")));
			this.scenes.addAll(PonderIndex.getSceneAccess().compile(list));
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

	@SuppressWarnings("UnstableApiUsage")
	private List<PonderScene> orderScenes(List<PonderScene> scenes) {
		Map<Boolean, List<PonderScene>> partitioned = scenes.stream()
			.collect(Collectors.partitioningBy(scene -> scene.getOrderingEntries().isEmpty()));

		List<PonderScene> scenesWithOrdering = partitioned.get(false);
		List<PonderScene> scenesWithoutOrdering = partitioned.get(true);

		if (scenesWithOrdering.isEmpty())
			return scenes;

		List<PonderScene> sceneList = new ArrayList<>(scenes);
		Collections.reverse(sceneList);

		Map<Identifier, PonderScene> sceneLookup = scenes.stream()
			.collect(Collectors.toMap(PonderScene::getSceneId, scene -> scene));

		MutableGraph<PonderScene> graph = GraphBuilder.directed().nodeOrder(ElementOrder.insertion()).build();
		sceneList.forEach(graph::addNode);

		IntStream.range(1, scenesWithoutOrdering.size())
			.forEach(i -> graph.putEdge(scenesWithoutOrdering.get(i - 1), scenesWithoutOrdering.get(i)));

		scenesWithOrdering.forEach(scene -> {
			List<SceneOrderingEntry> relevantOrderings = scene.getOrderingEntries()
				.stream()
				.filter(entry -> scenes.stream().anyMatch(sc -> sc.getSceneId().equals(entry.sceneId())))
				.toList();

			if (relevantOrderings.isEmpty())
				return;

			relevantOrderings.forEach(entry -> {
				PonderScene otherScene = sceneLookup.get(entry.sceneId());
				if (entry.type() == SceneOrderingType.BEFORE) {
					graph.putEdge(scene, otherScene);
				} else if (entry.type() == SceneOrderingType.AFTER) {
					graph.putEdge(otherScene, scene);
				}
			});
		});

		return topologicalSort(graph);

		/*sceneList.sort((scene1, scene2) -> {
			boolean hasOrderings1 = !scene1.getOrderingEntries().isEmpty();
			boolean hasOrderings2 = !scene2.getOrderingEntries().isEmpty();

			if (!hasOrderings1 && !hasOrderings2)
				return 0;

			Map<SceneOrderingType, Long> relevantOrderings1 = scene1.getOrderingEntries()
					.stream()
					.filter(entry -> entry.sceneId().equals(scene2.getId()))
					.collect(Collectors.groupingBy(SceneOrderingEntry::type, Collectors.counting()));

			Map<SceneOrderingType, Long> relevantOrderings2 = scene2.getOrderingEntries()
					.stream()
					.filter(entry -> entry.sceneId().equals(scene1.getId()))
					.collect(Collectors.groupingBy(SceneOrderingEntry::type, Collectors.counting()));

			// both scenes don't want to be ordered compared to each other
			if (relevantOrderings1.isEmpty() && relevantOrderings2.isEmpty())
				return 0;

			// only scene2 wants to be ordered either before or after scene1
			if (relevantOrderings1.isEmpty())
				return relevantOrderings2.containsKey(SceneOrderingType.AFTER) ? -1 : 1;

			// only scene1 wants to be ordered either before or after scene2
			if (relevantOrderings2.isEmpty())
				return relevantOrderings1.containsKey(SceneOrderingType.AFTER) ? 1 : -1;

			// both scenes want scene1 to be ordered after scene2
			if (relevantOrderings1.containsKey(SceneOrderingType.AFTER) && relevantOrderings2.containsKey(SceneOrderingType.BEFORE))
				return 1;

			// both scenes want scene1 to be ordered before scene2
			if (relevantOrderings1.containsKey(SceneOrderingType.BEFORE) && relevantOrderings2.containsKey(SceneOrderingType.AFTER))
				return -1;

			// everything else is contradictory so we ignore it
			return 0;
		});

		return sceneList;*/
	}

	private static List<PonderScene> topologicalSort(MutableGraph<PonderScene> graph) {
		List<PonderScene> result = new ArrayList<>();
		Set<PonderScene> visited = new HashSet<>();
		Set<PonderScene> currentlyVisiting = new HashSet<>();

		for (PonderScene node : graph.nodes()) {
			if (!visited.contains(node)) {
				if (!dfs(node, graph, visited, currentlyVisiting, result)) {
					throw new IllegalArgumentException("Graph has a cycle!");
				}
			}
		}

		Collections.reverse(result);
		return result;
	}

	private static boolean dfs(
		PonderScene node,
		MutableGraph<PonderScene> graph,
		Set<PonderScene> visited,
		Set<PonderScene> currentlyVisiting,
		List<PonderScene> result
	) {
		if (currentlyVisiting.contains(node)) {
			return false; // Detected a cycle
		}

		if (!visited.contains(node)) {
			currentlyVisiting.add(node);
			for (PonderScene neighbor : graph.successors(node)) {
				if (!dfs(neighbor, graph, visited, currentlyVisiting, result)) {
					return false; // Detected a cycle
				}
			}
			currentlyVisiting.remove(node);
			visited.add(node);
			result.add(node);
		}

		return true;
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
			.showing(CatnipGuiTextures.ICON_PONDER_IDENTIFY)
			.enableFade(0, 5)
			.withCallback(() -> {
				identifyMode = !identifyMode;
				if (!identifyMode)
					scenes.get(index)
						.deselect();
				else
					ponderPartialTicksPaused = AnimationTickHolder.getGuiPartialTicks();
			}));
		scan.atZLevel(600);

		addRenderableWidget(
			slowMode = new PonderButton(width - 20 - 31, bY).showing(CatnipGuiTextures.ICON_PONDER_SLOW_MODE)
				.enableFade(0, 5)
				.withCallback(() -> setComfyReadingEnabled(!isComfyReadingEnabled())));

		if (PonderIndex.editingModeActive()) {
			addRenderableWidget(
				userMode = new PonderButton(width - 50 - 31, bY).showing(CatnipGuiTextures.ICON_PONDER_USER_MODE)
					.enableFade(0, 5)
					.withCallback(() -> userViewMode = !userViewMode));
		}

		bX += 50 + spacing;
		addRenderableWidget(left = new PonderButton(bX, bY).withShortcut(bindings.keyLeft)
			.showing(CatnipGuiTextures.ICON_PONDER_LEFT)
			.enableFade(0, 5)
			.withCallback(() -> this.scroll(false)));

		bX += 20 + spacing;
		addRenderableWidget(close = new PonderButton(bX, bY).withShortcut(bindings.keyInventory)
			.showing(CatnipGuiTextures.ICON_PONDER_CLOSE)
			.enableFade(0, 5)
			.withCallback(this::onClose));

		bX += 20 + spacing;
		addRenderableWidget(right = new PonderButton(bX, bY).withShortcut(bindings.keyRight)
			.showing(CatnipGuiTextures.ICON_PONDER_RIGHT)
			.enableFade(0, 5)
			.withCallback(() -> this.scroll(true)));

		bX += 50 + spacing;
		addRenderableWidget(replay = new PonderButton(bX, bY).withShortcut(bindings.keyDown)
			.showing(CatnipGuiTextures.ICON_PONDER_REPLAY)
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
	public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
		if (scroll(scrollY > 0))
			return true;
		return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
	}

	protected void replay() {
		identifyMode = false;
		PonderScene scene = scenes.get(index);

		if (minecraft.hasShiftDown()) {
			PonderIndex.reload();
			this.scenes.clear();
			this.scenes.addAll(PonderIndex.getSceneAccess().compile(scene.getIdentifier()));


			/*PonderScene finalScene = scene;
			List<PonderStoryBoardEntry> list = PonderIndex.getSceneAccess().getRegisteredEntries().stream().filter(
					entry -> entry.getKey() == finalScene.getLocation()).map(Map.Entry::getValue).toList();
			PonderStoryBoardEntry sb = list.get(index);
			StructureTemplate activeTemplate = PonderSceneRegistry.loadSchematic(sb.getschematicIdentifier());
			PonderLevel world = new PonderLevel(BlockPos.ZERO, Minecraft.getInstance().level);
			activeTemplate.placeInWorld(world, BlockPos.ZERO, BlockPos.ZERO, new StructurePlaceSettings(),
										RandomSource.create(), Block.UPDATE_CLIENTS);
			world.createBackup();
			scene = PonderSceneRegistry.compileScene(scene.localization, sb, world);
			scene.begin();
			this.scenes.set(index, scene);*/
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
	public void extractScaledRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTicks) {
		super.extractScaledRenderState(graphics, mouseX, mouseY, partialTicks);

		float widgetTicks = this.identifyMode ? ponderPartialTicksPaused : partialTicks;
		float sceneTicks = this.skipCooling > 0 ? 0 : widgetTicks;

		this.renderVisibleScenes(graphics, mouseX, mouseY, sceneTicks);
		this.renderWidgets(graphics, mouseX, mouseY, widgetTicks);
	}

	protected void renderVisibleScenes(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTicks) {
		renderScene(graphics, index, partialTicks);
		float lazyIndexValue = lazyIndex.getValue(partialTicks);
		if (Math.abs(lazyIndexValue - index) > 1 / 512f)
			renderScene(graphics, lazyIndexValue < index ? index - 1 : index + 1, partialTicks);
	}

	protected void renderScene(GuiGraphicsExtractor graphics, int i, float partialTicks) {
		PonderScene scene = scenes.get(i);
		double value = lazyIndex.getValue(AnimationTickHolder.getGuiPartialTicks());
		double diff = i - value;
		double slide = Mth.lerp(diff * diff, 200, 600) * diff;

		RenderSystem.backupProjectionMatrix();

		graphics.guiRenderState.addPicturesInPictureState(new PonderSceneRenderState(
			new Matrix3x2f(graphics.pose()),
			scene,
			width,
			height,
			slide,
			finishingFlash,
			partialTicks,
			Minecraft.getInstance().gameRenderer.gameRenderState().windowRenderState
		));

		RenderSystem.restoreProjectionMatrix();
	}

	protected void renderWidgets(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTicks) {
		float fade = fadeIn.getValue(partialTicks);
		float lazyIndexValue = lazyIndex.getValue(partialTicks);
		float indexDiff = lazyIndexValue - index;
		PonderScene activeScene = scenes.get(index);
		PonderScene nextScene = scenes.size() > index + 1 ? scenes.get(index + 1) : null;

		boolean noWidgetsHovered = true;
		for (GuiEventListener child : children())
			noWidgetsHovered &= !child.isMouseOver(mouseX, mouseY);

		int tooltipColor = UIRenderHelper.COLOR_TEXT_DARKER.getFirst().getRGB();
		renderSceneInformation(graphics, fade, indexDiff, activeScene, tooltipColor);

		Matrix3x2fStack ms = graphics.pose();

		if (identifyMode) {
			if (noWidgetsHovered && mouseY < height - 80) {
				ms.pushMatrix();
				ms.translate(mouseX, mouseY);
				if (hoveredTooltipItem.isEmpty()) {

					MutableComponent text = Ponder.lang()
						.translate(AbstractPonderScreen.IDENTIFY_MODE,
							((MutableComponent) minecraft.options.keyDrop.getTranslatedKeyMessage())
								.withStyle(ChatFormatting.WHITE))
						.style(ChatFormatting.GRAY)
						.component();

					graphics.setComponentTooltipForNextFrame(
						font,
						font.getSplitter()
							.splitLines(text, width / 3, Style.EMPTY)
							.stream()
							.map(t -> (Component) Component.literal(t.getString()))
							.toList(),
						0,
						0
					);
				} else
					graphics.setTooltipForNextFrame(font, hoveredTooltipItem, 0, 0);
				if (hoveredBlockPos != null && PonderIndex.editingModeActive() && !userViewMode) {
					ms.translate(0, -15);
					boolean copied = hoveredBlockPos.equals(copiedBlockPos);
					MutableComponent coords = Component.literal(hoveredBlockPos.getX() + ", " + hoveredBlockPos.getY() + ", " + hoveredBlockPos.getZ())
						.withStyle(copied ? ChatFormatting.GREEN : ChatFormatting.GOLD);
					graphics.setTooltipForNextFrame(font, coords, 0, 0);
				}
				ms.popMatrix();
			}
			scan.flash();
		} else {
			scan.dim();
		}

		if (PonderIndex.editingModeActive()) {
			if (userViewMode)
				userMode.flash();
			else
				userMode.dim();
		}

		if (isComfyReadingEnabled())
			slowMode.flash();
		else
			slowMode.dim();

		renderSceneOverlay(graphics, partialTicks, lazyIndexValue, Math.abs(indexDiff));

		renderNextUp(graphics, partialTicks, nextScene);

		// Widgets
		getRenderables().forEach(w -> {
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

		// Arrows behind the main widgets
		Color c1 = COLOR_NAV_ARROW.getFirst().setAlpha(0x40);
		Color c2 = COLOR_NAV_ARROW.getFirst().setAlpha(0x20);
		Color c3 = COLOR_NAV_ARROW.getFirst().setAlpha(0x10);
		UIRenderHelper.breadcrumbArrow(graphics, width / 2 - 20, height - 51, 20, 20, 5, c1, c2);
		UIRenderHelper.breadcrumbArrow(graphics, width / 2 + 20, height - 51, -20, 20, -5, c1, c2);
		UIRenderHelper.breadcrumbArrow(graphics, width / 2 - 90, height - 51, 70, 20, 5, c1, c3);
		UIRenderHelper.breadcrumbArrow(graphics, width / 2 + 90, height - 51, -70, 20, -5, c1, c3);

		// Tags
		List<PonderTag> sceneTags = activeScene.getTags();
		boolean highlightAll = sceneTags.stream().anyMatch(tag -> tag.getId() == Highlight.ALL);
		double s = Minecraft.getInstance()
			.getWindow()
			.getGuiScale();
		IntStream.range(0, tagButtons.size())
			.forEach(i -> {
				ms.pushMatrix();
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

				int x = button.getX() + button.getWidth() + 4;
				int y = button.getY() - 2;
				ms.translate(x, y + 5 * (1 - fade));

				float fadedWidth = 200 * chase.getValue(partialTicks);
				UIRenderHelper.streak(graphics, 0, 0, 12, 26, (int) fadedWidth);

				RenderSystem.enableScissorForRenderTypeDraws((int) (x * s), 0, (int) (fadedWidth * s), (int) (height * s));

				String tagName = tag
					.getTitle();
				graphics.text(font, tagName, 3, 8, UIRenderHelper.COLOR_TEXT_ACCENT.getFirst().getRGB(), false);

				RenderSystem.disableScissorForRenderTypeDraws();

				ms.popMatrix();
			});

		renderHoverTooltips(graphics, tooltipColor);
	}

	private void renderHoverTooltips(GuiGraphicsExtractor graphics, int tooltipColor) {
		int tooltipY = height - 16;
		if (scan.isHoveredOrFocused())
			graphics.centeredText(font, Ponder.lang().translate(AbstractPonderScreen.IDENTIFY).component(), scan.getX() + 10, tooltipY, tooltipColor);
		if (index != 0 && left.isHoveredOrFocused())
			graphics.centeredText(font, Ponder.lang().translate(AbstractPonderScreen.PREVIOUS).component(), left.getX() + 10, tooltipY, tooltipColor);
		if (close.isHoveredOrFocused())
			graphics.centeredText(font, Ponder.lang().translate(AbstractPonderScreen.CLOSE).component(), close.getX() + 10, tooltipY, tooltipColor);
		if (index != scenes.size() - 1 && right.isHoveredOrFocused())
			graphics.centeredText(font, Ponder.lang().translate(AbstractPonderScreen.NEXT).component(), right.getX() + 10, tooltipY, tooltipColor);
		if (replay.isHoveredOrFocused())
			graphics.centeredText(font, Ponder.lang().translate(AbstractPonderScreen.REPLAY).component(), replay.getX() + 10, tooltipY, tooltipColor);
		if (slowMode.isHoveredOrFocused())
			graphics.centeredText(font, Ponder.lang().translate(AbstractPonderScreen.SLOW_TEXT).component(), slowMode.getX() + 5, tooltipY, tooltipColor);
		if (PonderIndex.editingModeActive() && userMode.isHoveredOrFocused())
			graphics.centeredText(font, "Editor View", userMode.getX() + 10, tooltipY, tooltipColor);
	}

	private void renderNextUp(GuiGraphicsExtractor graphics, float partialTicks, @Nullable PonderScene nextScene) {
		if (!getActiveScene().isFinished())
			return;

		if (nextScene == null || !nextScene.isNextUpEnabled())
			return;

		if (!(nextUp.getValue() > 1 / 16f))
			return;

		Matrix3x2fStack poseStack = graphics.pose();
		poseStack.pushMatrix();
		poseStack.translate(right.getX() + 10, right.getY() - 6 + nextUp.getValue(partialTicks) * 5);
		MutableComponent nextUpComponent = Ponder.lang().translate(AbstractPonderScreen.NEXT_UP).component();
		int boxWidth = (Math.max(font.width(nextScene.getTitle()), font.width(nextUpComponent)) + 5);
		renderSpeechBox(graphics, 0, 0, boxWidth, 20, right.isHoveredOrFocused(), Pointing.DOWN, false);
		poseStack.translate(0, -29);
		graphics.centeredText(font, nextUpComponent, 0, 0, UIRenderHelper.COLOR_TEXT_DARKER.getFirst().getRGB());
		graphics.centeredText(font, nextScene.getTitle(), 0, 10, UIRenderHelper.COLOR_TEXT.getFirst().getRGB());
		poseStack.popMatrix();
	}

	private void renderSceneOverlay(GuiGraphicsExtractor graphics, float partialTicks, float lazyIndexValue, float indexDiff) {
		// Scene overlay
		float scenePT = skipCooling > 0 ? 0 : partialTicks;
		Matrix3x2fStack poseStack = graphics.pose();
		renderOverlay(graphics, index, scenePT);
		if (indexDiff > 1 / 512f)
			renderOverlay(graphics, lazyIndexValue < index ? index - 1 : index + 1, scenePT);
	}

	private void renderSceneInformation(GuiGraphicsExtractor graphics, float fade, float indexDiff, PonderScene activeScene, int tooltipColor) {
		float absoluteIndexDiff = Math.abs(indexDiff);
		// info includes icon, scene title and the "Pondering about... " text

		int otherIndex = index;
		if (scenes.size() != 1 && absoluteIndexDiff >= 0.01) {
			float indexOffset = Math.signum(indexDiff);
			otherIndex = index + (int) indexOffset;
			if (otherIndex < 0 || otherIndex >= scenes.size()) {
				return; // should never be reached
			}
		}

		FormattedText title = FormattedText.of(activeScene.getTitle());
		FormattedText otherTitle = FormattedText.of(scenes.get(otherIndex).getTitle());

		int maxTitleWidth = 180;

		int titleWidth = font.width(title);
		if (titleWidth > maxTitleWidth)
			titleWidth = maxTitleWidth;

		int otherTitleWidth = font.width(otherTitle);
		if (otherTitleWidth > maxTitleWidth)
			otherTitleWidth = maxTitleWidth;

		int wrappedTitleHeight = font.wordWrapHeight(title, maxTitleWidth);
		int otherWrappedTitleHeight = font.wordWrapHeight(otherTitle, maxTitleWidth);

		// height is ideal for single line titles
		int streakHeight = 35 - 9 + (int) Mth.lerp(absoluteIndexDiff, wrappedTitleHeight, otherWrappedTitleHeight);
		int streakWidth = 70 + (int) Mth.lerp(absoluteIndexDiff, titleWidth, otherTitleWidth);

		Matrix3x2fStack poseStack = graphics.pose();
		poseStack.pushMatrix();
		// translate to top left of the background streak
		poseStack.translate(55, 19);

		// background streak
		UIRenderHelper.streak(graphics, 0, 0, streakHeight / 2, streakHeight, (int) (streakWidth * fade));
		UIRenderHelper.streak(graphics, 180, 0, streakHeight / 2, streakHeight, (int) (30 * fade));

		// icon
		new BoxElement().withBackground(PonderUI.BACKGROUND_FLAT)
			.gradientBorder(COLOR_IDLE)
			.at(-34, 2, 100)
			.withBounds(30, 30)
			.submit(graphics);

		GuiGameElement.of(stack)
			.scale(2)
			.at(-35, 1)
			.submit(graphics);

		// pondering about text
		poseStack.translate(4, 6);
		graphics.text(font, Ponder.lang().translate(AbstractPonderScreen.PONDERING).component(), 0, 0, tooltipColor, false);

		// scene title
		poseStack.translate(0, 14);

		// short version for single scene views
		if (scenes.size() == 1 || absoluteIndexDiff < 0.01) {
			ClientFontHelper.drawSplitString(graphics, font, title, 0, 0, maxTitleWidth, UIRenderHelper.COLOR_TEXT
				.getFirst()
				.scaleAlphaForText(fade)
				.getRGB());

			poseStack.popMatrix();
			return;
		}


		poseStack.translate(0, 6);
		poseStack.pushMatrix();
		poseStack.rotate((indexDiff * -90 + Math.signum(indexDiff) * 90) * Constants.DEG_TO_RAD);
		poseStack.translate(0, -6);
		ClientFontHelper.drawSplitString(graphics, font, otherTitle, 0, 0, maxTitleWidth, UIRenderHelper.COLOR_TEXT
			.getFirst()
			.scaleAlphaForText(absoluteIndexDiff)
			.getRGB()
		);
		poseStack.popMatrix();

		poseStack.rotate((indexDiff * -90) * Constants.DEG_TO_RAD);
		poseStack.translate(0, -6);
		ClientFontHelper.drawSplitString(graphics, font, title, 0, 0, maxTitleWidth, UIRenderHelper.COLOR_TEXT
			.getFirst()
			.scaleAlphaForText(1 - absoluteIndexDiff)
			.getRGB()
		);
		poseStack.popMatrix();
	}

	private void renderOverlay(GuiGraphicsExtractor graphics, int i, float partialTicks) {
		if (identifyMode)
			return;
		graphics.pose().pushMatrix();
		PonderScene story = scenes.get(i);
		story.renderOverlay(this, graphics, skipCooling > 0 ? 0 : identifyMode ? ponderPartialTicksPaused : partialTicks);
		graphics.pose().popMatrix();
	}

	@Override
	public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
		if (identifyMode && hoveredBlockPos != null && PonderIndex.editingModeActive()) {
			Window window = minecraft.getWindow();
			if (copiedBlockPos != null && event.button() == InputConstants.MOUSE_BUTTON_RIGHT) {
				clipboardHelper.setClipboard(window,
					"util.select().fromTo(" + copiedBlockPos.getX() + ", " + copiedBlockPos.getY() + ", "
						+ copiedBlockPos.getZ() + ", " + hoveredBlockPos.getX() + ", " + hoveredBlockPos.getY() + ", "
						+ hoveredBlockPos.getZ() + ")");
				copiedBlockPos = hoveredBlockPos;
				return true;
			}

			if (minecraft.hasShiftDown())
				clipboardHelper.setClipboard(window, "util.select().position(" + hoveredBlockPos.getX() + ", "
					+ hoveredBlockPos.getY() + ", " + hoveredBlockPos.getZ() + ")");
			else
				clipboardHelper.setClipboard(window, "util.grid().at(" + hoveredBlockPos.getX() + ", "
					+ hoveredBlockPos.getY() + ", " + hoveredBlockPos.getZ() + ")");
			copiedBlockPos = hoveredBlockPos;
			return true;
		}

		return super.mouseClicked(event, doubleClick);
	}

	public Font getFontRenderer() {
		return font;
	}

	protected boolean isMouseOver(double mouseX, double mouseY, int x, int y, int w, int h) {
		boolean hovered = !(mouseX < x || mouseX > x + w);
		hovered &= !(mouseY < y || mouseY > y + h);
		return hovered;
	}

	public static void renderSpeechBox(GuiGraphicsExtractor graphics, int x, int y, int w, int h, boolean highlighted, Pointing pointing,
									   boolean returnWithLocalTransform) {
		Matrix3x2fStack poseStack = graphics.pose();
		if (!returnWithLocalTransform) {
			poseStack.pushMatrix();
		}

		int boxX = x;
		int boxY = y;
		int divotX = x;
		int divotY = y;
		int divotRotation = 0;
		int divotSize = 8;
		int distance = 1;
		int divotRadius = divotSize / 2;
		Couple<Color> borderColors = highlighted ? PonderButton.COLOR_HOVER : COLOR_IDLE;
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

		new BoxElement().withBackground(PonderUI.BACKGROUND_FLAT)
			.gradientBorder(borderColors)
			.at(boxX, boxY, 100)
			.withBounds(w, h)
			.submit(graphics);

		poseStack.pushMatrix();
		poseStack.translate(divotX + divotRadius, divotY + divotRadius);
		poseStack.rotate(divotRotation * Constants.DEG_TO_RAD);
		poseStack.translate(-divotRadius, -divotRadius);
		CatnipGuiTextures.SPEECH_TOOLTIP_BACKGROUND.render(graphics, 0, 0);
		CatnipGuiTextures.SPEECH_TOOLTIP_COLOR.render(graphics, 0, 0, c);
		poseStack.popMatrix();

		if (returnWithLocalTransform) {
			poseStack.translate(boxX, boxY);
			return;
		}

		poseStack.popMatrix();
	}

	public ItemStack getHoveredTooltipItem() {
		return hoveredTooltipItem;
	}

	public ItemStack getSubject() {
		return stack;
	}

	@Override
	public boolean isEquivalentTo(NavigatableSimiScreen other) {
		if (other instanceof PonderUI otherUI)
			return !otherUI.stack.isEmpty() && stack.is(otherUI.stack.getItem());
		return super.isEquivalentTo(other);
	}

	@Override
	public void shareContextWith(NavigatableSimiScreen other) {
		if (other instanceof PonderUI ponderUI) {
			ponderUI.referredToByTag = referredToByTag;
		}
	}

	public static float getPartialTicks() {
		float renderPartialTicks = AnimationTickHolder.getGuiPartialTicks();

		if (Minecraft.getInstance().gui.screen() instanceof PonderUI ui) {
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

	public boolean isComfyReadingEnabled() {
		return PonderConfig.client().comfyReading.get();
	}

	public void setComfyReadingEnabled(boolean slowTextMode) {
		PonderConfig.client().comfyReading.set(slowTextMode);
	}
}
