package net.createmod.catnip.platform.services;

import java.util.List;
import java.util.function.Supplier;

public interface PlatformHelper {

	/**
	 * Gets the current loader
	 *
	 * @return The current loader.
	 */
	Loader getLoader();

	/**
	 * Gets the side that the mod currently being run on.
	 *
	 * @return The side the mod is running on, which will be {@link Env#CLIENT} or {@link Env#SERVER}.
	 */
	Env getEnv();

	/**
	 * Checks if a mod with the given id is loaded.
	 *
	 * @param modId The mod to check if it is loaded.
	 *
	 * @return True if the mod is loaded, false otherwise.
	 */
	boolean isModLoaded(String modId);

	/**
	 * Check if the game is currently in a development environment.
	 *
	 * @return True if in a development environment, false otherwise.
	 */
	boolean isDevelopmentEnvironment();

	List<String> getLoadedMods();

	void executeOnClientOnly(Supplier<Runnable> toRun);

	void executeOnServerOnly(Supplier<Runnable> toRun);

	enum Loader {
		FABRIC, FORGE;
	}

	enum Env {
		CLIENT, SERVER;
	}
}
