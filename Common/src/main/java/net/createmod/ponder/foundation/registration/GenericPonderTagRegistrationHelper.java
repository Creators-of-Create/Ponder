package net.createmod.ponder.foundation.registration;

import java.util.List;
import java.util.function.Function;

import net.createmod.ponder.api.registration.MultiTagBuilder;
import net.createmod.ponder.api.registration.PonderTagRegistrationHelper;
import net.createmod.ponder.api.registration.TagBuilder;
import net.minecraft.resources.ResourceLocation;

public class GenericPonderTagRegistrationHelper<T> implements PonderTagRegistrationHelper<T> {

	private final PonderTagRegistrationHelper<ResourceLocation> helperDelegate;
	private final Function<T, ResourceLocation> keyGen;

	public GenericPonderTagRegistrationHelper(PonderTagRegistrationHelper<ResourceLocation> helperDelegate,
											  Function<T, ResourceLocation> keyGen) {
		this.helperDelegate = helperDelegate;
		this.keyGen = keyGen;
	}

	@Override
	public <S> PonderTagRegistrationHelper<S> withKeyFunction(Function<S, T> keyGen) {
		return new GenericPonderTagRegistrationHelper<>(helperDelegate, keyGen.andThen(this.keyGen));
	}

	@Override
	public TagBuilder registerTag(ResourceLocation location) {
		return helperDelegate.registerTag(location);
	}

	@Override
	public TagBuilder registerTag(String id) {
		return helperDelegate.registerTag(id);
	}

	@Override
	public void addTagToComponent(T component, ResourceLocation tag) {
		helperDelegate.addTagToComponent(keyGen.apply(component), tag);
	}

	@Override
	public MultiTagBuilder.Tag<T> addToTag(ResourceLocation tag) {
		return new GenericMultiTagBuilder<T>().new Tag(this, List.of(tag));
	}

	@Override
	public MultiTagBuilder.Tag<T> addToTag(ResourceLocation... tags) {
		return new GenericMultiTagBuilder<T>().new Tag(this, List.of(tags));
	}

	@Override
	public MultiTagBuilder.Component addToComponent(T component) {
		return new GenericMultiTagBuilder<T>().new Component(this, List.of(component));
	}

	@Override
	public MultiTagBuilder.Component addToComponent(T... components) {
		return new GenericMultiTagBuilder<T>().new Component(this, List.of(components));
	}
}
