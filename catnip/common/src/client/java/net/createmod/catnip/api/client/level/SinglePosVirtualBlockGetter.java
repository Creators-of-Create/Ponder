package net.createmod.catnip.api.client.level;

import java.util.function.ToIntFunction;

import org.jspecify.annotations.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

// https://github.com/Engine-Room/Flywheel/blob/26.1.2/dev/common/src/lib/java/dev/engine_room/flywheel/lib/model/baked/SinglePosVirtualBlockGetter.java
public class SinglePosVirtualBlockGetter extends VirtualBlockGetter {
	protected BlockPos pos = BlockPos.ZERO;
	protected BlockState blockState = Blocks.AIR.defaultBlockState();
	@Nullable
	protected BlockEntity blockEntity;

	public SinglePosVirtualBlockGetter(ToIntFunction<BlockPos> blockLightFunc, ToIntFunction<BlockPos> skyLightFunc) {
		super(blockLightFunc, skyLightFunc);
	}

	public static SinglePosVirtualBlockGetter createFullDark() {
		return new SinglePosVirtualBlockGetter(p -> 0, p -> 0);
	}

	public static SinglePosVirtualBlockGetter createFullBright() {
		return new SinglePosVirtualBlockGetter(p -> 15, p -> 15);
	}

	public SinglePosVirtualBlockGetter pos(BlockPos pos) {
		this.pos = pos;
		return this;
	}

	public SinglePosVirtualBlockGetter blockState(BlockState state) {
		blockState = state;
		return this;
	}

	public SinglePosVirtualBlockGetter blockEntity(@Nullable BlockEntity blockEntity) {
		this.blockEntity = blockEntity;
		return this;
	}

	@Override
	@Nullable
	public BlockEntity getBlockEntity(BlockPos pos) {
		if (pos.equals(this.pos)) {
			return blockEntity;
		}

		return null;
	}

	@Override
	public BlockState getBlockState(BlockPos pos) {
		if (pos.equals(this.pos)) {
			return blockState;
		}

		return Blocks.AIR.defaultBlockState();
	}

	@Override
	public int getHeight() {
		return 1;
	}

	@Override
	public int getMinY() {
		return pos.getY();
	}
}
