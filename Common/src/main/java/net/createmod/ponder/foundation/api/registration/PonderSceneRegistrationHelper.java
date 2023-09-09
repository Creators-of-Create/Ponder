package net.createmod.ponder.foundation.api.registration;

import java.util.function.Function;

import net.createmod.ponder.foundation.PonderStoryBoardEntry;
import net.createmod.ponder.foundation.PonderTag;
import net.createmod.ponder.foundation.api.scene.PonderStoryBoard;
import net.minecraft.resources.ResourceLocation;

public interface PonderSceneRegistrationHelper<T> {

	<S> PonderSceneRegistrationHelper<S> withKeyFunction(Function<S, T> keyGen);

	PonderStoryBoardEntry addStoryBoard(T component, ResourceLocation schematicLocation, PonderStoryBoard storyBoard,
										PonderTag... tags);

	PonderStoryBoardEntry addStoryBoard(T component, String schematicPath, PonderStoryBoard storyBoard,
										PonderTag... tags);

	MultiSceneBuilder forComponents(T... components);

	MultiSceneBuilder forComponents(Iterable<T> components);

	ResourceLocation asLocation(String path);
}
