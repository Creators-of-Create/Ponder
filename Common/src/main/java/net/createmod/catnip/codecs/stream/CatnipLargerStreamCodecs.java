package net.createmod.catnip.codecs.stream;

import com.mojang.datafixers.util.Function10;
import com.mojang.datafixers.util.Function11;
import com.mojang.datafixers.util.Function12;
import com.mojang.datafixers.util.Function13;
import com.mojang.datafixers.util.Function14;
import com.mojang.datafixers.util.Function15;
import com.mojang.datafixers.util.Function16;
import com.mojang.datafixers.util.Function7;
import com.mojang.datafixers.util.Function8;
import com.mojang.datafixers.util.Function9;

import net.minecraft.network.codec.StreamCodec;

import org.jetbrains.annotations.NotNull;

import java.util.function.Function;

public interface CatnipLargerStreamCodecs {
    static <B, C, T1, T2, T3, T4, T5, T6, T7> StreamCodec<B, C> composite(
            StreamCodec<? super B, T1> codec1, Function<C, T1> getter1,
            StreamCodec<? super B, T2> codec2, Function<C, T2> getter2,
            StreamCodec<? super B, T3> codec3, Function<C, T3> getter3,
            StreamCodec<? super B, T4> codec4, Function<C, T4> getter4,
            StreamCodec<? super B, T5> codec5, Function<C, T5> getter5,
            StreamCodec<? super B, T6> codec6, Function<C, T6> getter6,
            StreamCodec<? super B, T7> codec7, Function<C, T7> getter7,
            Function7<T1, T2, T3, T4, T5, T6, T7, C> factory
    ) {
        return new StreamCodec<>() {
            @Override
            public @NotNull C decode(B buffer) {
                T1 t1 = codec1.decode(buffer);
                T2 t2 = codec2.decode(buffer);
                T3 t3 = codec3.decode(buffer);
                T4 t4 = codec4.decode(buffer);
                T5 t5 = codec5.decode(buffer);
                T6 t6 = codec6.decode(buffer);
                T7 t7 = codec7.decode(buffer);
                return factory.apply(t1, t2, t3, t4, t5, t6, t7);
            }

            @Override
            public void encode(B buffer, C object) {
                codec1.encode(buffer, getter1.apply(object));
                codec2.encode(buffer, getter2.apply(object));
                codec3.encode(buffer, getter3.apply(object));
                codec4.encode(buffer, getter4.apply(object));
                codec5.encode(buffer, getter5.apply(object));
                codec6.encode(buffer, getter6.apply(object));
                codec7.encode(buffer, getter7.apply(object));
            }
        };
    }

    static <B, C, T1, T2, T3, T4, T5, T6, T7, T8> StreamCodec<B, C> composite(
            StreamCodec<? super B, T1> codec1, Function<C, T1> getter1,
            StreamCodec<? super B, T2> codec2, Function<C, T2> getter2,
            StreamCodec<? super B, T3> codec3, Function<C, T3> getter3,
            StreamCodec<? super B, T4> codec4, Function<C, T4> getter4,
            StreamCodec<? super B, T5> codec5, Function<C, T5> getter5,
            StreamCodec<? super B, T6> codec6, Function<C, T6> getter6,
            StreamCodec<? super B, T7> codec7, Function<C, T7> getter7,
            StreamCodec<? super B, T8> codec8, Function<C, T8> getter8,
            Function8<T1, T2, T3, T4, T5, T6, T7, T8, C> factory
    ) {
        return new StreamCodec<>() {
            @Override
            public @NotNull C decode(B buffer) {
                T1 t1 = codec1.decode(buffer);
                T2 t2 = codec2.decode(buffer);
                T3 t3 = codec3.decode(buffer);
                T4 t4 = codec4.decode(buffer);
                T5 t5 = codec5.decode(buffer);
                T6 t6 = codec6.decode(buffer);
                T7 t7 = codec7.decode(buffer);
                T8 t8 = codec8.decode(buffer);
                return factory.apply(t1, t2, t3, t4, t5, t6, t7, t8);
            }

            @Override
            public void encode(B buffer, C object) {
                codec1.encode(buffer, getter1.apply(object));
                codec2.encode(buffer, getter2.apply(object));
                codec3.encode(buffer, getter3.apply(object));
                codec4.encode(buffer, getter4.apply(object));
                codec5.encode(buffer, getter5.apply(object));
                codec6.encode(buffer, getter6.apply(object));
                codec7.encode(buffer, getter7.apply(object));
                codec8.encode(buffer, getter8.apply(object));
            }
        };
    }

