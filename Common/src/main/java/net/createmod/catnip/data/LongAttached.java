package net.createmod.catnip.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import io.netty.buffer.ByteBuf;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

import java.util.Comparator;
import java.util.function.Function;

public class LongAttached<V> extends Pair<Long, V> {
	protected LongAttached(Long first, V second) {
		super(first, second);
	}

	public static <V> LongAttached<V> with(long number, V value) {
		return new LongAttached<>(number, value);
	}

	public static <V> LongAttached<V> withZero(V value) {
		return new LongAttached<>(0L, value);
	}

	public boolean isZero() {
		return first == 0;
	}

	public boolean exceeds(long value) {
		return first > value;
	}

	public boolean isOrBelowZero() {
		return first <= 0;
	}

	public void increment() {
		first++;
	}

	public void decrement() {
		first--;
	}

	public V getValue() {
		return getSecond();
	}

	public CompoundTag serializeNBT(Function<V, CompoundTag> serializer) {
		CompoundTag nbt = new CompoundTag();
		nbt.put("Item", serializer.apply(getValue()));
		nbt.putLong("Location", getFirst());
		return nbt;
	}

	public static Comparator<? super LongAttached<?>> comparator() {
		return (i1, i2) -> Long.compare(i2.getFirst(), i1.getFirst());
	}

	public static <T> LongAttached<T> read(CompoundTag nbt, Function<CompoundTag, T> deserializer) {
		return LongAttached.with(nbt.getLong("Location"), deserializer.apply(nbt.getCompound("Item")));
	}

	public static <T> Codec<LongAttached<T>> codec(Codec<T> codec) {
		return RecordCodecBuilder.create(instance -> instance.group(
			Codec.LONG.fieldOf("first").forGetter(LongAttached::getFirst),
			codec.fieldOf("second").forGetter(LongAttached::getSecond)
		).apply(instance, LongAttached::new));
	}

	public static <B extends ByteBuf, T> StreamCodec<B, LongAttached<T>> streamCodec(StreamCodec<? super B, T> codec) {
		return StreamCodec.composite(
			ByteBufCodecs.VAR_LONG, Pair::getFirst,
			codec, Pair::getSecond,
			LongAttached::new
		);
	}
}
