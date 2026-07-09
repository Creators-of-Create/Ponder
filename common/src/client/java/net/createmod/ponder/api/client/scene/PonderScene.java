package net.createmod.ponder.api.client.scene;

import static net.createmod.ponder.api.client.registration.StoryBoardEntry.SceneOrderingEntry;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import org.apache.commons.lang3.mutable.MutableDouble;
import org.apache.commons.lang3.mutable.MutableObject;
import org.joml.Matrix4f;
import org.joml.Vector4f;
import org.jspecify.annotations.Nullable;

import com.mojang.blaze3d.vertex.PoseStack;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import net.createmod.catnip.api.animation.LerpedFloat;
import net.createmod.catnip.api.client.animation.AnimationTickHolder;
import net.createmod.catnip.api.client.gui.UIRenderHelper;
import net.createmod.catnip.api.client.outliner.Outliner;
import net.createmod.catnip.api.client.render.SuperRenderTypeBuffer;
import net.createmod.catnip.api.data.Pair;
import net.createmod.catnip.api.math.VecHelper;
import net.createmod.catnip.api.platform.services.ModHooksHelper;
import net.createmod.ponder.api.client.PonderIndex;
import net.createmod.ponder.api.client.element.ElementLink;
import net.createmod.ponder.api.client.element.PonderElement;
import net.createmod.ponder.api.client.element.PonderOverlayElement;
import net.createmod.ponder.api.client.element.PonderSceneElement;
import net.createmod.ponder.api.client.element.WorldSectionElement;
import net.createmod.ponder.api.client.instruction.PonderInstruction;
import net.createmod.ponder.api.client.level.PonderLevel;
import net.createmod.ponder.api.client.registration.PonderTag;
import net.createmod.ponder.impl.client.element.WorldSectionElementImpl;
import net.createmod.ponder.impl.client.gui.PonderUI;
import net.createmod.ponder.impl.client.instruction.HideAllInstruction;
import net.createmod.ponder.impl.client.registration.PonderLocalization;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.chunk.ChunkSectionLayer;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.core.Vec3i;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;

public class PonderScene {
	public static final String TITLE_KEY = "header";

	final PonderLocalization localization;

	private boolean finished;
	//	private int sceneIndex;
	private int textIndex;
	Identifier sceneId;

	private final IntList keyframeTimes;

	List<PonderInstruction> schedule;
	private final List<PonderInstruction> activeSchedule;
	private final Map<UUID, PonderElement> linkedElements;
	private final Set<PonderElement> elements;
	private final List<PonderTag> tags;
	private final List<SceneOrderingEntry> orderingEntries;

	private final PonderLevel world;
	private final String namespace;
	private final Identifier identifier;
	private final SceneCamera camera;
	private final CameraRenderState cameraRenderState;
	private final Outliner outliner;
	private SceneTransform transform;
//	private String defaultTitle;

	private final WorldSectionElement baseWorldSection;
	@Nullable
	private final Entity renderViewEntity;
	private Vec3 pointOfInterest;
	@Nullable
	private Vec3 chasingPointOfInterest;

	int basePlateOffsetX;
	int basePlateOffsetZ;
	int basePlateSize;
	float scaleFactor;
	float yOffset;
	boolean hidePlatformShadow;

	private boolean stoppedCounting;
	private int totalTime;
	private int currentTime;
	private boolean nextUpEnabled = true;

