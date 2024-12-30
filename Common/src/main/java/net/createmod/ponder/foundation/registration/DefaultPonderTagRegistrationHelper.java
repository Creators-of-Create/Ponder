package net.createmod.ponder.foundation.registration;

import java.util.List;
import java.util.function.Function;

import net.createmod.ponder.api.registration.MultiTagBuilder;
import net.createmod.ponder.api.registration.PonderTagRegistrationHelper;
import net.createmod.ponder.api.registration.TagBuilder;
import net.createmod.ponder.foundation.PonderTag;
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
	public TagBuilder registerTag(ResourceLocation location) {
		return new PonderTagBuilder(location, this::finishTagRegister);
	}

	@Override
	public TagBuilder registerTag(String id) {
		return new PonderTagBuilder(ResourceLocation.fromNamespaceAndPath(namespace, id), this::finishTagRegister);
	}

	private void finishTagRegister(PonderTagBuilder builder) {
		localization.registerTag(builder.id, builder.title, builder.description);

		PonderTag tag = new PonderTag(builder.id, builder.textureIconLocation, builder.itemIcon, builder.mainItem);
		tagRegistry.registerTag(tag);

		if (builder.addToIndex)
			tagRegistry.listTag(tag);
	}

	@Override
	public void addTagToComponent(ResourceLocation component, ResourceLocation tag) {
		tagRegistry.addTagToComponent(tag, component);
	}

	@Override
	public MultiTagBuilder.Tag<ResourceLocation> addToTag(ResourceLocation tag) {
		return new GenericMultiTagBuilder<ResourceLocation>().new Tag(this, List.of(tag));
	}

	@Override
	public MultiTagBuilder.Tag<ResourceLocation> addToTag(ResourceLocation... tags) {
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
