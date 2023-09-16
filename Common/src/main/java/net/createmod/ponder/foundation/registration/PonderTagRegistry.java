package net.createmod.ponder.foundation.registration;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimap;

import net.createmod.ponder.Ponder;
import net.createmod.ponder.api.registration.TagRegistryAccess;
import net.createmod.ponder.foundation.PonderTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Items;

public class PonderTagRegistry implements TagRegistryAccess {

	private final PonderLocalization localization;
	private final Multimap<ResourceLocation, ResourceLocation> componentTagMap;
	private final Map<ResourceLocation, PonderTag> registeredTags;
	private final List<PonderTag> listedTags;

	private final PonderTag MISSING = new PonderTag(Ponder.asResource("not_registered"), null,
													Items.BARRIER.getDefaultInstance(),
													Items.BARRIER.getDefaultInstance());

	private boolean allowRegistration = true;

	public PonderTagRegistry(PonderLocalization localization) {
		this.localization = localization;
		componentTagMap = LinkedHashMultimap.create();
		registeredTags = new HashMap<>();
		listedTags = new ArrayList<>();
	}

	public void clearRegistry() {
		componentTagMap.clear();
		listedTags.clear();
		allowRegistration = true;
	}

	//

	public void registerTag(PonderTag tag) {
		if (!allowRegistration)
			throw new IllegalStateException("Registration Phase has already ended!");

		registeredTags.put(tag.getId(), tag);
	}

	public void listTag(PonderTag tag) {
		if (!allowRegistration)
			throw new IllegalStateException("Registration Phase has already ended!");

		listedTags.add(tag);
	}

	public void addTagToComponent(ResourceLocation tag, ResourceLocation item) {
		if (!allowRegistration)
			throw new IllegalStateException("Registration Phase has already ended!");

		synchronized (componentTagMap) {
			componentTagMap.put(item, tag);
		}
	}

	//

	@Override
	public PonderTag getRegisteredTag(ResourceLocation tagLocation) {
		return registeredTags.getOrDefault(tagLocation, MISSING);
	}

	@Override
	public List<PonderTag> getListedTags() {
		return listedTags;
	}

	@Override
	public Set<PonderTag> getTags(ResourceLocation item) {
		return componentTagMap.get(item).stream().map(this::getRegisteredTag).collect(Collectors.toUnmodifiableSet());
	}

	@Override
	public Set<ResourceLocation> getItems(ResourceLocation tag) {
		return componentTagMap.entries()
				.stream()
				.filter(e -> e.getValue().equals(tag))
				.map(Map.Entry::getKey)
				.collect(ImmutableSet.toImmutableSet());
	}

	@Override
	public Set<ResourceLocation> getItems(PonderTag tag) {
		return getItems(tag.getId());
	}

}