    static <B, C, T1, T2, T3, T4, T5, T6, T7, T8, T9> StreamCodec<B, C> composite(
            StreamCodec<? super B, T1> codec1, Function<C, T1> getter1,
            StreamCodec<? super B, T2> codec2, Function<C, T2> getter2,
            StreamCodec<? super B, T3> codec3, Function<C, T3> getter3,
            StreamCodec<? super B, T4> codec4, Function<C, T4> getter4,
            StreamCodec<? super B, T5> codec5, Function<C, T5> getter5,
            StreamCodec<? super B, T6> codec6, Function<C, T6> getter6,
            StreamCodec<? super B, T7> codec7, Function<C, T7> getter7,
            StreamCodec<? super B, T8> codec8, Function<C, T8> getter8,
            StreamCodec<? super B, T9> codec9, Function<C, T9> getter9,
            Function9<T1, T2, T3, T4, T5, T6, T7, T8, T9, C> factory
    ) {
        return new StreamCodec<>() {
            @Override
            public @NotNull C decode(B buffer) {
                T1 t1 = codec1.decode(buffer);
                T2 t2 = codec2.decode(buffer);
                T3 t3 = codec3.decode(buffer);
                T4 t4 = codec4.decode(buffer);
                T5 t5 = codec5.decode(buffer);
                T6 t6 = codec6.decode(buffer);
                T7 t7 = codec7.decode(buffer);
                T8 t8 = codec8.decode(buffer);
                T9 t9 = codec9.decode(buffer);
                return factory.apply(t1, t2, t3, t4, t5, t6, t7, t8, t9);
            }

            @Override
            public void encode(B buffer, C object) {
                codec1.encode(buffer, getter1.apply(object));
                codec2.encode(buffer, getter2.apply(object));
                codec3.encode(buffer, getter3.apply(object));
                codec4.encode(buffer, getter4.apply(object));
                codec5.encode(buffer, getter5.apply(object));
                codec6.encode(buffer, getter6.apply(object));
                codec7.encode(buffer, getter7.apply(object));
                codec8.encode(buffer, getter8.apply(object));
                codec9.encode(buffer, getter9.apply(object));
            }
        };
    }

    static <B, C, T1, T2, T3, T4, T5, T6, T7, T8, T9, T10> StreamCodec<B, C> composite(
            StreamCodec<? super B, T1> codec1, Function<C, T1> getter1,
            StreamCodec<? super B, T2> codec2, Function<C, T2> getter2,
            StreamCodec<? super B, T3> codec3, Function<C, T3> getter3,
            StreamCodec<? super B, T4> codec4, Function<C, T4> getter4,
            StreamCodec<? super B, T5> codec5, Function<C, T5> getter5,
            StreamCodec<? super B, T6> codec6, Function<C, T6> getter6,
            StreamCodec<? super B, T7> codec7, Function<C, T7> getter7,
            StreamCodec<? super B, T8> codec8, Function<C, T8> getter8,
            StreamCodec<? super B, T9> codec9, Function<C, T9> getter9,
            StreamCodec<? super B, T10> codec10, Function<C, T10> getter10,
            Function10<T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, C> factory
    ) {
        return new StreamCodec<>() {
            @Override
            public @NotNull C decode(B buffer) {
                T1 t1 = codec1.decode(buffer);
                T2 t2 = codec2.decode(buffer);
                T3 t3 = codec3.decode(buffer);
                T4 t4 = codec4.decode(buffer);
                T5 t5 = codec5.decode(buffer);
                T6 t6 = codec6.decode(buffer);
                T7 t7 = codec7.decode(buffer);
                T8 t8 = codec8.decode(buffer);
                T9 t9 = codec9.decode(buffer);
                T10 t10 = codec10.decode(buffer);
                return factory.apply(t1, t2, t3, t4, t5, t6, t7, t8, t9, t10);
            }

            @Override
            public void encode(B buffer, C object) {
                codec1.encode(buffer, getter1.apply(object));
                codec2.encode(buffer, getter2.apply(object));
                codec3.encode(buffer, getter3.apply(object));
                codec4.encode(buffer, getter4.apply(object));
                codec5.encode(buffer, getter5.apply(object));
                codec6.encode(buffer, getter6.apply(object));
                codec7.encode(buffer, getter7.apply(object));
                codec8.encode(buffer, getter8.apply(object));
                codec9.encode(buffer, getter9.apply(object));
                codec10.encode(buffer, getter10.apply(object));
            }
        };
    }

