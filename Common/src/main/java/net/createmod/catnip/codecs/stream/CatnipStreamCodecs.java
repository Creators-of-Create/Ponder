package net.createmod.catnip.codecs.stream;

import java.util.function.Function;

import org.jetbrains.annotations.NotNull;

import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.Vec3i;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public interface CatnipStreamCodecs {
    StreamCodec<ByteBuf, Character> CHAR = new StreamCodec<>() {
        public @NotNull Character decode(ByteBuf buffer) {
            return buffer.readChar();
        }

        public void encode(ByteBuf buffer, @NotNull Character value) {
            buffer.writeChar(value);
        }
    };

    StreamCodec<ByteBuf, Vec3> VEC3 = new StreamCodec<>() {
        @Override
        public @NotNull Vec3 decode(ByteBuf buffer) {
            return new Vec3(buffer.readDouble(), buffer.readDouble(), buffer.readDouble());
        }

        @Override
        public void encode(ByteBuf buffer, Vec3 value) {
            buffer.writeDouble(value.x);
            buffer.writeDouble(value.y);
            buffer.writeDouble(value.z);
        }
    };

    StreamCodec<ByteBuf, Vec3i> VEC3I = new StreamCodec<>() {
        @Override
        public @NotNull Vec3i decode(ByteBuf buffer) {
            return new Vec3i(buffer.readInt(), buffer.readInt(), buffer.readInt());
        }

        @Override
        public void encode(ByteBuf buffer, Vec3i value) {
            buffer.writeInt(value.getX());
            buffer.writeInt(value.getY());
            buffer.writeInt(value.getZ());
        }
    };

    StreamCodec<FriendlyByteBuf, ListTag> COMPOUND_LIST_TAG = new StreamCodec<>() {
        @Override
        public @NotNull ListTag decode(FriendlyByteBuf buffer) {
            return buffer.readCollection(size -> new ListTag(), COMPOUND_AS_TAG);
        }

        @Override
        public void encode(FriendlyByteBuf buffer, ListTag value) {
            buffer.writeCollection(value, COMPOUND_AS_TAG);
        }
    };

	StreamCodec<RegistryFriendlyByteBuf, Holder<Fluid>> HOLDER_FLUID = ByteBufCodecs.holderRegistry(Registries.FLUID);
	StreamCodec<RegistryFriendlyByteBuf, Fluid> FLUID = ByteBufCodecs.registry(Registries.FLUID);

    StreamCodec<ByteBuf, Tag> COMPOUND_AS_TAG = ByteBufCodecs.COMPOUND_TAG.map(Function.identity(), tag -> (CompoundTag) tag);
    StreamCodec<ByteBuf, BlockState> BLOCK_STATE = ByteBufCodecs.idMapper(Block.BLOCK_STATE_REGISTRY);
    StreamCodec<ByteBuf, BlockPos> NULLABLE_BLOCK_POS = CatnipStreamCodecBuilders.nullable(BlockPos.STREAM_CODEC);
    StreamCodec<ByteBuf, Direction.Axis> AXIS = CatnipStreamCodecBuilders.ofEnum(Direction.Axis.class);
    StreamCodec<ByteBuf, Rotation> ROTATION = CatnipStreamCodecBuilders.ofEnum(Rotation.class);
    StreamCodec<ByteBuf, Mirror> MIRROR = CatnipStreamCodecBuilders.ofEnum(Mirror.class);

    // optimization: 2 values, use bool instead of ofEnum
    StreamCodec<ByteBuf, InteractionHand> HAND = ByteBufCodecs.BOOL.map(
            value -> value ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND,
            hand -> hand == InteractionHand.MAIN_HAND
    );

    StreamCodec<ByteBuf, BlockHitResult> BLOCK_HIT_RESULT = StreamCodec.composite(
            ByteBufCodecs.BOOL, i -> i.getType() == HitResult.Type.MISS,
            CatnipStreamCodecs.VEC3, HitResult::getLocation,
            Direction.STREAM_CODEC, BlockHitResult::getDirection,
            BlockPos.STREAM_CODEC, BlockHitResult::getBlockPos,
            ByteBufCodecs.BOOL, BlockHitResult::isInside,
            (miss, location, direction, blockPos, isInside) ->
                miss ? BlockHitResult.miss(location, direction, blockPos) :
                        new BlockHitResult(location, direction, blockPos, isInside)
    );
}
