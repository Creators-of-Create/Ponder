package net.createmod.catnip.codecs;

import java.util.Optional;
import java.util.Set;

import org.apache.commons.lang3.mutable.MutableBoolean;
import org.apache.commons.lang3.mutable.MutableByte;
import org.apache.commons.lang3.mutable.MutableDouble;
import org.apache.commons.lang3.mutable.MutableFloat;
import org.apache.commons.lang3.mutable.MutableInt;
import org.apache.commons.lang3.mutable.MutableLong;
import org.apache.commons.lang3.mutable.MutableShort;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.PrimitiveCodec;

public interface CatnipCodecs {
	Codec<MutableBoolean> MUTABLE_BOOLEAN_CODEC = Codec.BOOL.xmap(MutableBoolean::new, MutableBoolean::getValue);
	Codec<MutableByte> MUTABLE_BYTE_CODEC = Codec.BYTE.xmap(MutableByte::new, MutableByte::getValue);
	Codec<MutableDouble> MUTABLE_DOUBLE_CODEC = Codec.DOUBLE.xmap(MutableDouble::new, MutableDouble::getValue);
	Codec<MutableFloat> MUTABLE_FLOAT_CODEC = Codec.FLOAT.xmap(MutableFloat::new, MutableFloat::getValue);
	Codec<MutableInt> MUTABLE_INT_CODEC = Codec.INT.xmap(MutableInt::new, MutableInt::getValue);
	Codec<MutableLong> MUTABLE_LONG_CODEC = Codec.LONG.xmap(MutableLong::new, MutableLong::getValue);
	Codec<MutableShort> MUTABLE_SHORT_CODEC = Codec.SHORT.xmap(MutableShort::new, MutableShort::getValue);

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