    static <B, C, T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11> StreamCodec<B, C> composite(
            StreamCodec<? super B, T1> codec1, Function<C, T1> getter1,
            StreamCodec<? super B, T2> codec2, Function<C, T2> getter2,
            StreamCodec<? super B, T3> codec3, Function<C, T3> getter3,
            StreamCodec<? super B, T4> codec4, Function<C, T4> getter4,
            StreamCodec<? super B, T5> codec5, Function<C, T5> getter5,
            StreamCodec<? super B, T6> codec6, Function<C, T6> getter6,
            StreamCodec<? super B, T7> codec7, Function<C, T7> getter7,
            StreamCodec<? super B, T8> codec8, Function<C, T8> getter8,
            StreamCodec<? super B, T9> codec9, Function<C, T9> getter9,
            StreamCodec<? super B, T10> codec10, Function<C, T10> getter10,
            StreamCodec<? super B, T11> codec11, Function<C, T11> getter11,
            Function11<T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, C> factory
    ) {
        return new StreamCodec<>() {
            @Override
            public @NotNull C decode(B buffer) {
                T1 t1 = codec1.decode(buffer);
                T2 t2 = codec2.decode(buffer);
                T3 t3 = codec3.decode(buffer);
                T4 t4 = codec4.decode(buffer);
                T5 t5 = codec5.decode(buffer);
                T6 t6 = codec6.decode(buffer);
                T7 t7 = codec7.decode(buffer);
                T8 t8 = codec8.decode(buffer);
                T9 t9 = codec9.decode(buffer);
                T10 t10 = codec10.decode(buffer);
                T11 t11 = codec11.decode(buffer);
                return factory.apply(t1, t2, t3, t4, t5, t6, t7, t8, t9, t10, t11);
            }

            @Override
            public void encode(B buffer, C object) {
                codec1.encode(buffer, getter1.apply(object));
                codec2.encode(buffer, getter2.apply(object));
                codec3.encode(buffer, getter3.apply(object));
                codec4.encode(buffer, getter4.apply(object));
                codec5.encode(buffer, getter5.apply(object));
                codec6.encode(buffer, getter6.apply(object));
                codec7.encode(buffer, getter7.apply(object));
                codec8.encode(buffer, getter8.apply(object));
                codec9.encode(buffer, getter9.apply(object));
                codec10.encode(buffer, getter10.apply(object));
                codec11.encode(buffer, getter11.apply(object));
            }
        };
    }

    static <B, C, T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12> StreamCodec<B, C> composite(
            StreamCodec<? super B, T1> codec1, Function<C, T1> getter1,
            StreamCodec<? super B, T2> codec2, Function<C, T2> getter2,
            StreamCodec<? super B, T3> codec3, Function<C, T3> getter3,
            StreamCodec<? super B, T4> codec4, Function<C, T4> getter4,
            StreamCodec<? super B, T5> codec5, Function<C, T5> getter5,
            StreamCodec<? super B, T6> codec6, Function<C, T6> getter6,
            StreamCodec<? super B, T7> codec7, Function<C, T7> getter7,
            StreamCodec<? super B, T8> codec8, Function<C, T8> getter8,
            StreamCodec<? super B, T9> codec9, Function<C, T9> getter9,
            StreamCodec<? super B, T10> codec10, Function<C, T10> getter10,
            StreamCodec<? super B, T11> codec11, Function<C, T11> getter11,
            StreamCodec<? super B, T12> codec12, Function<C, T12> getter12,
            Function12<T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, C> factory
    ) {
        return new StreamCodec<>() {
            @Override
            public @NotNull C decode(B buffer) {
                T1 t1 = codec1.decode(buffer);
                T2 t2 = codec2.decode(buffer);
                T3 t3 = codec3.decode(buffer);
                T4 t4 = codec4.decode(buffer);
                T5 t5 = codec5.decode(buffer);
                T6 t6 = codec6.decode(buffer);
                T7 t7 = codec7.decode(buffer);
                T8 t8 = codec8.decode(buffer);
                T9 t9 = codec9.decode(buffer);
                T10 t10 = codec10.decode(buffer);
                T11 t11 = codec11.decode(buffer);
                T12 t12 = codec12.decode(buffer);
                return factory.apply(t1, t2, t3, t4, t5, t6, t7, t8, t9, t10, t11, t12);
            }

            @Override
            public void encode(B buffer, C object) {
                codec1.encode(buffer, getter1.apply(object));
                codec2.encode(buffer, getter2.apply(object));
                codec3.encode(buffer, getter3.apply(object));
                codec4.encode(buffer, getter4.apply(object));
                codec5.encode(buffer, getter5.apply(object));
                codec6.encode(buffer, getter6.apply(object));
                codec7.encode(buffer, getter7.apply(object));
                codec8.encode(buffer, getter8.apply(object));
                codec9.encode(buffer, getter9.apply(object));
                codec10.encode(buffer, getter10.apply(object));
                codec11.encode(buffer, getter11.apply(object));
                codec12.encode(buffer, getter12.apply(object));
            }
        };
    }

