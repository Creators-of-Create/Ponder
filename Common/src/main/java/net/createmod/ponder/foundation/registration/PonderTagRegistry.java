package net.createmod.ponder.foundation.registration;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimap;

import net.createmod.ponder.foundation.PonderTag;
import net.createmod.ponder.foundation.api.registration.TagRegistryAccess;
import net.minecraft.resources.ResourceLocation;

public class PonderTagRegistry {

	private final PonderLocalization localization;
	private final Multimap<ResourceLocation, PonderTag> tags;
	private final List<PonderTag> listedTags;

	private boolean allowRegistration = true;

	public PonderTagRegistry(PonderLocalization localization) {
		this.localization = localization;
		tags = LinkedHashMultimap.create();
		listedTags = new ArrayList<>();
	}

	public TagRegistryAccess access() {
		return new PonderTagRegistryAccess();
	}

	public void clearRegistry() {
		tags.clear();
		listedTags.clear();
		allowRegistration = true;
	}

	private Set<PonderTag> getTags(ResourceLocation item) {
		return ImmutableSet.copyOf(tags.get(item));
	}

	private Set<ResourceLocation> getItems(PonderTag tag) {
		return tags.entries()
				.stream()
				.filter(e -> e.getValue() == tag)
				.map(Map.Entry::getKey)
				.collect(ImmutableSet.toImmutableSet());
	}

	private List<PonderTag> getListedTags() {
		return listedTags;
	}

	public void listTag(PonderTag tag) {
		if (!allowRegistration)
			throw new IllegalStateException("Registration Phase has already ended!");

		listedTags.add(tag);
	}

	public void add(PonderTag tag, ResourceLocation item) {
		if (!allowRegistration)
			throw new IllegalStateException("Registration Phase has already ended!");

		synchronized (tags) {
			tags.put(item, tag);
		}
	}

	public class PonderTagRegistryAccess implements TagRegistryAccess {

		@Override
		public List<PonderTag> getListedTags() {
			return PonderTagRegistry.this.getListedTags();
		}

		@Override
		public Set<PonderTag> getTags(ResourceLocation item) {
			return PonderTagRegistry.this.getTags(item);
		}

		@Override
		public Set<ResourceLocation> getItems(PonderTag tag) {
			return PonderTagRegistry.this.getItems(tag);
		}
	}

}
