package net.createmod.catnip.registration.callback;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

/**
 * A utility to run things as soon as an object is ready, which may be immediately
 */
public class FusedCallback<T> {
	private @Nullable T obj;
	private @Nullable Consumer<@NotNull T> cons;

	public FusedCallback() {}

	public synchronized void provide(@NotNull T obj) {
		this.obj = obj;
		if (cons != null) {
			cons.accept(obj);
			cons = null;
		}
	}

	public synchronized void addListener(@NotNull Consumer<? super T> consumer) {
		if (obj != null) {
			consumer.accept(obj);
		} else if (cons == null) {
			cons = consumer::accept;
		} else {
			cons = cons.andThen(consumer);
		}
	}

	public <B> FusedBiCallback<T, B> extend() {
		FusedBiCallback<T, B> cb = new FusedBiCallback<>();
		this.addListener(cb::provideA);
		return cb;
	}
}