    static <B, C, T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13> StreamCodec<B, C> composite(
            StreamCodec<? super B, T1> codec1, Function<C, T1> getter1,
            StreamCodec<? super B, T2> codec2, Function<C, T2> getter2,
            StreamCodec<? super B, T3> codec3, Function<C, T3> getter3,
            StreamCodec<? super B, T4> codec4, Function<C, T4> getter4,
            StreamCodec<? super B, T5> codec5, Function<C, T5> getter5,
            StreamCodec<? super B, T6> codec6, Function<C, T6> getter6,
            StreamCodec<? super B, T7> codec7, Function<C, T7> getter7,
            StreamCodec<? super B, T8> codec8, Function<C, T8> getter8,
            StreamCodec<? super B, T9> codec9, Function<C, T9> getter9,
            StreamCodec<? super B, T10> codec10, Function<C, T10> getter10,
            StreamCodec<? super B, T11> codec11, Function<C, T11> getter11,
            StreamCodec<? super B, T12> codec12, Function<C, T12> getter12,
            StreamCodec<? super B, T13> codec13, Function<C, T13> getter13,
            Function13<T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, C> factory
    ) {
        return new StreamCodec<>() {
            @Override
            public @NotNull C decode(B buffer) {
                T1 t1 = codec1.decode(buffer);
                T2 t2 = codec2.decode(buffer);
                T3 t3 = codec3.decode(buffer);
                T4 t4 = codec4.decode(buffer);
                T5 t5 = codec5.decode(buffer);
                T6 t6 = codec6.decode(buffer);
                T7 t7 = codec7.decode(buffer);
                T8 t8 = codec8.decode(buffer);
                T9 t9 = codec9.decode(buffer);
                T10 t10 = codec10.decode(buffer);
                T11 t11 = codec11.decode(buffer);
                T12 t12 = codec12.decode(buffer);
                T13 t13 = codec13.decode(buffer);
                return factory.apply(t1, t2, t3, t4, t5, t6, t7, t8, t9, t10, t11, t12, t13);
            }

            @Override
            public void encode(B buffer, C object) {
                codec1.encode(buffer, getter1.apply(object));
                codec2.encode(buffer, getter2.apply(object));
                codec3.encode(buffer, getter3.apply(object));
                codec4.encode(buffer, getter4.apply(object));
                codec5.encode(buffer, getter5.apply(object));
                codec6.encode(buffer, getter6.apply(object));
                codec7.encode(buffer, getter7.apply(object));
                codec8.encode(buffer, getter8.apply(object));
                codec9.encode(buffer, getter9.apply(object));
                codec10.encode(buffer, getter10.apply(object));
                codec11.encode(buffer, getter11.apply(object));
                codec12.encode(buffer, getter12.apply(object));
                codec13.encode(buffer, getter13.apply(object));
            }
        };
    }

