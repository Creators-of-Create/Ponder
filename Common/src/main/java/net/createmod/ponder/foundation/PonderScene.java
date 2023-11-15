package net.createmod.ponder.foundation;

import static net.createmod.ponder.api.registration.StoryBoardEntry.SceneOrderingEntry;

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

import javax.annotation.Nullable;

import org.apache.commons.lang3.mutable.MutableDouble;
import org.apache.commons.lang3.mutable.MutableObject;
import org.joml.Matrix4f;
import org.joml.Vector4f;

import com.jozufozu.flywheel.util.DiffuseLightCalculator;
import com.jozufozu.flywheel.util.NonNullSupplier;
import com.mojang.blaze3d.vertex.PoseStack;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import net.createmod.catnip.gui.UIRenderHelper;
import net.createmod.catnip.platform.CatnipServices;
import net.createmod.catnip.render.ForcedDiffuseState;
import net.createmod.catnip.render.SuperRenderTypeBuffer;
import net.createmod.catnip.utility.Pair;
import net.createmod.catnip.utility.VecHelper;
import net.createmod.catnip.utility.animation.LerpedFloat;
import net.createmod.catnip.utility.outliner.Outliner;
import net.createmod.ponder.api.element.ElementLink;
import net.createmod.ponder.api.element.PonderElement;
import net.createmod.ponder.api.element.PonderOverlayElement;
import net.createmod.ponder.api.element.PonderSceneElement;
import net.createmod.ponder.api.element.WorldSectionElement;
import net.createmod.ponder.api.level.EmptyLevel;
import net.createmod.ponder.api.level.PonderLevel;
import net.createmod.ponder.api.scene.SceneBuilder;
import net.createmod.ponder.api.scene.SceneBuildingUtil;
import net.createmod.ponder.foundation.element.WorldSectionElementImpl;
import net.createmod.ponder.foundation.instruction.HideAllInstruction;
import net.createmod.ponder.foundation.instruction.PonderInstruction;
import net.createmod.ponder.foundation.registration.PonderLocalization;
import net.createmod.ponder.foundation.ui.PonderUI;
import net.createmod.ponder.utility.LevelTickHolder;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.core.Vec3i;
import net.minecraft.resources.ResourceLocation;
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
	ResourceLocation sceneId;

	private final IntList keyframeTimes;

	List<PonderInstruction> schedule;
	private final List<PonderInstruction> activeSchedule;
	private final Map<UUID, PonderElement> linkedElements;
	private final Set<PonderElement> elements;
	private final List<PonderTag> tags;
	private final List<SceneOrderingEntry> orderingEntries;

	private final PonderLevel world;
	private final String namespace;
	private final ResourceLocation location;
	private final SceneCamera camera;
	private final Outliner outliner;
	private SceneTransform transform;
