package net.createmod.ponder.foundation.registration;

import java.util.Arrays;
import java.util.function.Function;

import net.createmod.ponder.api.registration.MultiSceneBuilder;
import net.createmod.ponder.api.registration.PonderSceneRegistrationHelper;
import net.createmod.ponder.api.registration.StoryBoardEntry;
import net.createmod.ponder.api.scene.PonderStoryBoard;
import net.minecraft.resources.ResourceLocation;

public class GenericPonderSceneRegistrationHelper<T> implements PonderSceneRegistrationHelper<T> {

	private final PonderSceneRegistrationHelper<ResourceLocation> helperDelegate;
	private final Function<T, ResourceLocation> keyGen;

	public GenericPonderSceneRegistrationHelper(PonderSceneRegistrationHelper<ResourceLocation> helperDelegate,
												Function<T, ResourceLocation> keyGen) {
		this.helperDelegate = helperDelegate;
		this.keyGen = keyGen;
	}

	@Override
	public <S> PonderSceneRegistrationHelper<S> withKeyFunction(Function<S, T> keyGen) {
		return new GenericPonderSceneRegistrationHelper<>(helperDelegate, keyGen.andThen(this.keyGen));
	}

	public StoryBoardEntry addStoryBoard(T component, ResourceLocation schematicLocation,
                                         PonderStoryBoard storyBoard, ResourceLocation... tags) {
		return helperDelegate.addStoryBoard(keyGen.apply(component), schematicLocation, storyBoard, tags);
	}

	public StoryBoardEntry addStoryBoard(T component, String schematicPath, PonderStoryBoard storyBoard,
                                         ResourceLocation... tags) {
		return helperDelegate.addStoryBoard(keyGen.apply(component), schematicPath, storyBoard, tags);
	}

	@Override
	public MultiSceneBuilder forComponents(Iterable<? extends T> components) {
		return new GenericMultiSceneBuilder<>(this, components);
	}

	@Override
	@SafeVarargs
	public final MultiSceneBuilder forComponents(T... components) {
		return new GenericMultiSceneBuilder<>(this, Arrays.asList(components));
	}

	@Override
	public ResourceLocation asLocation(String path) {
		return helperDelegate.asLocation(path);
	}
}
