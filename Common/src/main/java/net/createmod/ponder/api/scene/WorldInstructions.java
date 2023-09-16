package net.createmod.ponder.api.scene;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.UnaryOperator;

import net.createmod.ponder.foundation.ElementLink;
import net.createmod.ponder.foundation.Selection;
import net.createmod.ponder.foundation.element.EntityElement;
import net.createmod.ponder.foundation.element.WorldSectionElement;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.phys.Vec3;

public interface WorldInstructions {
	void incrementBlockBreakingProgress(BlockPos pos);

	void showSection(Selection selection, Direction fadeInDirection);

	void showSectionAndMerge(Selection selection, Direction fadeInDirection,
							 ElementLink<WorldSectionElement> link);

	void glueBlockOnto(BlockPos position, Direction fadeInDirection, ElementLink<WorldSectionElement> link);

	ElementLink<WorldSectionElement> showIndependentSection(Selection selection, Direction fadeInDirection);

	ElementLink<WorldSectionElement> showIndependentSectionImmediately(Selection selection);

	void hideSection(Selection selection, Direction fadeOutDirection);

	void hideIndependentSection(ElementLink<WorldSectionElement> link, Direction fadeOutDirection);

	void restoreBlocks(Selection selection);

	ElementLink<WorldSectionElement> makeSectionIndependent(Selection selection);

	void rotateSection(ElementLink<WorldSectionElement> link, double xRotation, double yRotation,
					   double zRotation, int duration);

	void configureCenterOfRotation(ElementLink<WorldSectionElement> link, Vec3 anchor);

	void configureStabilization(ElementLink<WorldSectionElement> link, Vec3 anchor);

	void moveSection(ElementLink<WorldSectionElement> link, Vec3 offset, int duration);

	void setBlocks(Selection selection, BlockState state, boolean spawnParticles);

	void destroyBlock(BlockPos pos);

	void setBlock(BlockPos pos, BlockState state, boolean spawnParticles);

	void replaceBlocks(Selection selection, BlockState state, boolean spawnParticles);

	void modifyBlock(BlockPos pos, UnaryOperator<BlockState> stateFunc, boolean spawnParticles);

	void cycleBlockProperty(BlockPos pos, Property<?> property);

	void modifyBlocks(Selection selection, UnaryOperator<BlockState> stateFunc, boolean spawnParticles);

	void toggleRedstonePower(Selection selection);

	<T extends Entity> void modifyEntities(Class<T> entityClass, Consumer<T> entityCallBack);

	<T extends Entity> void modifyEntitiesInside(Class<T> entityClass, Selection area,
												 Consumer<T> entityCallBack);

	void modifyEntity(ElementLink<EntityElement> link, Consumer<Entity> entityCallBack);

	ElementLink<EntityElement> createEntity(Function<Level, Entity> factory);

	ElementLink<EntityElement> createItemEntity(Vec3 location, Vec3 motion, ItemStack stack);

	void modifyBlockEntityNBT(Selection selection, Class<? extends BlockEntity> beType,
							  Consumer<CompoundTag> consumer);

	<T extends BlockEntity> void modifyBlockEntity(BlockPos position, Class<T> beType,
												   Consumer<T> consumer);

	void modifyBlockEntityNBT(Selection selection, Class<? extends BlockEntity> teType,
							  Consumer<CompoundTag> consumer, boolean reDrawBlocks);
}
