package net.createmod.ponder.foundation.api.registration;

import java.util.List;
import java.util.Set;

import net.createmod.ponder.foundation.PonderTag;
import net.minecraft.resources.ResourceLocation;

public interface TagRegistryAccess {

	List<PonderTag> getListedTags();

	Set<PonderTag> getTags(ResourceLocation item);

	Set<ResourceLocation> getItems(PonderTag tag);

}
