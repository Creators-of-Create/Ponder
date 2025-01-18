package net.createmod.catnip.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.network.codec.StreamCodec;

import java.util.Objects;

public class Pair<F, S> {

	F first;
	S second;

	protected Pair(F first, S second) {
		this.first = first;
		this.second = second;
	}

	public static <F, S> Pair<F, S> of(F first, S second) {
		return new Pair<>(first, second);
	}

	public F getFirst() {
		return first;
	}

	public S getSecond() {
		return second;
	}

	public void setFirst(F first) {
		this.first = first;
	}

	public void setSecond(S second) {
		this.second = second;
	}

	public Pair<F, S> copy() {
		return Pair.of(first, second);
	}

	@Override
	public boolean equals(final Object obj) {
		if (obj == this)
			return true;
		if (obj instanceof Pair) {
			final Pair<?, ?> other = (Pair<?, ?>) obj;
			return Objects.equals(first, other.first) && Objects.equals(second, other.second);
		}
		return false;
	}

	@Override
	public int hashCode() {
		return (nullHash(first) * 31) ^ nullHash(second);
	}

	int nullHash(Object o) {
		return o == null ? 0 : o.hashCode();
	}

	@Override
	public String toString() {
		return "(" + first + ", " + second + ")";
	}

	public Pair<S, F> swap() {
		return Pair.of(second, first);
	}

	public static <F, S> Codec<Pair<F, S>> codec(Codec<F> firstCodec, Codec<S> secondCodec) {
		return RecordCodecBuilder.create(instance -> instance.group(
			firstCodec.fieldOf("first").forGetter(Pair::getFirst),
			secondCodec.fieldOf("second").forGetter(Pair::getSecond)
		).apply(instance, Pair::new));
	}

	public static <B, F, S> StreamCodec<B, Pair<F, S>> streamCodec(StreamCodec<? super B, F> firstCodec, StreamCodec<? super B, S> secondCodec) {
		return StreamCodec.composite(
			firstCodec, Pair::getFirst,
			secondCodec, Pair::getSecond,
			Pair::new
		);
	}
}