//	private String defaultTitle;

	private final WorldSectionElement baseWorldSection;
	private final Entity renderViewEntity;
	private Vec3 pointOfInterest;
	@Nullable private Vec3 chasingPointOfInterest;

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
					   ResourceLocation location, Collection<ResourceLocation> tags,
					   Collection<SceneOrderingEntry> orderingEntries) {
		if (world != null) {
            world.scene = this;
			this.world = world;
        } else {
			this.world = new PonderLevel(BlockPos.ZERO, new EmptyLevel());
		}

		this.localization = localization;

		pointOfInterest = Vec3.ZERO;
		textIndex = 1;
		hidePlatformShadow = false;

		this.namespace = namespace;
		this.location = location;
		this.sceneId = new ResourceLocation(namespace, "missing_title");

		outliner = new Outliner();
		elements = new HashSet<>();
		linkedElements = new HashMap<>();
		this.tags = tags.stream().map(PonderIndex.getTagAccess()::getRegisteredTag).toList();
		this.orderingEntries = new ArrayList<>(orderingEntries);
		schedule = new ArrayList<>();
		activeSchedule = new ArrayList<>();
		transform = new SceneTransform();
		basePlateSize = getBounds().getXSpan();
		camera = new SceneCamera();
		baseWorldSection = new WorldSectionElementImpl();
		renderViewEntity = new ArmorStand(world, 0, 0, 0);
		keyframeTimes = new IntArrayList(4);
		scaleFactor = 1;
		yOffset = 0;

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
			if (nearestHit.getValue() != null && distanceTo >= bestDistance.getValue())
				return;

			nearestHit.setValue(Pair.of(wse, rayTrace));
			bestDistance.setValue(distanceTo);
		});

		if (nearestHit.getValue() == null)
			return Pair.of(ItemStack.EMPTY, BlockPos.ZERO);

		Pair<Vec3, BlockHitResult> selectedHit = nearestHit.getValue().getSecond();
		BlockPos selectedPos = selectedHit.getSecond().getBlockPos();

		BlockPos origin = new BlockPos(basePlateOffsetX, 0, basePlateOffsetZ);
		if (!world.getBounds()
			.isInside(selectedPos))
			return Pair.of(ItemStack.EMPTY, null);
		if (BoundingBox.fromCorners(origin, origin.offset(new Vec3i(basePlateSize - 1, 0, basePlateSize - 1)))
			.isInside(selectedPos)) {
			if (PonderIndex.editingModeActive())
				nearestHit.getValue()
					.getFirst()
					.selectBlock(selectedPos);
			return Pair.of(ItemStack.EMPTY, selectedPos);
		}

		nearestHit.getValue()
			.getFirst()
			.selectBlock(selectedPos);
		BlockState blockState = world.getBlockState(selectedPos);

		Direction direction = selectedHit.getSecond().getDirection();
		Vec3 location = selectedHit.getSecond().getLocation();

		ItemStack pickBlock = CatnipServices.HOOKS.getCloneItemFromBlockstate(
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

	public void renderScene(SuperRenderTypeBuffer buffer, GuiGraphics graphics, float pt) {
		PoseStack ms = graphics.pose();
		ForcedDiffuseState.pushCalculator(DiffuseLightCalculator.DEFAULT);
		ms.pushPose();
		Minecraft mc = Minecraft.getInstance();
		Entity prevRVE = mc.cameraEntity;

		mc.cameraEntity = this.renderViewEntity;
		forEachVisible(PonderSceneElement.class, e -> e.renderFirst(world, buffer, graphics, pt));
		mc.cameraEntity = prevRVE;

		for (RenderType type : RenderType.chunkBufferLayers())
			forEachVisible(PonderSceneElement.class, e -> e.renderLayer(world, buffer, type, graphics, pt));

		forEachVisible(PonderSceneElement.class, e -> e.renderLast(world, buffer, graphics, pt));
		camera.set(transform.xRotation.getValue(pt) + 90, transform.yRotation.getValue(pt) + 180);
		world.renderEntities(ms, buffer, camera, pt);
		world.renderParticles(ms, buffer, camera, pt);
		outliner.renderOutlines(ms, buffer, Vec3.ZERO, pt);

		ms.popPose();
		ForcedDiffuseState.popCalculator();
	}

	public void renderOverlay(PonderUI screen, GuiGraphics graphics, float partialTicks) {
		graphics.pose().pushPose();
		forEachVisible(PonderOverlayElement.class, e -> e.render(this, screen, graphics, partialTicks));
		graphics.pose().popPose();
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

		for (Iterator<PonderInstruction> iterator = activeSchedule.iterator(); iterator.hasNext();) {
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
		world.getEntityStream()
			.filter(type::isInstance)
			.map(type::cast)
			.forEach(function);
		/*
		 * for (Entity element : world.getEntities()) { if (type.isInstance(element))
		 * function.accept(type.cast(element)); }
		 */
	}

	public NonNullSupplier<String> registerText(String defaultText) {
		final String key = "text_" + textIndex;
		localization.registerSpecific(sceneId, key, defaultText);
		NonNullSupplier<String> supplier = () -> localization.getSpecific(sceneId, key);
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

	public ResourceLocation getLocation() {
		return location;
	}

	public Set<PonderElement> getElements() {
		return elements;
	}

	public BoundingBox getBounds() {
		return world.getBounds();
	}

	public ResourceLocation getId() {
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

		public void updateScreenParams(int width, int height, double offset) {
			this.width = width;
			this.height = height;
			this.offset = offset;
			cachedMat = null;
		}

		public PoseStack apply(PoseStack ms) {
			return apply(ms, LevelTickHolder.getPartialTicks(world));
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
			float f = 30 * scaleFactor;
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

			float f = 1f / (30 * scaleFactor);

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
			return new Vec2(vec4.x(), vec4.y());
		}

		protected void refreshMatrix(float pt) {
			if (cachedMat != null)
				return;
			cachedMat = apply(new PoseStack(), pt).last()
				.pose();
		}

	}

	public static class SceneCamera extends Camera {

		public void set(float xRotation, float yRotation) {
			setRotation(yRotation, xRotation);
		}

	}

}
