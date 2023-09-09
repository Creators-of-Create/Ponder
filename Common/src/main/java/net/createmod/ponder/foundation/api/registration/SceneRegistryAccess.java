package net.createmod.ponder.foundation.api.registration;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import net.createmod.ponder.foundation.PonderScene;
import net.createmod.ponder.foundation.PonderStoryBoardEntry;
import net.minecraft.resources.ResourceLocation;

public interface SceneRegistryAccess {

	boolean doScenesExistForId(ResourceLocation id);

	Collection<Map.Entry<ResourceLocation, PonderStoryBoardEntry>> getRegisteredEntries();

	List<PonderScene> compile(ResourceLocation id);

	List<PonderScene> compile(Collection<PonderStoryBoardEntry> entries);

}
