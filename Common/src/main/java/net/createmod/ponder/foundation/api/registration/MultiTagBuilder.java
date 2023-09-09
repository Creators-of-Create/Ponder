package net.createmod.ponder.foundation.api.registration;

import net.createmod.ponder.foundation.PonderTag;

public interface MultiTagBuilder {

	interface Tag<T> {

		Tag<T> add(T component);

	}

	interface Component {

		Component add(PonderTag tag);

	}

}
