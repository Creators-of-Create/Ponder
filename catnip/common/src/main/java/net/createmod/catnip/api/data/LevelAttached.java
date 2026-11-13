package net.createmod.catnip.api.data;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;

import org.jspecify.annotations.NonNull;

import net.minecraft.world.level.LevelAccessor;

public class LevelAttached<T> {
	// weak references to prevent leaking hashmaps when a LevelAttached is GC'd during runtime
	static List<WeakReference<Map<LevelAccessor, ?>>> allMaps = new ArrayList<>();
	private final Map<LevelAccessor, T> attached;
	private final Function<LevelAccessor, T> factory;

	public LevelAttached(Function<LevelAccessor, T> factory) {
		this.factory = factory;
		// Weak key hashmaps prevent levels not existing anywhere else from leaking memory.
		// This is only a fallback in the event that unload events fail to fire for any reason.
		attached = new WeakHashMap<>();
		allMaps.add(new WeakReference<>(attached));
	}

	public static void invalidateLevel(LevelAccessor level) {
		var i = allMaps.iterator();
		while (i.hasNext()) {
			Map<LevelAccessor, ?> map = i.next()
				.get();
			if (map == null) {
				// If the map has been GC'd, remove the weak reference
				i.remove();
			} else {
				// Prevent leaks
				map.remove(level);
			}
		}
	}

	@NonNull
	public T get(LevelAccessor level) {
		T t = attached.get(level);
		if (t != null) return t;
		T entry = factory.apply(level);
		put(level, entry);
		return entry;
	}

	public void put(LevelAccessor level, T entry) {
		attached.put(level, entry);
	}

	/**
	 * Replaces the entry with a new one from the factory and returns the new entry.
	 */
	@NonNull
	public T replace(LevelAccessor level) {
		attached.remove(level);

		return get(level);
	}

	/**
	 * Replaces the entry with a new one from the factory and returns the new entry.
	 */
	@NonNull
	public T replace(LevelAccessor level, Consumer<T> finalizer) {
		T remove = attached.remove(level);

		if (remove != null)
			finalizer.accept(remove);

		return get(level);
	}

	/**
	 * Deletes all entries after calling a function on them.
	 *
	 * @param finalizer Do something with all of the level-value pairs
	 */
	public void empty(BiConsumer<LevelAccessor, T> finalizer) {
		attached.forEach(finalizer);
		attached.clear();
	}

	/**
	 * Deletes all entries after calling a function on them.
	 *
	 * @param finalizer Do something with all of the values
	 */
	public void empty(Consumer<T> finalizer) {
		attached.values().forEach(finalizer);
		attached.clear();
	}
}
