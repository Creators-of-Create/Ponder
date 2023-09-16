package net.createmod.ponder.api.scene;

@FunctionalInterface
public interface PonderStoryBoard {
	void program(SceneBuilder scene, SceneBuildingUtil util);
}
