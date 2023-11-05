package net.createmod.ponder.foundation;

import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;

import com.mojang.math.Vector3f;

import net.createmod.catnip.utility.Pointing;
import net.createmod.catnip.utility.VecHelper;
import net.createmod.catnip.utility.theme.Color;
import net.createmod.ponder.Ponder;
import net.createmod.ponder.api.ParticleEmitter;
import net.createmod.ponder.api.PonderPalette;
import net.createmod.ponder.api.element.AnimatedSceneElement;
import net.createmod.ponder.api.element.ElementLink;
import net.createmod.ponder.api.element.EntityElement;
import net.createmod.ponder.api.element.InputElementBuilder;
import net.createmod.ponder.api.element.ParrotElement;
import net.createmod.ponder.api.element.ParrotPose;
import net.createmod.ponder.api.element.TextElementBuilder;
import net.createmod.ponder.api.element.WorldSectionElement;
import net.createmod.ponder.api.level.PonderLevel;
import net.createmod.ponder.api.scene.DebugInstructions;
import net.createmod.ponder.api.scene.EffectInstructions;
import net.createmod.ponder.api.scene.OverlayInstructions;
import net.createmod.ponder.api.scene.SceneBuilder;
import net.createmod.ponder.api.scene.Selection;
import net.createmod.ponder.api.scene.SpecialInstructions;
import net.createmod.ponder.api.scene.WorldInstructions;
import net.createmod.ponder.foundation.element.ElementLinkImpl;
import net.createmod.ponder.foundation.element.EntityElementImpl;
import net.createmod.ponder.foundation.element.InputWindowElement;
import net.createmod.ponder.foundation.element.MinecartElement;
import net.createmod.ponder.foundation.element.MinecartElement.MinecartConstructor;
import net.createmod.ponder.foundation.element.MinecartElementImpl;
import net.createmod.ponder.foundation.element.ParrotElementImpl;
import net.createmod.ponder.foundation.element.TextWindowElement;
import net.createmod.ponder.foundation.element.WorldSectionElementImpl;
import net.createmod.ponder.foundation.instruction.AnimateMinecartInstruction;
import net.createmod.ponder.foundation.instruction.AnimateParrotInstruction;
import net.createmod.ponder.foundation.instruction.AnimateWorldSectionInstruction;
import net.createmod.ponder.foundation.instruction.BlockEntityDataInstruction;
import net.createmod.ponder.foundation.instruction.ChaseAABBInstruction;
import net.createmod.ponder.foundation.instruction.CreateMinecartInstruction;
import net.createmod.ponder.foundation.instruction.CreateParrotInstruction;
import net.createmod.ponder.foundation.instruction.DelayInstruction;
import net.createmod.ponder.foundation.instruction.DisplayWorldSectionInstruction;
import net.createmod.ponder.foundation.instruction.EmitParticlesInstruction;
import net.createmod.ponder.foundation.instruction.FadeOutOfSceneInstruction;
import net.createmod.ponder.foundation.instruction.HighlightValueBoxInstruction;
import net.createmod.ponder.foundation.instruction.KeyframeInstruction;
import net.createmod.ponder.foundation.instruction.LineInstruction;
import net.createmod.ponder.foundation.instruction.MarkAsFinishedInstruction;
import net.createmod.ponder.foundation.instruction.MovePoiInstruction;
import net.createmod.ponder.foundation.instruction.OutlineSelectionInstruction;
import net.createmod.ponder.foundation.instruction.PonderInstruction;
import net.createmod.ponder.foundation.instruction.ReplaceBlocksInstruction;
import net.createmod.ponder.foundation.instruction.RotateSceneInstruction;
import net.createmod.ponder.foundation.instruction.ShowInputInstruction;
import net.createmod.ponder.foundation.instruction.TextInstruction;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.core.Vec3i;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RedstoneTorchBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/**
 * Enqueue instructions to the schedule via this object's methods.
 */
public class PonderSceneBuilder implements SceneBuilder {

	private final OverlayInstructions overlay;
	private final WorldInstructions world;
	private final DebugInstructions debug;
	private final EffectInstructions effects;
	private final SpecialInstructions special;

	protected final PonderScene scene;

