package net.createmod.catnip.registration.callback;

import java.util.Objects;

@FunctionalInterface
public interface TriConsumer<A, B, C> {
	void accept(A a, B b, C c);

	default TriConsumer<A, B, C> andThen(TriConsumer<? super A, ? super B, ? super C> after) {
		Objects.requireNonNull(after);

		return (a, b, c) -> {
			accept(a, b, c);
			after.accept(a, b, c);
		};
	}
}
