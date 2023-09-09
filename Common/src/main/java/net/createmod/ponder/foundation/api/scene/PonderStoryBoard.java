package net.createmod.ponder.foundation.api.scene;

import net.createmod.ponder.foundation.SceneBuilder;
import net.createmod.ponder.foundation.SceneBuildingUtil;

@FunctionalInterface
public interface PonderStoryBoard {
	void program(SceneBuilder scene, SceneBuildingUtil util);
}
