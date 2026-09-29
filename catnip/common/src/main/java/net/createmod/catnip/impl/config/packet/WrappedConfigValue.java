package net.createmod.catnip.impl.config.packet;

import io.netty.buffer.ByteBuf;
import net.createmod.catnip.api.data.codec.stream.CatnipStreamCodecBuilders;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

import java.util.Optional;

public sealed interface WrappedConfigValue {
	StreamCodec<ByteBuf, WrappedConfigValue> STREAM_CODEC = Type.STREAM_CODEC.dispatch(Type::of, type -> type.streamCodec);

	static WrappedConfigValue wrapOrThrow(Object value) {
		return wrap(value).orElseThrow(() -> new IllegalArgumentException("Cannot wrap value " + value));
	}

	static Optional<WrappedConfigValue> wrap(Object value) {
		return Optional.ofNullable(switch (value) {
			case Boolean b -> new Bool(b);
			case Integer i -> new Int(i);
			case Float f -> new Float32(f);
			case Double d -> new Float64(d);
			case String s -> new Str(s);
			case Enum<?> e -> new EnumOrdinal(e.ordinal());
			default -> null;
		});
	}

	record Bool(boolean value) implements WrappedConfigValue {}
	record Int(int value) implements WrappedConfigValue {}
	record Float32(float value) implements WrappedConfigValue {}
	record Float64(double value) implements WrappedConfigValue {}
	record Str(String value) implements WrappedConfigValue {}
	record EnumOrdinal(int ordinal) implements WrappedConfigValue {}

	enum Type {
		BOOL(ByteBufCodecs.BOOL.map(Bool::new, Bool::value)),
		INT(ByteBufCodecs.VAR_INT.map(Int::new, Int::value)),
		FLOAT(ByteBufCodecs.FLOAT.map(Float32::new, Float32::value)),
		DOUBLE(ByteBufCodecs.DOUBLE.map(Float64::new, Float64::value)),
		STRING(ByteBufCodecs.STRING_UTF8.map(Str::new, Str::value)),
		ENUM(ByteBufCodecs.VAR_INT.map(EnumOrdinal::new, EnumOrdinal::ordinal));

		public static final StreamCodec<ByteBuf, Type> STREAM_CODEC = CatnipStreamCodecBuilders.ofEnum(Type.class);

		public final StreamCodec<ByteBuf, ? extends WrappedConfigValue> streamCodec;

		Type(StreamCodec<ByteBuf, ? extends WrappedConfigValue> streamCodec) {
			this.streamCodec = streamCodec;
		}

		private static Type of(WrappedConfigValue wrapped) {
			return switch (wrapped) {
				case Bool _ -> BOOL;
				case Int _ -> INT;
				case Float32 _ -> FLOAT;
				case Float64 _ -> DOUBLE;
				case Str _ -> STRING;
				case EnumOrdinal _ -> ENUM;
			};
		}
	}
}