    static <B, C, T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14> StreamCodec<B, C> composite(
            StreamCodec<? super B, T1> codec1, Function<C, T1> getter1,
            StreamCodec<? super B, T2> codec2, Function<C, T2> getter2,
            StreamCodec<? super B, T3> codec3, Function<C, T3> getter3,
            StreamCodec<? super B, T4> codec4, Function<C, T4> getter4,
            StreamCodec<? super B, T5> codec5, Function<C, T5> getter5,
            StreamCodec<? super B, T6> codec6, Function<C, T6> getter6,
            StreamCodec<? super B, T7> codec7, Function<C, T7> getter7,
            StreamCodec<? super B, T8> codec8, Function<C, T8> getter8,
            StreamCodec<? super B, T9> codec9, Function<C, T9> getter9,
            StreamCodec<? super B, T10> codec10, Function<C, T10> getter10,
            StreamCodec<? super B, T11> codec11, Function<C, T11> getter11,
            StreamCodec<? super B, T12> codec12, Function<C, T12> getter12,
            StreamCodec<? super B, T13> codec13, Function<C, T13> getter13,
            StreamCodec<? super B, T14> codec14, Function<C, T14> getter14,
            Function14<T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, C> factory
    ) {
        return new StreamCodec<>() {
            @Override
            public @NotNull C decode(B buffer) {
                T1 t1 = codec1.decode(buffer);
                T2 t2 = codec2.decode(buffer);
                T3 t3 = codec3.decode(buffer);
                T4 t4 = codec4.decode(buffer);
                T5 t5 = codec5.decode(buffer);
                T6 t6 = codec6.decode(buffer);
                T7 t7 = codec7.decode(buffer);
                T8 t8 = codec8.decode(buffer);
                T9 t9 = codec9.decode(buffer);
                T10 t10 = codec10.decode(buffer);
                T11 t11 = codec11.decode(buffer);
                T12 t12 = codec12.decode(buffer);
                T13 t13 = codec13.decode(buffer);
                T14 t14 = codec14.decode(buffer);
                return factory.apply(t1, t2, t3, t4, t5, t6, t7, t8, t9, t10, t11, t12, t13, t14);
            }

            @Override
            public void encode(B buffer, C object) {
                codec1.encode(buffer, getter1.apply(object));
                codec2.encode(buffer, getter2.apply(object));
                codec3.encode(buffer, getter3.apply(object));
                codec4.encode(buffer, getter4.apply(object));
                codec5.encode(buffer, getter5.apply(object));
                codec6.encode(buffer, getter6.apply(object));
                codec7.encode(buffer, getter7.apply(object));
                codec8.encode(buffer, getter8.apply(object));
                codec9.encode(buffer, getter9.apply(object));
                codec10.encode(buffer, getter10.apply(object));
                codec11.encode(buffer, getter11.apply(object));
                codec12.encode(buffer, getter12.apply(object));
                codec13.encode(buffer, getter13.apply(object));
                codec14.encode(buffer, getter14.apply(object));
            }
        };
    }

    static <B, C, T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15> StreamCodec<B, C> composite(
            StreamCodec<? super B, T1> codec1, Function<C, T1> getter1,
            StreamCodec<? super B, T2> codec2, Function<C, T2> getter2,
            StreamCodec<? super B, T3> codec3, Function<C, T3> getter3,
            StreamCodec<? super B, T4> codec4, Function<C, T4> getter4,
            StreamCodec<? super B, T5> codec5, Function<C, T5> getter5,
            StreamCodec<? super B, T6> codec6, Function<C, T6> getter6,
            StreamCodec<? super B, T7> codec7, Function<C, T7> getter7,
            StreamCodec<? super B, T8> codec8, Function<C, T8> getter8,
            StreamCodec<? super B, T9> codec9, Function<C, T9> getter9,
            StreamCodec<? super B, T10> codec10, Function<C, T10> getter10,
            StreamCodec<? super B, T11> codec11, Function<C, T11> getter11,
            StreamCodec<? super B, T12> codec12, Function<C, T12> getter12,
            StreamCodec<? super B, T13> codec13, Function<C, T13> getter13,
            StreamCodec<? super B, T14> codec14, Function<C, T14> getter14,
            StreamCodec<? super B, T15> codec15, Function<C, T15> getter15,
            Function15<T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, C> factory
    ) {
        return new StreamCodec<>() {
            @Override
            public @NotNull C decode(B buffer) {
                T1 t1 = codec1.decode(buffer);
                T2 t2 = codec2.decode(buffer);
                T3 t3 = codec3.decode(buffer);
                T4 t4 = codec4.decode(buffer);
                T5 t5 = codec5.decode(buffer);
                T6 t6 = codec6.decode(buffer);
                T7 t7 = codec7.decode(buffer);
                T8 t8 = codec8.decode(buffer);
                T9 t9 = codec9.decode(buffer);
                T10 t10 = codec10.decode(buffer);
                T11 t11 = codec11.decode(buffer);
                T12 t12 = codec12.decode(buffer);
                T13 t13 = codec13.decode(buffer);
                T14 t14 = codec14.decode(buffer);
                T15 t15 = codec15.decode(buffer);
                return factory.apply(t1, t2, t3, t4, t5, t6, t7, t8, t9, t10, t11, t12, t13, t14, t15);
            }

            @Override
            public void encode(B buffer, C object) {
                codec1.encode(buffer, getter1.apply(object));
                codec2.encode(buffer, getter2.apply(object));
                codec3.encode(buffer, getter3.apply(object));
                codec4.encode(buffer, getter4.apply(object));
                codec5.encode(buffer, getter5.apply(object));
                codec6.encode(buffer, getter6.apply(object));
                codec7.encode(buffer, getter7.apply(object));
                codec8.encode(buffer, getter8.apply(object));
                codec9.encode(buffer, getter9.apply(object));
                codec10.encode(buffer, getter10.apply(object));
                codec11.encode(buffer, getter11.apply(object));
                codec12.encode(buffer, getter12.apply(object));
                codec13.encode(buffer, getter13.apply(object));
                codec14.encode(buffer, getter14.apply(object));
                codec15.encode(buffer, getter15.apply(object));
            }
        };
    }