	public PonderSceneBuilder(PonderScene ponderScene) {
		scene = ponderScene;
		overlay = new PonderOverlayInstructions();
		special = new PonderSpecialInstructions();
		world = new PonderWorldInstructions();
		debug = new PonderDebugInstructions();
		effects = new PonderEffectInstructions();
	}

	@Override
	public OverlayInstructions overlay() {
		return overlay;
	}

	@Override
	public WorldInstructions world() {
		return world;
	}

	@Override
	public DebugInstructions debug() {
		return debug;
	}

	@Override
	public EffectInstructions effects() {
		return effects;
	}

	@Override
	public SpecialInstructions special() {
		return special;
	}

	@Override
	public PonderScene getScene() {
		return scene;
	}

	// General

	@Override
	public void title(String sceneId, String title) {
		scene.sceneId = new ResourceLocation(scene.getNamespace(), sceneId);
		scene.localization.registerSpecific(scene.sceneId, PonderScene.TITLE_KEY, title);
	}

	@Override
	public void configureBasePlate(int xOffset, int zOffset, int basePlateSize) {
		scene.basePlateOffsetX = xOffset;
		scene.basePlateOffsetZ = zOffset;
		scene.basePlateSize = basePlateSize;
	}

	@Override
	public void scaleSceneView(float factor) {
		scene.scaleFactor = factor;
	}

	@Override
	public void removeShadow() {
		scene.hidePlatformShadow = true;
	}

	@Override
	public void setSceneOffsetY(float yOffset) {
		scene.yOffset = yOffset;
	}

	@Override
	public void showBasePlate() {
		world.showSection(scene.getSceneBuildingUtil().select().cuboid(
			new BlockPos(scene.getBasePlateOffsetX(), 0, scene.getBasePlateOffsetZ()),
			new Vec3i(scene.getBasePlateSize() - 1, 0, scene.getBasePlateSize() - 1)), Direction.UP);
	}

	@Override
	public void addInstruction(PonderInstruction instruction) {
		scene.schedule.add(instruction);
	}

	@Override
	public void addInstruction(Consumer<PonderScene> callback) {
		addInstruction(PonderInstruction.simple(callback));
	}

	@Override
	public void idle(int ticks) {
		addInstruction(new DelayInstruction(ticks));
	}

	@Override
	public void idleSeconds(int seconds) {
		idle(seconds * 20);
	}

	@Override
	public void markAsFinished() {
		addInstruction(new MarkAsFinishedInstruction());
	}

	@Override
	public void setNextUpEnabled(boolean isEnabled) {
		addInstruction(scene -> scene.setNextUpEnabled(isEnabled));
	}

	@Override
	public void rotateCameraY(float degrees) {
		addInstruction(new RotateSceneInstruction(0, degrees, true));
	}

	@Override
	public void addKeyframe() {
		addInstruction(KeyframeInstruction.IMMEDIATE);
	}

	@Override
	public void addLazyKeyframe() {
		addInstruction(KeyframeInstruction.DELAYED);
	}

	public class PonderEffectInstructions implements EffectInstructions {

		@Override
		public void emitParticles(Vec3 location, ParticleEmitter emitter, float amountPerCycle, int cycles) {
			addInstruction(new EmitParticlesInstruction(location, emitter, amountPerCycle, cycles));
		}

		@Override
		public <T extends ParticleOptions> ParticleEmitter simpleParticleEmitter(T data, Vec3 motion) {
			return (w, x, y, z) -> w.addParticle(data, x, y, z, motion.x, motion.y, motion.z);
		}

		@Override
		public <T extends ParticleOptions> ParticleEmitter particleEmitterWithinBlockSpace(T data, Vec3 motion) {
			return (w, x, y, z) -> w.addParticle(data, Math.floor(x) + Ponder.RANDOM.nextFloat(),
					Math.floor(y) + Ponder.RANDOM.nextFloat(), Math.floor(z) + Ponder.RANDOM.nextFloat(), motion.x,
					motion.y, motion.z);
		}

		@Override
		public void indicateRedstone(BlockPos pos) {
			createRedstoneParticles(pos, 0xFF0000, 10);
		}