	public PonderScene(@Nullable PonderLevel world, PonderLocalization localization, String namespace,
					   Identifier identifier, Collection<Identifier> tags,
					   Collection<SceneOrderingEntry> orderingEntries) {
		if (world != null) {
			world.scene = this;
		}
		this.world = world;

		this.localization = localization;

		pointOfInterest = Vec3.ZERO;
		textIndex = 1;
		hidePlatformShadow = false;

		this.namespace = namespace;
		this.identifier = identifier;
		this.sceneId = Identifier.fromNamespaceAndPath(namespace, "missing_title");

		outliner = new Outliner();
		elements = new HashSet<>();
		linkedElements = new HashMap<>();
		this.tags = tags.stream().map(PonderIndex.getTagAccess()::getRegisteredTag).flatMap(Optional::stream).toList();
		this.orderingEntries = new ArrayList<>(orderingEntries);
		schedule = new ArrayList<>();
		activeSchedule = new ArrayList<>();
		transform = new SceneTransform();
		basePlateSize = getBounds().getXSpan();
		camera = new SceneCamera();
		cameraRenderState = new CameraRenderState();
		baseWorldSection = new WorldSectionElementImpl();
		keyframeTimes = new IntArrayList(4);
		scaleFactor = 1;
		yOffset = 0;

		if (world != null) {
			renderViewEntity = new ArmorStand(world, 0, 0, 0);
		} else {
			renderViewEntity = null;
		}

		setPointOfInterest(new Vec3(0, 4, 0));
	}

	public void deselect() {
		forEach(WorldSectionElement.class, WorldSectionElement::resetSelectedBlock);
	}

	public Pair<ItemStack, BlockPos> rayTraceScene(Vec3 from, Vec3 to) {
		MutableObject<Pair<WorldSectionElement, Pair<Vec3, BlockHitResult>>> nearestHit = new MutableObject<>();
		MutableDouble bestDistance = new MutableDouble(0);

		forEach(WorldSectionElement.class, wse -> {
			wse.resetSelectedBlock();
			if (!wse.isVisible())
				return;
			Pair<Vec3, BlockHitResult> rayTrace = wse.rayTrace(world, from, to);
			if (rayTrace == null)
				return;
			double distanceTo = rayTrace.getFirst()
				.distanceTo(from);
			if (nearestHit.get() != null && distanceTo >= bestDistance.doubleValue())
				return;

			nearestHit.setValue(Pair.of(wse, rayTrace));
			bestDistance.setValue(distanceTo);
		});

		if (nearestHit.get() == null)
			return Pair.of(ItemStack.EMPTY, BlockPos.ZERO);

		Pair<Vec3, BlockHitResult> selectedHit = nearestHit.get().getSecond();
		BlockPos selectedPos = selectedHit.getSecond().getBlockPos();

		BlockPos origin = new BlockPos(basePlateOffsetX, 0, basePlateOffsetZ);
		if (!world.getBounds()
			.isInside(selectedPos))
			return Pair.of(ItemStack.EMPTY, null);
		if (BoundingBox.fromCorners(origin, origin.offset(new Vec3i(basePlateSize - 1, 0, basePlateSize - 1)))
			.isInside(selectedPos)) {
			if (PonderIndex.editingModeActive())
				nearestHit.get()
					.getFirst()
					.selectBlock(selectedPos);
			return Pair.of(ItemStack.EMPTY, selectedPos);
		}

		nearestHit.get()
			.getFirst()
			.selectBlock(selectedPos);
		BlockState blockState = world.getBlockState(selectedPos);

		Direction direction = selectedHit.getSecond().getDirection();
		Vec3 location = selectedHit.getSecond().getLocation();

		ItemStack pickBlock = ModHooksHelper.INSTANCE.getCloneItemFromBlockstate(
			blockState,
			new BlockHitResult(location, direction, selectedPos, true),
			world,
			selectedPos,
			Minecraft.getInstance().player
		);

		return Pair.of(pickBlock, selectedPos);
	}

	public void reset() {
		currentTime = 0;
		activeSchedule.clear();
		schedule.forEach(mdi -> mdi.reset(this));
	}

	public void begin() {
		reset();
		forEach(pe -> pe.reset(this));

		world.restore();
		elements.clear();
		linkedElements.clear();
		keyframeTimes.clear();

		transform = new SceneTransform();
		finished = false;
		setPointOfInterest(new Vec3(0, 4, 0));

		baseWorldSection.setEmpty();
		baseWorldSection.forceApplyFade(1);
		elements.add(baseWorldSection);

		totalTime = 0;
		stoppedCounting = false;
		activeSchedule.addAll(schedule);
		activeSchedule.forEach(i -> i.onScheduled(this));
	}

	public WorldSectionElement getBaseWorldSection() {
		return baseWorldSection;
	}

