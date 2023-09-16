package net.createmod.ponder.api.registration;

import java.util.List;
import java.util.Set;

import net.createmod.ponder.foundation.PonderTag;
import net.minecraft.resources.ResourceLocation;

public interface TagRegistryAccess {

	PonderTag getRegisteredTag(ResourceLocation tagLocation);

	List<PonderTag> getListedTags();

	Set<PonderTag> getTags(ResourceLocation item);

	Set<ResourceLocation> getItems(ResourceLocation tag);
	Set<ResourceLocation> getItems(PonderTag tag);

}
