package net.createmod.catnip.registration.callback;

import net.createmod.catnip.data.Pair;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.BiConsumer;

/**
 * A utility to run things as soon as multiple objects are ready, which may be immediately
 */
public class FusedBiCallback<A, B> {
	private @Nullable A objA;
	private @Nullable B objB;
	private @Nullable BiConsumer<@NotNull A, @NotNull B> cons;

	public FusedBiCallback() {}

	public synchronized void provideA(@NotNull A obj) {
		objA = obj;
		tryRunAndClear();
	}

	public synchronized void provideB(@NotNull B obj) {
		objB = obj;
		tryRunAndClear();
	}

	private void tryRunAndClear() {
		if (cons == null || objA == null || objB == null)
			return;

		cons.accept(objA, objB);
		cons = null;
	}

	public synchronized void addListener(@NotNull BiConsumer<? super A, ? super B> consumer) {
		if (objA != null && objB != null) {
			consumer.accept(objA, objB);
		} else if (cons == null) {
			cons = consumer::accept;
		} else {
			cons = cons.andThen(consumer);
		}
	}

	public <C> FusedBiCallback<Pair<A, B>, C> extend() {
		FusedBiCallback<Pair<A, B>, C> cb = new FusedBiCallback<>();
		this.addListener((a, b) -> cb.provideA(Pair.of(a, b)));
		return cb;
	}

	public static <A, B, C, D> FusedBiCallback<Pair<A, B>, Pair<C, D>> combine(FusedBiCallback<A, B> first, FusedBiCallback<C, D> second) {
		FusedBiCallback<Pair<A, B>, Pair<C, D>> cb = new FusedBiCallback<>();
		first.addListener((a, b) -> cb.provideA(Pair.of(a, b)));
		second.addListener((c, d) -> cb.provideB(Pair.of(c, d)));
		return cb;
	}
}
