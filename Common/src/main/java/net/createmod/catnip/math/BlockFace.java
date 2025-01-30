package net.createmod.catnip.math;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import io.netty.buffer.ByteBuf;
import net.createmod.catnip.nbt.NBTHelper;
import net.createmod.catnip.data.Pair;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.codec.StreamCodec;

public class BlockFace extends Pair<BlockPos, Direction> {
	public static Codec<BlockFace> CODEC = RecordCodecBuilder.create(instance -> instance.group(
		BlockPos.CODEC.fieldOf("pos").forGetter(BlockFace::getPos),
		Direction.CODEC.fieldOf("direction").forGetter(BlockFace::getFace)
	).apply(instance, BlockFace::new));

	public static StreamCodec<ByteBuf, BlockFace> STREAM_CODEC = StreamCodec.composite(
	    BlockPos.STREAM_CODEC, BlockFace::getPos,
	    Direction.STREAM_CODEC, BlockFace::getFace,
	    BlockFace::new
	);

	public BlockFace(BlockPos first, Direction second) {
		super(first, second);
	}

	public boolean isEquivalent(BlockFace other) {
		if (equals(other))
			return true;
		return getConnectedPos().equals(other.getPos()) && getPos().equals(other.getConnectedPos());
	}

	public BlockPos getPos() {
		return getFirst();
	}

	public Direction getFace() {
		return getSecond();
	}

	public Direction getOppositeFace() {
		return getSecond().getOpposite();
	}

	public BlockFace getOpposite() {
		return new BlockFace(getConnectedPos(), getOppositeFace());
	}

	public BlockPos getConnectedPos() {
		return getPos().relative(getFace());
	}

	public CompoundTag serializeNBT() {
		CompoundTag compoundNBT = new CompoundTag();
		compoundNBT.put("Pos", NbtUtils.writeBlockPos(getPos()));
		NBTHelper.writeEnum(compoundNBT, "Face", getFace());
		return compoundNBT;
	}

	public static BlockFace fromNBT(CompoundTag compound) {
		return new BlockFace(NBTHelper.readBlockPos(compound, "Pos"),
			NBTHelper.readEnum(compound, "Face", Direction.class));
	}

}
