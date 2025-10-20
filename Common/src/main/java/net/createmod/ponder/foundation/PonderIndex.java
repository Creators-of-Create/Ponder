package net.createmod.ponder.foundation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Stream;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.common.base.Stopwatch;

import net.createmod.ponder.api.registration.LangRegistryAccess;
import net.createmod.ponder.api.registration.PonderPlugin;
import net.createmod.ponder.api.registration.SceneRegistryAccess;
import net.createmod.ponder.api.registration.TagRegistryAccess;
import net.createmod.ponder.enums.PonderConfig;
import net.createmod.ponder.foundation.registration.DefaultPonderSceneRegistrationHelper;
import net.createmod.ponder.foundation.registration.DefaultPonderTagRegistrationHelper;
import net.createmod.ponder.foundation.registration.DefaultSharedTextRegistrationHelper;
import net.createmod.ponder.foundation.registration.PonderLocalization;
import net.createmod.ponder.foundation.registration.PonderSceneRegistry;
import net.createmod.ponder.foundation.registration.PonderTagRegistry;

public class PonderIndex {

	private static final PonderLocalization LOCALIZATION = new PonderLocalization();
	private static final PonderSceneRegistry SCENES = new PonderSceneRegistry(LOCALIZATION);
	private static final PonderTagRegistry TAGS = new PonderTagRegistry(LOCALIZATION);

	private static final List<PonderPlugin> plugins = new ArrayList<>();

	private static final Comparator<PonderPlugin> pluginComparator = Comparator.comparing(
		(PonderPlugin plugin) -> !plugin.getModId().equals("create")
	).thenComparing(PonderPlugin::getModId);

	private static final Logger LOGGER = LogManager.getLogger("PonderIndex");

	public static void addPlugin(PonderPlugin plugin) {
		// synchronized since mod loading is asynchronous on Forge
		synchronized (plugins) {
			// we want plugins to be sorted by mod ID, but using a SortedSet would only allow 1 plugin for each mod ID.
			// this finds the correct location to insert the plugin.
			int index = Collections.binarySearch(plugins, plugin, pluginComparator);
			int insertionPoint = index >= 0 ? index : -index - 1;
			plugins.add(insertionPoint, plugin);
		}
	}

	public static void forEachPlugin(Consumer<PonderPlugin> action) {
		plugins.forEach(action);
	}

	public static Stream<PonderPlugin> streamPlugins() {
		return plugins.stream();
	}

	public static void reload() {
		LOGGER.info("Reloading all Ponder Plugins ...");
		Stopwatch stopwatch = Stopwatch.createStarted();
		LOCALIZATION.clearShared();
		SCENES.clearRegistry();
		TAGS.clearRegistry();

		registerAll();
		gatherSharedText();
		LOGGER.info("Reloading Ponder Plugins took {}", stopwatch.stop());
	}

	public static void registerAll() {
		Stopwatch stopwatch = Stopwatch.createStarted();
		forEachPlugin(plugin -> plugin.registerScenes(new DefaultPonderSceneRegistrationHelper(plugin.getModId(), SCENES)));
		LOGGER.info("Registering Ponder Scenes took {}", stopwatch.stop());

		stopwatch.reset().start();
		forEachPlugin(plugin -> plugin.registerTags(new DefaultPonderTagRegistrationHelper(plugin.getModId(), TAGS, LOCALIZATION)));
		LOGGER.info("Registering Ponder Tags took {}", stopwatch.stop());
	}

	public static void gatherSharedText() {
		Stopwatch stopwatch = Stopwatch.createStarted();
		forEachPlugin(plugin -> plugin.registerSharedText(new DefaultSharedTextRegistrationHelper(plugin.getModId(), LOCALIZATION)));
		LOGGER.info("Collecting Shared Ponder Text took {}", stopwatch.stop());
	}

	public static SceneRegistryAccess getSceneAccess() {
		return SCENES;
	}

	public static TagRegistryAccess getTagAccess() {
		return TAGS;
	}

	public static LangRegistryAccess getLangAccess() {
		return LOCALIZATION;
	}

	public static boolean editingModeActive() {
		return PonderConfig.Client().editingMode.get();
	}
}
