package net.createmod.catnip.utility;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import javax.annotation.Nonnull;

import net.minecraft.world.level.LevelAccessor;

public class WorldAttached<T> {

	static List<Map<LevelAccessor, ?>> allMaps = new ArrayList<>();
	Map<LevelAccessor, T> attached;
	private final Function<LevelAccessor, T> factory;

	public WorldAttached(Function<LevelAccessor, T> factory) {
		this.factory = factory;
		attached = new HashMap<>();
		allMaps.add(attached);
	}

	public static void invalidateWorld(LevelAccessor world) {
		allMaps.forEach(m -> m.remove(world));
	}

	@Nonnull
	public T get(LevelAccessor world) {
		T t = attached.get(world);
		if (t != null)
			return t;
		T entry = factory.apply(world);
		put(world, entry);
		return entry;
	}

	public void put(LevelAccessor world, T entry) {
		attached.put(world, entry);
	}

}
