package net.createmod.ponder.api.registration;

import java.util.List;

import net.createmod.ponder.api.scene.PonderStoryBoard;
import net.minecraft.resources.ResourceLocation;

public interface StoryBoardEntry {
	PonderStoryBoard getBoard();

	String getNamespace();

	ResourceLocation getSchematicLocation();

	ResourceLocation getComponent();

	List<ResourceLocation> getTags();

	List<SceneOrderingEntry> getOrderingEntries();

	/**
	 * inside the PonderUI, will order this scene somewhere <i>before</i> the other scene.
	 * only has an effect if a scene with the given id can be found.
	 * <br />
	 * use {@link StoryBoardEntry#orderBefore(String, String)} to target scenes added by other mods.
	 *
	 * @param otherSceneId id of the scene that should appear after this one
	 * @return this StoryBoardEntry
	 */
	default StoryBoardEntry orderBefore(String otherSceneId) {
		return orderBefore(getNamespace(), otherSceneId);
	}

	/**
	 * inside the PonderUI, will order this scene somewhere <i>before</i> the other scene.
	 * only has an effect if a scene with the given id can be found.
	 *
	 * @param namespace modId of the mod that added the other scene
	 * @param otherSceneId id of the scene that should appear after this one
	 * @return this StoryBoardEntry
	 */
	StoryBoardEntry orderBefore(String namespace, String otherSceneId);

	/**
	 * inside the PonderUI, will order this scene somewhere <i>after</i> the other scene.
	 * only has an effect if a scene with the given id can be found.
	 * <br />
	 * use {@link StoryBoardEntry#orderAfter(String, String)} to target scenes added by other mods.
	 *
	 * @param otherSceneId id of the scene that should appear before this one
	 * @return this StoryBoardEntry
	 */
	default StoryBoardEntry orderAfter(String otherSceneId) {
		return orderAfter(getNamespace(), otherSceneId);
	}

	/**
	 * inside the PonderUI, will order this scene somewhere <i>after</i> the other scene.
	 * only has an effect if a scene with the given id can be found.
	 *
	 * @param namespace modId of the mod that added the other scene
	 * @param otherSceneId id of the scene that should appear before this one
	 * @return this StoryBoardEntry
	 */
	StoryBoardEntry orderAfter(String namespace, String otherSceneId);

	/**
	 * causes the supplied PonderTag to flash when viewing this scene in the PonderUI
	 *
	 * @return this StoryBoardEntry
	 */
	StoryBoardEntry highlightTag(ResourceLocation tag);

	/**
	 * causes the supplied PonderTags to flash when viewing this scene in the PonderUI
	 *
	 * @return this StoryBoardEntry
	 */
	StoryBoardEntry highlightTags(ResourceLocation... tags);

	/**
	 * causes all assigned PonderTags to flash when viewing this scene in the PonderUI
	 *
	 * @return this StoryBoardEntry
	 */
	StoryBoardEntry highlightAllTags();

	enum SceneOrderingType {
		BEFORE, AFTER
	}

	record SceneOrderingEntry(SceneOrderingType type, ResourceLocation sceneId) {

		public static SceneOrderingEntry after(String namespace, String sceneId) {
			return new SceneOrderingEntry(StoryBoardEntry.SceneOrderingType.AFTER, new ResourceLocation(namespace, sceneId));
		}

		public static SceneOrderingEntry before(String namespace, String sceneId) {
			return new SceneOrderingEntry(StoryBoardEntry.SceneOrderingType.BEFORE, new ResourceLocation(namespace, sceneId));
		}
	}
}