	public float getSceneProgress() {
		return totalTime == 0 ? 0 : currentTime / (float) totalTime;
	}

	public void fadeOut() {
		reset();
		activeSchedule.add(new HideAllInstruction(10, null));
	}

	public void renderScene(SuperRenderTypeBuffer buffer, SubmitNodeCollector queue, PoseStack poseStack, float pt) {
		Minecraft mc = Minecraft.getInstance();

		poseStack.pushPose();
		Entity prevRVE = mc.getCameraEntity();

		// Setup CameraRenderState
		camera.set(transform.xRotation.getValue(pt) + 90, transform.yRotation.getValue(pt) + 180);
		cameraRenderState.initialized = camera.isInitialized();
		cameraRenderState.pos = camera.position();
		cameraRenderState.blockPos = camera.blockPosition();
		cameraRenderState.orientation.set(camera.rotation());

		mc.setCameraEntity(this.renderViewEntity);
		forEachVisible(PonderSceneElement.class, e -> e.renderFirst(world, buffer, queue, camera, cameraRenderState, poseStack, pt));
		mc.setCameraEntity(prevRVE);

		for (ChunkSectionLayer layer : ChunkSectionLayer.values())
			forEachVisible(PonderSceneElement.class, e -> e.renderLayer(world, buffer, layer, queue, camera, cameraRenderState, poseStack, pt));

		forEachVisible(PonderSceneElement.class, e -> e.renderLast(world, buffer, queue, camera, cameraRenderState, poseStack, pt));
		world.renderEntities(poseStack, queue, camera, cameraRenderState, pt);
		world.renderParticles(poseStack, queue, camera, cameraRenderState, pt);
		outliner.renderOutlines(poseStack, buffer, Vec3.ZERO, pt);

		poseStack.popPose();
	}

	public void renderOverlay(PonderUI screen, GuiGraphicsExtractor graphics, float partialTicks) {
		graphics.pose().pushMatrix();
		forEachVisible(PonderOverlayElement.class, e -> e.render(this, screen, graphics, partialTicks));
		graphics.pose().popMatrix();
	}

	public void setPointOfInterest(Vec3 poi) {
		if (chasingPointOfInterest == null)
			pointOfInterest = poi;
		chasingPointOfInterest = poi;
	}

	public Vec3 getPointOfInterest() {
		return pointOfInterest;
	}

	public void tick() {
		if (chasingPointOfInterest != null)
			pointOfInterest = VecHelper.lerp(.25f, pointOfInterest, chasingPointOfInterest);

		outliner.tickOutlines();
		world.tick();
		transform.tick();
		forEach(e -> e.tick(this));

		if (currentTime < totalTime)
			currentTime++;

		for (Iterator<PonderInstruction> iterator = activeSchedule.iterator(); iterator.hasNext(); ) {
			PonderInstruction instruction = iterator.next();
			instruction.tick(this);
			if (instruction.isComplete()) {
				iterator.remove();
				if (instruction.isBlocking())
					break;
				continue;
			}
			if (instruction.isBlocking())
				break;
		}

		if (activeSchedule.isEmpty())
			finished = true;
	}

	public void seekToTime(int time) {
		if (time < currentTime)
			throw new IllegalStateException("Cannot seek backwards. Rewind first.");

		while (currentTime < time && !finished) {
			forEach(e -> e.whileSkipping(this));
			tick();
		}

		forEach(WorldSectionElement.class, WorldSectionElement::queueRedraw);
	}

	public void addToSceneTime(int time) {
		if (!stoppedCounting)
			totalTime += time;
	}

	public void stopCounting() {
		stoppedCounting = true;
	}

	public void markKeyframe(int offset) {
		if (!stoppedCounting)
			keyframeTimes.add(totalTime + offset);
	}

	public void addElement(PonderElement e) {
		elements.add(e);
	}

	public <E extends PonderElement> void linkElement(E e, ElementLink<E> link) {
		linkedElements.put(link.getId(), e);
	}

	@Nullable
	public <E extends PonderElement> E resolve(ElementLink<E> link) {
		return link.cast(linkedElements.get(link.getId()));
	}

