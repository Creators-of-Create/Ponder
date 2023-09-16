package net.createmod.ponder.api.registration;

import net.minecraft.resources.ResourceLocation;

public interface MultiTagBuilder {

	interface Tag<T> {

		Tag<T> add(T component);

	}

	interface Component {

		Component add(ResourceLocation tag);

	}

}
