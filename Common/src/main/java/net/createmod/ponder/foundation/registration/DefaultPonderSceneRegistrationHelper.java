package net.createmod.ponder.foundation.registration;

import java.util.Arrays;
import java.util.function.Function;

import net.createmod.ponder.foundation.PonderStoryBoardEntry;
import net.createmod.ponder.foundation.PonderTag;
import net.createmod.ponder.foundation.api.registration.MultiSceneBuilder;
import net.createmod.ponder.foundation.api.registration.PonderSceneRegistrationHelper;
import net.createmod.ponder.foundation.api.scene.PonderStoryBoard;
import net.minecraft.resources.ResourceLocation;

public class DefaultPonderSceneRegistrationHelper implements PonderSceneRegistrationHelper<ResourceLocation> {

	protected String namespace;
	protected PonderSceneRegistry sceneRegistry;

	public DefaultPonderSceneRegistrationHelper(String namespace, PonderSceneRegistry sceneRegistry) {
		this.namespace = namespace;
		this.sceneRegistry = sceneRegistry;
	}

	@Override
	public <T> GenericPonderSceneRegistrationHelper<T> withKeyFunction(Function<T, ResourceLocation> keyGen) {
		return new GenericPonderSceneRegistrationHelper<>(this, keyGen);
	}

	@Override
	public PonderStoryBoardEntry addStoryBoard(ResourceLocation component, ResourceLocation schematicLocation,
											   PonderStoryBoard storyBoard, PonderTag... tags) {
		PonderStoryBoardEntry entry = this.createStoryBoardEntry(storyBoard, schematicLocation, component);
		entry.highlightTags(tags);
		sceneRegistry.addStoryBoard(entry);
		return entry;
	}

	@Override
	public PonderStoryBoardEntry addStoryBoard(ResourceLocation component, String schematicPath,
											   PonderStoryBoard storyBoard, PonderTag... tags) {
		return addStoryBoard(component, asLocation(schematicPath), storyBoard, tags);
	}

	@Override
	public MultiSceneBuilder forComponents(ResourceLocation... components) {
		return new GenericMultiSceneBuilder<>(this, Arrays.asList(components));
	}

	@Override
	public MultiSceneBuilder forComponents(Iterable<ResourceLocation> components) {
		return new GenericMultiSceneBuilder<>(this, components);
	}

	@Override
	public ResourceLocation asLocation(String path) {
		return new ResourceLocation(namespace, path);
	}

	private PonderStoryBoardEntry createStoryBoardEntry(PonderStoryBoard storyBoard, ResourceLocation schematicLocation,
														ResourceLocation component) {
		return new PonderStoryBoardEntry(storyBoard, namespace, schematicLocation, component);
	}

}