	public <E extends PonderElement> Optional<E> resolveOptional(ElementLink<E> link) {
		return Optional.ofNullable(resolve(link));
	}

	public <E extends PonderElement> void runWith(ElementLink<E> link, Consumer<E> callback) {
		callback.accept(resolve(link));
	}

	public <E extends PonderElement, F> F applyTo(ElementLink<E> link, Function<E, F> function) {
		return function.apply(resolve(link));
	}

	public void forEach(Consumer<? super PonderElement> function) {
		for (PonderElement elemtent : elements)
			function.accept(elemtent);
	}

	public <T extends PonderElement> void forEach(Class<T> type, Consumer<T> function) {
		for (PonderElement element : elements)
			if (type.isInstance(element))
				function.accept(type.cast(element));
	}

	public <T extends PonderElement> void forEachVisible(Class<T> type, Consumer<T> function) {
		for (PonderElement element : elements)
			if (type.isInstance(element) && element.isVisible())
				function.accept(type.cast(element));
	}

	public <T extends Entity> void forEachWorldEntity(Class<T> type, Consumer<T> function) {
		for (Entity entity : world.getEntityList()) {
			if (type.isInstance(entity)) {
				function.accept(type.cast(entity));
			}
		}
	}

	public Supplier<String> registerText(String defaultText) {
		final String key = "text_" + textIndex;
		localization.registerSpecific(sceneId, key, defaultText);
		Supplier<String> supplier = () -> localization.getSpecific(sceneId, key);
		textIndex++;
		return supplier;
	}

	public Supplier<String> registerText(String defaultText, Object... params) {
		final String key = "text_" + textIndex;
		localization.registerSpecific(sceneId, key, defaultText);
		Supplier<String> supplier = () -> localization.getSpecific(sceneId, key, params);
		textIndex++;
		return supplier;
	}

	public SceneBuilder builder() {
		return new PonderSceneBuilder(this);
	}

	public SceneBuildingUtil getSceneBuildingUtil() {
		return new PonderSceneBuildingUtil(getBounds());
	}

	public String getTitle() {
		return getString(TITLE_KEY);
	}

	public String getString(String key) {
		return localization.getSpecific(sceneId, key);
	}

	public PonderLevel getWorld() {
		return world;
	}

	public String getNamespace() {
		return namespace;
	}

	public int getKeyframeCount() {
		return keyframeTimes.size();
	}

	public int getKeyframeTime(int index) {
		return keyframeTimes.getInt(index);
	}

	public List<PonderTag> getTags() {
		return tags;
	}

	public List<SceneOrderingEntry> getOrderingEntries() {
		return orderingEntries;
	}

	public Identifier getIdentifier() {
		return identifier;
	}

	public Set<PonderElement> getElements() {
		return elements;
	}

	public BoundingBox getBounds() {
		return world == null ? new BoundingBox(BlockPos.ZERO) : world.getBounds();
	}

	public Identifier getSceneId() {
		return sceneId;
	}

	public SceneTransform getTransform() {
		return transform;
	}

	public Outliner getOutliner() {
		return outliner;
	}

	public boolean isFinished() {
		return finished;
	}

	public void setFinished(boolean finished) {
		this.finished = finished;
	}

	public int getBasePlateOffsetX() {
		return basePlateOffsetX;
	}

	public int getBasePlateOffsetZ() {
		return basePlateOffsetZ;
	}

	public boolean shouldHidePlatformShadow() {
		return hidePlatformShadow;
	}

	public int getBasePlateSize() {
		return basePlateSize;
	}

	public float getScaleFactor() {
		return scaleFactor;
	}

	public float getYOffset() {
		return yOffset;
	}

	public int getTotalTime() {
		return totalTime;
	}

	public int getCurrentTime() {
		return currentTime;
	}

	public void setNextUpEnabled(boolean nextUpEnabled) {
		this.nextUpEnabled = nextUpEnabled;
	}

	public boolean isNextUpEnabled() {
		return nextUpEnabled;
	}

	public class SceneTransform {
		public LerpedFloat xRotation, yRotation;

