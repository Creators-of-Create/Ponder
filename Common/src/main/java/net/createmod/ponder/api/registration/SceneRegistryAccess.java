package net.createmod.ponder.api.registration;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import net.createmod.ponder.foundation.PonderScene;
import net.minecraft.resources.ResourceLocation;

public interface SceneRegistryAccess {

	boolean doScenesExistForId(ResourceLocation id);

	Collection<Map.Entry<ResourceLocation, StoryBoardEntry>> getRegisteredEntries();

	List<PonderScene> compile(ResourceLocation id);

	List<PonderScene> compile(Collection<StoryBoardEntry> entries);

}