		@Override
		public void indicateSuccess(BlockPos pos) {
			createRedstoneParticles(pos, 0x80FFaa, 10);
		}

		@Override
		public void createRedstoneParticles(BlockPos pos, int color, int amount) {
			Vector3f rgb = new Color(color).asVectorF();
			addInstruction(new EmitParticlesInstruction(VecHelper.getCenterOf(pos),
					effects().particleEmitterWithinBlockSpace(new DustParticleOptions(rgb, 1), Vec3.ZERO), amount, 2));
		}

	}

	public class PonderOverlayInstructions implements OverlayInstructions {

		@Override
		public TextElementBuilder showText(int duration) {
			TextWindowElement textWindowElement = new TextWindowElement();
			addInstruction(new TextInstruction(textWindowElement, duration));
			return textWindowElement.builder(scene);
		}

		@Override
		public TextElementBuilder showOutlineWithText(Selection selection, int duration) {
			TextWindowElement textWindowElement = new TextWindowElement();
			addInstruction(new TextInstruction(textWindowElement, duration, selection));
			return textWindowElement.builder(scene).pointAt(selection.getCenter());
		}

		@Override
		public InputElementBuilder showControls(Vec3 sceneSpace, Pointing direction, int duration) {
			InputWindowElement inputWindowElement = new InputWindowElement(sceneSpace, direction);
			addInstruction(new ShowInputInstruction(inputWindowElement, duration));
			return inputWindowElement.builder();
		}

		@Override
		public void chaseBoundingBoxOutline(PonderPalette color, Object slot, AABB boundingBox, int duration) {
			addInstruction(new ChaseAABBInstruction(color, slot, boundingBox, duration));
		}

		@Override
		public void showCenteredScrollInput(BlockPos pos, Direction side, int duration) {
			showScrollInput(scene.getSceneBuildingUtil().vector().blockSurface(pos, side), side, duration);
		}

		@Override
		public void showScrollInput(Vec3 location, Direction side, int duration) {
			Axis axis = side.getAxis();
			float s = 1 / 16f;
			float q = 1 / 4f;
			Vec3 expands = new Vec3(axis == Axis.X ? s : q, axis == Axis.Y ? s : q, axis == Axis.Z ? s : q);
			addInstruction(new HighlightValueBoxInstruction(location, expands, duration));
		}

		@Override
		public void showRepeaterScrollInput(BlockPos pos, int duration) {
			float s = 1 / 16f;
			float q = 1 / 6f;
			Vec3 expands = new Vec3(q, s, q);
			addInstruction(
				new HighlightValueBoxInstruction(scene.getSceneBuildingUtil().vector().blockSurface(pos, Direction.DOWN)
					.add(0, 3 / 16f, 0), expands, duration));
		}

		@Override
		public void showFilterSlotInput(Vec3 location, int duration) {
			float s = .1f;
			Vec3 expands = new Vec3(s, s, s);
			addInstruction(new HighlightValueBoxInstruction(location, expands, duration));
		}

		@Override
		public void showFilterSlotInput(Vec3 location, Direction side, int duration) {
			location = location.add(Vec3.atLowerCornerOf(side.getNormal()).scale(-3 / 128f));
			Vec3 expands = VecHelper.axisAlingedPlaneOf(side).scale(11 / 128f);
			addInstruction(new HighlightValueBoxInstruction(location, expands, duration));
		}

		@Override
		public void showLine(PonderPalette color, Vec3 start, Vec3 end, int duration) {
			addInstruction(new LineInstruction(color, start, end, duration, false));
		}

		@Override
		public void showBigLine(PonderPalette color, Vec3 start, Vec3 end, int duration) {
			addInstruction(new LineInstruction(color, start, end, duration, true));
		}

		@Override
		public void showOutline(PonderPalette color, Object slot, Selection selection, int duration) {
			addInstruction(new OutlineSelectionInstruction(color, slot, selection, duration));
		}

	}

	public class PonderSpecialInstructions implements SpecialInstructions {