    static <B, C, T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16> StreamCodec<B, C> composite(
            StreamCodec<? super B, T1> codec1, Function<C, T1> getter1,
            StreamCodec<? super B, T2> codec2, Function<C, T2> getter2,
            StreamCodec<? super B, T3> codec3, Function<C, T3> getter3,
            StreamCodec<? super B, T4> codec4, Function<C, T4> getter4,
            StreamCodec<? super B, T5> codec5, Function<C, T5> getter5,
            StreamCodec<? super B, T6> codec6, Function<C, T6> getter6,
            StreamCodec<? super B, T7> codec7, Function<C, T7> getter7,
            StreamCodec<? super B, T8> codec8, Function<C, T8> getter8,
            StreamCodec<? super B, T9> codec9, Function<C, T9> getter9,
            StreamCodec<? super B, T10> codec10, Function<C, T10> getter10,
            StreamCodec<? super B, T11> codec11, Function<C, T11> getter11,
            StreamCodec<? super B, T12> codec12, Function<C, T12> getter12,
            StreamCodec<? super B, T13> codec13, Function<C, T13> getter13,
            StreamCodec<? super B, T14> codec14, Function<C, T14> getter14,
            StreamCodec<? super B, T15> codec15, Function<C, T15> getter15,
            StreamCodec<? super B, T16> codec16, Function<C, T16> getter16,
            Function16<T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, C> factory
    ) {
        return new StreamCodec<>() {
            @Override
            public @NotNull C decode(B buffer) {
                T1 t1 = codec1.decode(buffer);
                T2 t2 = codec2.decode(buffer);
                T3 t3 = codec3.decode(buffer);
                T4 t4 = codec4.decode(buffer);
                T5 t5 = codec5.decode(buffer);
                T6 t6 = codec6.decode(buffer);
                T7 t7 = codec7.decode(buffer);
                T8 t8 = codec8.decode(buffer);
                T9 t9 = codec9.decode(buffer);
                T10 t10 = codec10.decode(buffer);
                T11 t11 = codec11.decode(buffer);
                T12 t12 = codec12.decode(buffer);
                T13 t13 = codec13.decode(buffer);
                T14 t14 = codec14.decode(buffer);
                T15 t15 = codec15.decode(buffer);
                T16 t16 = codec16.decode(buffer);
                return factory.apply(t1, t2, t3, t4, t5, t6, t7, t8, t9, t10, t11, t12, t13, t14, t15, t16);
            }

            @Override
            public void encode(B buffer, C object) {
                codec1.encode(buffer, getter1.apply(object));
                codec2.encode(buffer, getter2.apply(object));
                codec3.encode(buffer, getter3.apply(object));
                codec4.encode(buffer, getter4.apply(object));
                codec5.encode(buffer, getter5.apply(object));
                codec6.encode(buffer, getter6.apply(object));
                codec7.encode(buffer, getter7.apply(object));
                codec8.encode(buffer, getter8.apply(object));
                codec9.encode(buffer, getter9.apply(object));
                codec10.encode(buffer, getter10.apply(object));
                codec11.encode(buffer, getter11.apply(object));
                codec12.encode(buffer, getter12.apply(object));
                codec13.encode(buffer, getter13.apply(object));
                codec14.encode(buffer, getter14.apply(object));
                codec15.encode(buffer, getter15.apply(object));
                codec16.encode(buffer, getter16.apply(object));
            }
        };
    }
}