		// Screen params
		private int width, height;
		private double offset;
		private Matrix4f cachedMat;

		private int guiScale = 1;

		public SceneTransform() {
			xRotation = LerpedFloat.angular()
				.disableSmartAngleChasing()
				.startWithValue(-35);
			yRotation = LerpedFloat.angular()
				.disableSmartAngleChasing()
				.startWithValue(55 + 90);
		}

		public void tick() {
			xRotation.tickChaser();
			yRotation.tickChaser();
		}

		public void updateScreenParams(int width, int height, double offset, int guiScale) {
			this.width = width * guiScale;
			this.height = height * guiScale;
			this.offset = offset;
			this.guiScale = guiScale;
			cachedMat = null;
		}

		public PoseStack apply(PoseStack ms) {
			return apply(ms, AnimationTickHolder.getPartialTicks());
		}

		public PoseStack apply(PoseStack ms, float pt) {
			ms.translate(width / 2, height / 2, 200 + offset);

			ms.mulPose(com.mojang.math.Axis.XP.rotationDegrees(-35));
			ms.mulPose(com.mojang.math.Axis.YP.rotationDegrees(55));
			ms.translate(offset, 0, 0);
			ms.mulPose(com.mojang.math.Axis.YP.rotationDegrees(-55));
			ms.mulPose(com.mojang.math.Axis.XP.rotationDegrees(35));
			ms.mulPose(com.mojang.math.Axis.XP.rotationDegrees(xRotation.getValue(pt)));
			ms.mulPose(com.mojang.math.Axis.YP.rotationDegrees(yRotation.getValue(pt)));

			UIRenderHelper.flipForGuiRender(ms);
			float f = 30 * scaleFactor * guiScale;
			ms.scale(f, f, f);
			ms.translate(
				basePlateSize / -2f - basePlateOffsetX,
				-1f + yOffset,
				basePlateSize / -2f - basePlateOffsetZ
			);

			return ms;
		}

		public void updateSceneRVE(float pt) {
			Vec3 v = screenToScene(width / 2, height / 2, 500, pt);
			if (renderViewEntity != null)
				renderViewEntity.setPos(v.x, v.y, v.z);
		}

		public Vec3 screenToScene(double x, double y, int depth, float pt) {
			refreshMatrix(pt);
			Vec3 vec = new Vec3(x, y, depth);

			vec = vec.subtract(width / 2, height / 2, 200 + offset);
			vec = VecHelper.rotate(vec, 35, Axis.X);
			vec = VecHelper.rotate(vec, -55, Axis.Y);
			vec = vec.subtract(offset, 0, 0);
			vec = VecHelper.rotate(vec, 55, Axis.Y);
			vec = VecHelper.rotate(vec, -35, Axis.X);
			vec = VecHelper.rotate(vec, -xRotation.getValue(pt), Axis.X);
			vec = VecHelper.rotate(vec, -yRotation.getValue(pt), Axis.Y);

			float f = 1f / (30 * scaleFactor * guiScale);

			vec = vec.multiply(f, -f, f);
			vec = vec.subtract(
				basePlateSize / -2f - basePlateOffsetX,
				-1f + yOffset,
				basePlateSize / -2f - basePlateOffsetZ
			);

			return vec;
		}

		public Vec2 sceneToScreen(Vec3 vec, float pt) {
			refreshMatrix(pt);
			Vector4f vec4 = new Vector4f((float) vec.x, (float) vec.y, (float) vec.z, 1);
			vec4.mul(cachedMat);
			vec4.div(guiScale);
			return new Vec2(vec4.x(), vec4.y());
		}

		protected void refreshMatrix(float pt) {
			if (cachedMat != null)
				return;
			cachedMat = apply(new PoseStack(), pt).last()
				.pose();
		}

		public int guiScale() {
			return guiScale;
		}

		public int width() {
			return width;
		}

		public int height() {
			return height;
		}
	}

	public static class SceneCamera extends Camera {
		public void set(float xRotation, float yRotation) {
			setRotation(yRotation, xRotation);
		}
	}
}
