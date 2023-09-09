package net.createmod.ponder.foundation.api.registration;

import java.util.function.Function;

import net.createmod.ponder.foundation.PonderTag;

public interface PonderTagRegistrationHelper<T> {

	<S> PonderTagRegistrationHelper<S> withKeyFunction(Function<S, T> keyGen);

	default PonderTagRegistrationHelper<T> registerTag(PonderTag tag) {
		return registerTag(tag, false);
	}

	PonderTagRegistrationHelper<T> registerTag(PonderTag tag, boolean listTagInIndexScreens);

	void addTagToComponent(T component, PonderTag tag);

	MultiTagBuilder.Tag<T> addToTag(PonderTag tag);

	MultiTagBuilder.Tag<T> addToTag(PonderTag... tags);

	MultiTagBuilder.Component addToComponent(T component);

	MultiTagBuilder.Component addToComponent(T... component);

}
