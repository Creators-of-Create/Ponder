package net.createmod.catnip.codecs.stream;

import java.lang.reflect.Array;
import java.util.List;
import java.util.Optional;

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.mojang.datafixers.util.Pair;

import io.netty.buffer.ByteBuf;
import net.minecraft.core.NonNullList;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.VarInt;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public interface CatnipStreamCodecBuilders {
    static <T extends ByteBuf, S extends Enum<S>> StreamCodec<T, S> ofEnum(Class<S> clazz) {
        return new StreamCodec<>() {
            public @NotNull S decode(@NotNull T buffer) {
                return clazz.getEnumConstants()[VarInt.read(buffer)];
            }

            public void encode(@NotNull T buffer, @NotNull S value) {
                VarInt.write(buffer, value.ordinal());
            }
        };
    }

    static <B extends ByteBuf, L, R> StreamCodec<B, Pair<L, R>> pair(StreamCodec<B, L> codecL, StreamCodec<B, R> codecR) {
        return new StreamCodec<>() {
            @Override
            public @NotNull Pair<L, R> decode(B buffer) {
                L l = codecL.decode(buffer);
                R r = codecR.decode(buffer);
                return Pair.of(l, r);
            }

            @Override
            public void encode(B buffer, Pair<L, R> value) {
                codecL.encode(buffer, value.getFirst());
                codecR.encode(buffer, value.getSecond());
            }
        };
    }

    static <B extends ByteBuf, V> StreamCodec.CodecOperation<B, V, Optional<V>> optional() {
        return ByteBufCodecs::optional;
    }

    static <B extends ByteBuf, V> StreamCodec<B, @Nullable V> nullable(StreamCodec<B, V> base) {
        return new StreamCodec<>() {
            @Override
            @SuppressWarnings("NullableProblems")
            public @Nullable V decode(@NotNull B buffer) {
				return FriendlyByteBuf.readNullable(buffer, base);
            }

            @Override
            public void encode(@NotNull B buffer, @Nullable V value) {
				FriendlyByteBuf.writeNullable(buffer, value, base);
            }
        };
    }

    static <B extends ByteBuf, V> StreamCodec.CodecOperation<B, V, @Nullable V> nullable() {
        return CatnipStreamCodecBuilders::nullable;
    }

    static <B extends ByteBuf, V> StreamCodec<B, List<V>> list(StreamCodec<B, V> base) {
        return base.apply(ByteBufCodecs.list());
    }

    static <B extends ByteBuf, V> StreamCodec<B, List<V>> list(StreamCodec<B, V> base, int maxSize) {
        return base.apply(ByteBufCodecs.list(maxSize));
    }

    static <B extends ByteBuf, V> StreamCodec.CodecOperation<B, V, NonNullList<V>> nonNullList() {
        return streamCodec -> ByteBufCodecs.collection(NonNullList::createWithCapacity, streamCodec);
    }

    static <B extends ByteBuf, V> StreamCodec.CodecOperation<B, V, NonNullList<V>> nonNullList(int maxSize) {
        return streamCodec -> ByteBufCodecs.collection(NonNullList::createWithCapacity, streamCodec, maxSize);
    }

    static <B extends ByteBuf, V> StreamCodec<B, NonNullList<V>> nonNullList(StreamCodec<B, V> base) {
        return base.apply(nonNullList());
    }

    static <B extends ByteBuf, V> StreamCodec<B, NonNullList<V>> nonNullList(StreamCodec<B, V> base, int maxSize) {
        return base.apply(nonNullList(maxSize));
    }

    static <B extends FriendlyByteBuf, V> StreamCodec<B, V[]> array(StreamCodec<? super B, V> base, Class<?> clazz) {
        return new StreamCodec<>() {
            @Override
            public V @NotNull [] decode(@NotNull B buffer) {
                int size = buffer.readVarInt();
                @SuppressWarnings("unchecked")
                V[] array = (V[]) Array.newInstance(clazz, size);
                for (int i = 0; i < size; i++) {
                    array[i] = base.decode(buffer);
                }
                return array;
            }

            @Override
            public void encode(@NotNull B buffer, @NotNull V[] value) {
                buffer.writeVarInt(value.length);
                for (V v : value) {
                    base.encode(buffer, v);
                }
            }
        };
    }

    static <B extends FriendlyByteBuf, V> StreamCodec.CodecOperation<B, V, V[]> array(Class<?> clazz) {
        return streamCodec -> array(streamCodec, clazz);
    }

	static <T> StreamCodec<ByteBuf, TagKey<T>> tagKey(ResourceKey<? extends Registry<T>> registry) {
		return ResourceLocation.STREAM_CODEC.map(id -> TagKey.create(registry, id), TagKey::location);
	}
}
