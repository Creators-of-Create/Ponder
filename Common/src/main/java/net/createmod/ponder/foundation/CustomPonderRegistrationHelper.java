package net.createmod.ponder.foundation;

import java.util.function.Function;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import net.createmod.ponder.foundation.PonderStoryBoardEntry.PonderStoryBoard;
import net.minecraft.resources.ResourceLocation;

public class CustomPonderRegistrationHelper<T> extends PonderRegistrationHelper {

	private final Function<T, ResourceLocation> keyGen;

	public CustomPonderRegistrationHelper(String namespace, Function<T, ResourceLocation> keyGen) {
		super(namespace);
		this.keyGen = keyGen;
	}

	public PonderStoryBoardEntry addStoryBoard(T component, ResourceLocation schematicLocation, PonderStoryBoard storyBoard, PonderTag... tags) {
		return addStoryBoard(keyGen.apply(component), schematicLocation, storyBoard, tags);
	}

	public PonderStoryBoardEntry addStoryBoard(T component, String schematicPath, PonderStoryBoard storyBoard, PonderTag... tags) {
		return addStoryBoard(keyGen.apply(component), schematicPath, storyBoard, tags);
	}

	public MultiSceneBuilder forComponents(T... components) {
		return new MultiSceneBuilder(Stream.of(components).map(keyGen).toList());
	}

	public MultiSceneBuilder forComponentsIterable(Iterable<? extends T> components) {
		return new MultiSceneBuilder(StreamSupport.stream(components.spliterator(), false).map(keyGen).toList());
	}

	public TagRegistryTagBuilder addToTag(PonderTag tag) {
		return new TagRegistryTagBuilder(PonderRegistry.TAGS, tag);
	}

	public TagRegistryItemBuilder addTagsToItems(T... items) {
		return new TagRegistryItemBuilder(PonderRegistry.TAGS, items);
	}

	public class TagRegistryTagBuilder extends PonderTagRegistry.TagBuilder {

		protected TagRegistryTagBuilder(PonderTagRegistry registry, PonderTag tag) {
			super(registry, tag);
		}

		public TagRegistryTagBuilder add(T entry) {
			add(keyGen.apply(entry));
			return this;
		}
	}

	public class TagRegistryItemBuilder extends PonderTagRegistry.ItemBuilder {
		protected TagRegistryItemBuilder(PonderTagRegistry registry, T... items) {
			super(registry, Stream.of(items).map(keyGen).toList());
		}
	}
}
