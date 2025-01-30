package net.createmod.catnip.codecs;

import java.util.Optional;
import java.util.Set;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.PrimitiveCodec;

public interface CatnipCodecs {
	PrimitiveCodec<Character> CHAR = new PrimitiveCodec<>() {
		@Override
		public <T> DataResult<Character> read(final DynamicOps<T> ops, final T input) {
			return ops.getNumberValue(input)
				.map(n -> (char) n.intValue());
		}

		@Override
		public <T> T write(final DynamicOps<T> ops, final Character value) {
			return ops.createInt(value);
		}

		@Override
		public String toString() {
			return "Char";
		}
	};

	static <E> Codec<Set<E>> set(final Codec<E> elementCodec) {
		return set(elementCodec, 0, Integer.MAX_VALUE);
	}

	static <E> Codec<Set<E>> set(final Codec<E> elementCodec, final int minSize, final int maxSize) {
		return new SetCodec<>(elementCodec, minSize, maxSize);
	}

	static <T> MapCodec<T> nullableFieldOf(Codec<T> codec, String fieldName) {
		return nullableFieldOf(codec, fieldName, null);
	}

	static <T> MapCodec<T> nullableFieldOf(Codec<T> codec, String fieldName, T defaultValue) {
		return codec
			.optionalFieldOf(fieldName)
			.xmap(o -> o.orElse(defaultValue), Optional::ofNullable);
	}
}