		@Override
		public ElementLink<ParrotElement> createBirb(Vec3 location, Supplier<? extends ParrotPose> pose) {
			ElementLink<ParrotElement> link = new ElementLinkImpl<>(ParrotElement.class);
			ParrotElement parrot = ParrotElementImpl.create(location, pose);
			addInstruction(new CreateParrotInstruction(10, Direction.DOWN, parrot));
			addInstruction(scene -> scene.linkElement(parrot, link));
			return link;
		}

		@Override
		public void changeBirbPose(ElementLink<ParrotElement> birb, Supplier<? extends ParrotPose> pose) {
			addInstruction(scene -> scene.resolveOptional(birb)
					.ifPresent(safeBirb -> safeBirb.setPose(pose.get())));
		}

		@Override
		public void movePointOfInterest(Vec3 location) {
			addInstruction(new MovePoiInstruction(location));
		}

		@Override
		public void movePointOfInterest(BlockPos location) {
			movePointOfInterest(VecHelper.getCenterOf(location));
		}

		@Override
		public void rotateParrot(ElementLink<ParrotElement> link, double xRotation, double yRotation, double zRotation,
								 int duration) {
			addInstruction(AnimateParrotInstruction.rotate(link, new Vec3(xRotation, yRotation, zRotation), duration));
		}

		@Override
		public void moveParrot(ElementLink<ParrotElement> link, Vec3 offset, int duration) {
			addInstruction(AnimateParrotInstruction.move(link, offset, duration));
		}

		@Override
		public ElementLink<MinecartElement> createCart(Vec3 location, float angle, MinecartConstructor type) {
			ElementLink<MinecartElement> link = new ElementLinkImpl<>(MinecartElement.class);
			MinecartElement cart = new MinecartElementImpl(location, angle, type);
			addInstruction(new CreateMinecartInstruction(10, Direction.DOWN, cart));
			addInstruction(scene -> scene.linkElement(cart, link));
			return link;
		}

		@Override
		public void rotateCart(ElementLink<MinecartElement> link, float yRotation, int duration) {
			addInstruction(AnimateMinecartInstruction.rotate(link, yRotation, duration));
		}

		@Override
		public void moveCart(ElementLink<MinecartElement> link, Vec3 offset, int duration) {
			addInstruction(AnimateMinecartInstruction.move(link, offset, duration));
		}

		@Override
		public <T extends AnimatedSceneElement> void hideElement(ElementLink<T> link, Direction direction) {
			addInstruction(new FadeOutOfSceneInstruction<>(15, direction, link));
		}

	}

	public class PonderWorldInstructions implements WorldInstructions {

		@Override
		public void incrementBlockBreakingProgress(BlockPos pos) {
			addInstruction(scene -> {
				PonderLevel world = scene.getWorld();
				int progress = world.getBlockBreakingProgressions()
					.getOrDefault(pos, -1) + 1;
				if (progress == 9) {
					world.addBlockDestroyEffects(pos, world.getBlockState(pos));
					world.destroyBlock(pos, false);
					world.setBlockBreakingProgress(pos, 0);
					scene.forEach(WorldSectionElement.class, WorldSectionElement::queueRedraw);
				} else
					world.setBlockBreakingProgress(pos, progress + 1);
			});
		}

		@Override
		public void showSection(Selection selection, Direction fadeInDirection) {
			addInstruction(new DisplayWorldSectionInstruction(15, fadeInDirection, selection, scene::getBaseWorldSection));
		}

		@Override
		public void showSectionAndMerge(Selection selection, Direction fadeInDirection,
										ElementLink<WorldSectionElement> link) {
			addInstruction(new DisplayWorldSectionInstruction(15, fadeInDirection, selection, () -> scene.resolve(link)));
		}

		@Override
		public void glueBlockOnto(BlockPos position, Direction fadeInDirection, ElementLink<WorldSectionElement> link) {
			addInstruction(new DisplayWorldSectionInstruction(15, fadeInDirection,
															  scene.getSceneBuildingUtil().select().position(position), () -> scene.resolve(link), position));
		}

		@Override
		public ElementLink<WorldSectionElement> showIndependentSection(Selection selection, Direction fadeInDirection) {
			DisplayWorldSectionInstruction instruction =
				new DisplayWorldSectionInstruction(15, fadeInDirection, selection, null);
			addInstruction(instruction);
			return instruction.createLink(scene);
		}

