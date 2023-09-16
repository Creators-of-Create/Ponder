package net.createmod.ponder.api.registration;

import java.util.function.Function;

import net.minecraft.resources.ResourceLocation;

public interface PonderTagRegistrationHelper<T> {

	<S> PonderTagRegistrationHelper<S> withKeyFunction(Function<S, T> keyGen);

	TagBuilder registerTag(ResourceLocation location);

	TagBuilder registerTag(String id);

	void addTagToComponent(T component, ResourceLocation tag);

	MultiTagBuilder.Tag<T> addToTag(ResourceLocation tag);

	MultiTagBuilder.Tag<T> addToTag(ResourceLocation... tags);

	MultiTagBuilder.Component addToComponent(T component);

	MultiTagBuilder.Component addToComponent(T... component);

}
