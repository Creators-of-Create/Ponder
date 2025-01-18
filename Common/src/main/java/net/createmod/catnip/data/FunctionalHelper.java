package net.createmod.catnip.data;

import java.util.function.Function;

public class FunctionalHelper {

	public static <U> Function<Object, U> filterAndCast(Class<? extends U> clazz) {
		return t -> clazz.isInstance(t) ? clazz.cast(t) : null;
	}

}