		@Override
		public ElementLink<WorldSectionElement> showIndependentSectionImmediately(Selection selection) {
			DisplayWorldSectionInstruction instruction =
				new DisplayWorldSectionInstruction(0, Direction.DOWN, selection, null);
			addInstruction(instruction);
			return instruction.createLink(scene);
		}

		@Override
		public void hideSection(Selection selection, Direction fadeOutDirection) {
			WorldSectionElement worldSectionElement = new WorldSectionElementImpl(selection);
			ElementLink<WorldSectionElement> elementLink = new ElementLinkImpl<>(WorldSectionElement.class);

			addInstruction(scene -> {
				scene.getBaseWorldSection()
					.erase(selection);
				scene.linkElement(worldSectionElement, elementLink);
				scene.addElement(worldSectionElement);
				worldSectionElement.queueRedraw();
			});

			hideIndependentSection(elementLink, fadeOutDirection);
		}

		@Override
		public void hideIndependentSection(ElementLink<WorldSectionElement> link, Direction fadeOutDirection) {
			addInstruction(new FadeOutOfSceneInstruction<>(15, fadeOutDirection, link));
		}

		@Override
		public void restoreBlocks(Selection selection) {
			addInstruction(scene -> scene.getWorld()
				.restoreBlocks(selection));
		}

		@Override
		public ElementLink<WorldSectionElement> makeSectionIndependent(Selection selection) {
			WorldSectionElementImpl worldSectionElement = new WorldSectionElementImpl(selection);
			ElementLink<WorldSectionElement> elementLink = new ElementLinkImpl<>(WorldSectionElement.class);

			addInstruction(scene -> {
				scene.getBaseWorldSection()
					.erase(selection);
				scene.linkElement(worldSectionElement, elementLink);
				scene.addElement(worldSectionElement);
				worldSectionElement.queueRedraw();
				worldSectionElement.resetAnimatedTransform();
				worldSectionElement.setVisible(true);
				worldSectionElement.forceApplyFade(1);
			});

			return elementLink;
		}

		@Override
		public void rotateSection(ElementLink<WorldSectionElement> link, double xRotation, double yRotation,
								  double zRotation, int duration) {
			addInstruction(
				AnimateWorldSectionInstruction.rotate(link, new Vec3(xRotation, yRotation, zRotation), duration));
		}

		@Override
		public void configureCenterOfRotation(ElementLink<WorldSectionElement> link, Vec3 anchor) {
			addInstruction(scene -> scene.resolveOptional(link)
					.ifPresent(safe -> safe.setCenterOfRotation(anchor)));
		}

		@Override
		public void configureStabilization(ElementLink<WorldSectionElement> link, Vec3 anchor) {
			addInstruction(scene -> scene.resolveOptional(link)
					.ifPresent(safe -> safe.stabilizeRotation(anchor)));
		}

		@Override
		public void moveSection(ElementLink<WorldSectionElement> link, Vec3 offset, int duration) {
			addInstruction(AnimateWorldSectionInstruction.move(link, offset, duration));
		}

		@Override
		public void setBlocks(Selection selection, BlockState state, boolean spawnParticles) {
			addInstruction(new ReplaceBlocksInstruction(selection, $ -> state, true, spawnParticles));
		}

		@Override
		public void destroyBlock(BlockPos pos) {
			setBlock(pos, Blocks.AIR.defaultBlockState(), true);
		}

		@Override
		public void setBlock(BlockPos pos, BlockState state, boolean spawnParticles) {
			setBlocks(scene.getSceneBuildingUtil().select().position(pos), state, spawnParticles);
		}

		@Override
		public void replaceBlocks(Selection selection, BlockState state, boolean spawnParticles) {
			modifyBlocks(selection, $ -> state, spawnParticles);
		}

		@Override
		public void modifyBlock(BlockPos pos, UnaryOperator<BlockState> stateFunc, boolean spawnParticles) {
			modifyBlocks(scene.getSceneBuildingUtil().select().position(pos), stateFunc, spawnParticles);
		}

		@Override
		public void cycleBlockProperty(BlockPos pos, Property<?> property) {
			modifyBlocks(scene.getSceneBuildingUtil().select().position(pos),
				s -> s.hasProperty(property) ? s.cycle(property) : s, false);
		}

