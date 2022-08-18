package net.createmod.ponder.foundation;

import java.util.HashSet;
import java.util.Set;

public class PonderIndex {

	private static final Set<Runnable> callbacks = new HashSet<>();

	public static void registerAll() {
		callbacks.forEach(Runnable::run);
	}

	/**
	 * Add a callback here to make sure all of your Localized Ponder Objects are
	 * registered when Lang creation occurs
	 * <p>
	 * An Example can be found in Create's Client Init Method
	 *
	 * @param register A method that loads your scenes, tags or chapters
	 */
	public static void addIndex(Runnable register) {
		callbacks.add(register);
	}

}
