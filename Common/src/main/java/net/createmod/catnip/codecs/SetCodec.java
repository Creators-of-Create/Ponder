package net.createmod.catnip.codecs;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import com.mojang.datafixers.util.Pair;
import com.mojang.datafixers.util.Unit;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.Lifecycle;
import com.mojang.serialization.ListBuilder;

public record SetCodec<E>(Codec<E> elementCodec, int minSize, int maxSize) implements Codec<Set<E>> {
    private <R> DataResult<R> createTooShortError(final int size) {
        return DataResult.error(() -> "Set is too short: " + size + ", expected range [" + minSize + "-" + maxSize + "]");
    }

    private <R> DataResult<R> createTooLongError(final int size) {
        return DataResult.error(() -> "Set is too long: " + size + ", expected range [" + minSize + "-" + maxSize + "]");
    }

    @Override
    public <T> DataResult<T> encode(final Set<E> input, final DynamicOps<T> ops, final T prefix) {
        if (input.size() < minSize) {
            return createTooShortError(input.size());
        }
        if (input.size() > maxSize) {
            return createTooLongError(input.size());
        }
        final ListBuilder<T> builder = ops.listBuilder();
        for (final E element : input) {
            builder.add(elementCodec.encodeStart(ops, element));
        }
        return builder.build(prefix);
    }

    @Override
    public <T> DataResult<Pair<Set<E>, T>> decode(final DynamicOps<T> ops, final T input) {
        return ops.getList(input).setLifecycle(Lifecycle.stable()).flatMap(stream -> {
            final DecoderState<T> decoder = new DecoderState<>(ops);
            stream.accept(decoder::accept);
            return decoder.build();
        });
    }

    @Override
    public String toString() {
        return "SetCodec[" + elementCodec + ']';
    }

    private class DecoderState<T> {
        private static final DataResult<Unit> INITIAL_RESULT = DataResult.success(Unit.INSTANCE, Lifecycle.stable());

        private final DynamicOps<T> ops;
        private final List<E> elements = new ArrayList<>();
        private final Stream.Builder<T> failed = Stream.builder();
        private DataResult<Unit> result = INITIAL_RESULT;
        private int totalCount;

        private DecoderState(final DynamicOps<T> ops) {
            this.ops = ops;
        }

        public void accept(final T value) {
            totalCount++;
            if (elements.size() >= maxSize) {
                failed.add(value);
                return;
            }
            final DataResult<Pair<E, T>> elementResult = elementCodec.decode(ops, value);
            elementResult.error().ifPresent(error -> failed.add(value));
            elementResult.resultOrPartial().ifPresent(pair -> elements.add(pair.getFirst()));
            result = result.apply2stable((result, element) -> result, elementResult);
        }

        public DataResult<Pair<Set<E>, T>> build() {
            if (elements.size() < minSize) {
                return createTooShortError(elements.size());
            }
            final T errors = ops.createList(failed.build());
            final Pair<Set<E>, T> pair = Pair.of(Set.copyOf(elements), errors);
            if (totalCount > maxSize) {
                result = createTooLongError(totalCount);
            }
            return result.map(ignored -> pair).setPartial(pair);
        }
    }
}