		@Override
		public void modifyBlocks(Selection selection, UnaryOperator<BlockState> stateFunc, boolean spawnParticles) {
			addInstruction(new ReplaceBlocksInstruction(selection, stateFunc, false, spawnParticles));
		}

		@Override
		public void toggleRedstonePower(Selection selection) {
			modifyBlocks(selection, s -> {
				if (s.hasProperty(BlockStateProperties.POWER))
					s = s.setValue(BlockStateProperties.POWER, s.getValue(BlockStateProperties.POWER) == 0 ? 15 : 0);
				if (s.hasProperty(BlockStateProperties.POWERED))
					s = s.cycle(BlockStateProperties.POWERED);
				if (s.hasProperty(RedstoneTorchBlock.LIT))
					s = s.cycle(RedstoneTorchBlock.LIT);
				return s;
			}, false);
		}

		@Override
		public <T extends Entity> void modifyEntities(Class<T> entityClass, Consumer<T> entityCallBack) {
			addInstruction(scene -> scene.forEachWorldEntity(entityClass, entityCallBack));
		}

		@Override
		public <T extends Entity> void modifyEntitiesInside(Class<T> entityClass, Selection area,
															Consumer<T> entityCallBack) {
			addInstruction(scene -> scene.forEachWorldEntity(entityClass, e -> {
				if (area.test(e.blockPosition()))
					entityCallBack.accept(e);
			}));
		}

		@Override
		public void modifyEntity(ElementLink<EntityElement> link, Consumer<Entity> entityCallBack) {
			addInstruction(scene -> {
				EntityElement resolve = scene.resolve(link);
				if (resolve != null)
					resolve.ifPresent(entityCallBack);
			});
		}

		@Override
		public ElementLink<EntityElement> createEntity(Function<Level, Entity> factory) {
			ElementLink<EntityElement> link = new ElementLinkImpl<>(EntityElement.class, UUID.randomUUID());
			addInstruction(scene -> {
				PonderLevel world = scene.getWorld();
				Entity entity = factory.apply(world);
				EntityElement handle = new EntityElementImpl(entity);
				scene.addElement(handle);
				scene.linkElement(handle, link);
				world.addFreshEntity(entity);
			});
			return link;
		}

		@Override
		public ElementLink<EntityElement> createItemEntity(Vec3 location, Vec3 motion, ItemStack stack) {
			return createEntity(world -> {
				ItemEntity itemEntity = new ItemEntity(world, location.x, location.y, location.z, stack);
				itemEntity.setDeltaMovement(motion);
				return itemEntity;
			});
		}

		@Override
		public void modifyBlockEntityNBT(Selection selection, Class<? extends BlockEntity> beType,
										 Consumer<CompoundTag> consumer) {
			modifyBlockEntityNBT(selection, beType, consumer, false);
		}

		@Override
		public <T extends BlockEntity> void modifyBlockEntity(BlockPos position, Class<T> beType,
															  Consumer<T> consumer) {
			addInstruction(scene -> {
				BlockEntity blockEntity = scene.getWorld()
						.getBlockEntity(position);
				if (beType.isInstance(blockEntity))
					consumer.accept(beType.cast(blockEntity));
			});
		}

		@Override
		public void modifyBlockEntityNBT(Selection selection, Class<? extends BlockEntity> teType,
										 Consumer<CompoundTag> consumer, boolean reDrawBlocks) {
			addInstruction(new BlockEntityDataInstruction(selection, teType, nbt -> {
				consumer.accept(nbt);
				return nbt;
			}, reDrawBlocks));
		}
	}

	public class PonderDebugInstructions implements DebugInstructions {

		@Override
		public void debugSchematic() {
			addInstruction(
				scene -> scene.addElement(new WorldSectionElementImpl(scene.getSceneBuildingUtil().select().everywhere())));
		}

		@Override
		public void addInstructionInstance(PonderInstruction instruction) {
			addInstruction(instruction);
		}

		@Override
		public void enqueueCallback(Consumer<PonderScene> callback) {
			addInstruction(callback);
		}

	}

}
