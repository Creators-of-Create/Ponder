package net.createmod.ponder.foundation.api.registration;

import java.util.function.Consumer;

import net.createmod.ponder.foundation.PonderStoryBoardEntry;
import net.createmod.ponder.foundation.PonderTag;
import net.createmod.ponder.foundation.api.scene.PonderStoryBoard;
import net.minecraft.resources.ResourceLocation;

public interface MultiSceneBuilder {
	MultiSceneBuilder addStoryBoard(ResourceLocation schematicLocation,
									PonderStoryBoard storyBoard);

	MultiSceneBuilder addStoryBoard(ResourceLocation schematicLocation,
									PonderStoryBoard storyBoard, PonderTag... tags);

	MultiSceneBuilder addStoryBoard(ResourceLocation schematicLocation,
									PonderStoryBoard storyBoard,
									Consumer<PonderStoryBoardEntry> extras);

	MultiSceneBuilder addStoryBoard(String schematicPath, PonderStoryBoard storyBoard);

	MultiSceneBuilder addStoryBoard(String schematicPath, PonderStoryBoard storyBoard,
									PonderTag... tags);

	MultiSceneBuilder addStoryBoard(String schematicPath, PonderStoryBoard storyBoard,
									Consumer<PonderStoryBoardEntry> extras);
}
