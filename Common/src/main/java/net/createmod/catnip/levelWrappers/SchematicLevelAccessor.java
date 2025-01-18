package net.createmod.catnip.levelWrappers;

import java.util.List;
import java.util.Map;
import java.util.Set;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.BoundingBox;

public interface SchematicLevelAccessor extends LevelAccessor {
	Set<BlockPos> getAllPositions();

	List<Entity> getEntityList();

	Map<BlockPos, BlockState> getBlockMap();

	BoundingBox getBounds();

	void setBounds(BoundingBox bounds);

	Iterable<BlockEntity> getBlockEntities();

	Iterable<BlockEntity> getRenderedBlockEntities();
}
