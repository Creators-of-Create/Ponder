package net.createmod.ponder.foundation.registration;

import java.util.List;
import java.util.function.Function;

import net.createmod.ponder.foundation.PonderTag;
import net.createmod.ponder.foundation.api.registration.MultiTagBuilder;
import net.createmod.ponder.foundation.api.registration.PonderTagRegistrationHelper;
import net.minecraft.resources.ResourceLocation;

public class DefaultPonderTagRegistrationHelper implements PonderTagRegistrationHelper<ResourceLocation> {

	protected String namespace;
	protected PonderTagRegistry tagRegistry;
	protected PonderLocalization localization;

	public DefaultPonderTagRegistrationHelper(String namespace, PonderTagRegistry tagRegistry, PonderLocalization localization) {
		this.namespace = namespace;
		this.tagRegistry = tagRegistry;
		this.localization = localization;
	}

	@Override
	public <T> PonderTagRegistrationHelper<T> withKeyFunction(Function<T, ResourceLocation> keyGen) {
		return new GenericPonderTagRegistrationHelper<>(this, keyGen);
	}

	@Override
	public PonderTagRegistrationHelper<ResourceLocation> registerTag(PonderTag tag, boolean listTagInIndexScreens) {
		tag.registerLang(localization);

		if (listTagInIndexScreens)
			tagRegistry.listTag(tag);

		return this;
	}

	@Override
	public void addTagToComponent(ResourceLocation component, PonderTag tag) {
		tagRegistry.add(tag, component);
	}

	@Override
	public MultiTagBuilder.Tag<ResourceLocation> addToTag(PonderTag tag) {
		return new GenericMultiTagBuilder<ResourceLocation>().new Tag(this, List.of(tag));
	}

	@Override
	public MultiTagBuilder.Tag<ResourceLocation> addToTag(PonderTag... tags) {
		return new GenericMultiTagBuilder<ResourceLocation>().new Tag(this, List.of(tags));
	}

	@Override
	public MultiTagBuilder.Component addToComponent(ResourceLocation component) {
		return new GenericMultiTagBuilder<ResourceLocation>().new Component(this, List.of(component));
	}

	@Override
	public MultiTagBuilder.Component addToComponent(ResourceLocation... components) {
		return new GenericMultiTagBuilder<ResourceLocation>().new Component(this, List.of(components));
	}
}
