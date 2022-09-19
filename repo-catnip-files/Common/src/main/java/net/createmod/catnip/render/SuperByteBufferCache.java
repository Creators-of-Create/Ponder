package net.createmod.catnip.render;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

public class SuperByteBufferCache {

	private static final SuperByteBufferCache INSTANCE = new SuperByteBufferCache();

	public static SuperByteBufferCache getInstance() {
		return INSTANCE;
	}

	protected final Map<Compartment<?>, Cache<Object, SuperByteBuffer>> caches = new HashMap<>();

	public synchronized void registerCompartment(Compartment<?> compartment) {
		caches.put(compartment, CacheBuilder.newBuilder()
			.build());
	}

	public synchronized void registerCompartment(Compartment<?> compartment, long ticksUntilExpired) {
		caches.put(compartment, CacheBuilder.newBuilder()
			.expireAfterAccess(ticksUntilExpired * 50, TimeUnit.MILLISECONDS)
			.build());
	}

	public <T> SuperByteBuffer get(Compartment<T> compartment, T key, Callable<SuperByteBuffer> callable) {
		Cache<Object, SuperByteBuffer> cache = caches.get(compartment);

		if (cache == null)
			throw new IllegalArgumentException("Trying to access Buffer Cache for not registered Compartment: " + compartment + " <" + key.getClass().getSimpleName() + ">");


		try {
			return cache.get(key, callable);
		} catch (ExecutionException e) {
			e.printStackTrace();
			throw new RuntimeException("Unable to populate Buffer Cache for key: " + key + " <" + key.getClass().getSimpleName() + ">");
		}
	}

	public <T> void invalidate(Compartment<T> compartment, T key) {
		caches.get(compartment).invalidate(key);
	}

	public void invalidate(Compartment<?> compartment) {
		caches.get(compartment).invalidateAll();
	}

	public void invalidate() {
		caches.forEach((compartment, cache) -> cache.invalidateAll());
	}

	public static class Compartment<T> {}

}
