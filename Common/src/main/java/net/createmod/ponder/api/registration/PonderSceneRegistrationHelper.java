package net.createmod.ponder.api.registration;

import java.util.function.Function;

import net.createmod.ponder.api.scene.PonderStoryBoard;
import net.createmod.ponder.foundation.PonderStoryBoardEntry;
import net.minecraft.resources.ResourceLocation;

public interface PonderSceneRegistrationHelper<T> {

	<S> PonderSceneRegistrationHelper<S> withKeyFunction(Function<S, T> keyGen);

	PonderStoryBoardEntry addStoryBoard(T component, ResourceLocation schematicLocation, PonderStoryBoard storyBoard,
										ResourceLocation... tags);

	PonderStoryBoardEntry addStoryBoard(T component, String schematicPath, PonderStoryBoard storyBoard,
										ResourceLocation... tags);

	MultiSceneBuilder forComponents(T... components);

	MultiSceneBuilder forComponents(Iterable<T> components);

	ResourceLocation asLocation(String path);
}
