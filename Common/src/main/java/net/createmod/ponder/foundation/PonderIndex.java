package net.createmod.ponder.foundation;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Stream;

public class PonderIndex {

	private static final Set<PonderPlugin> plugins = new HashSet<>();

	public static void addPlugin(PonderPlugin plugin) {
		plugins.add(plugin);
	}

	public static void forEachPlugin(Consumer<PonderPlugin> action) {
		plugins.forEach(action);
	}

	public static Stream<PonderPlugin> streamPlugins() {
		return plugins.stream();
	}

	public static void registerAll() {
		forEachPlugin(PonderPlugin::registerScenes);
		forEachPlugin(PonderPlugin::registerTags);
		//forEachPlugin(PonderPlugin::registerChapters);
	}
}
