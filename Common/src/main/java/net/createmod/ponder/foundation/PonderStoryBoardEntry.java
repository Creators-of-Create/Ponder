package net.createmod.ponder.foundation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import net.createmod.ponder.api.scene.PonderStoryBoard;
import net.minecraft.resources.ResourceLocation;

public class PonderStoryBoardEntry {

	private final PonderStoryBoard board;
	private final String namespace;
	private final ResourceLocation schematicLocation;
	private final ResourceLocation component;
	private final List<ResourceLocation> tags;
	private final List<SceneOrderingEntry> orderingEntries;

	public PonderStoryBoardEntry(PonderStoryBoard board, String namespace, ResourceLocation schematicLocation, ResourceLocation component) {
		this.board = board;
		this.namespace = namespace;
		this.schematicLocation = schematicLocation;
		this.component = component;
		this.tags = new ArrayList<>();
		this.orderingEntries = new ArrayList<>();
	}

	public PonderStoryBoardEntry(PonderStoryBoard board, String namespace, String schematicPath, ResourceLocation component) {
		this(board, namespace, new ResourceLocation(namespace, schematicPath), component);
	}

	public PonderStoryBoard getBoard() {
		return board;
	}

	public String getNamespace() {
		return namespace;
	}

	public ResourceLocation getSchematicLocation() {
		return schematicLocation;
	}

	public ResourceLocation getComponent() {
		return component;
	}

	public List<ResourceLocation> getTags() {
		return tags;
	}

	public List<SceneOrderingEntry> getOrderingEntries() {
		return orderingEntries;
	}

	// Builder start

	public PonderStoryBoardEntry orderBefore(String otherSceneId) {
		return orderBefore(namespace, otherSceneId);
	}

	public PonderStoryBoardEntry orderBefore(String namespace, String otherSceneId) {
		this.orderingEntries.add(SceneOrderingEntry.before(namespace, otherSceneId));
		return this;
	}

	public PonderStoryBoardEntry orderAfter(String otherSceneId) {
		return orderAfter(namespace, otherSceneId);
	}

	public PonderStoryBoardEntry orderAfter(String namespace, String otherSceneId) {
		this.orderingEntries.add(SceneOrderingEntry.after(namespace, otherSceneId));
		return this;
	}

	public PonderStoryBoardEntry highlightTag(ResourceLocation tag) {
		tags.add(tag);
		return this;
	}

	public PonderStoryBoardEntry highlightTags(ResourceLocation... tags) {
		Collections.addAll(this.tags, tags);
		return this;
	}

	public PonderStoryBoardEntry highlightAllTags() {
		tags.add(PonderTag.Highlight.ALL);
		return this;
	}

	public record SceneOrderingEntry(SceneOrderingType type, ResourceLocation sceneId) {

		public static SceneOrderingEntry after(String namespace, String sceneId) {
			return new SceneOrderingEntry(SceneOrderingType.AFTER, new ResourceLocation(namespace, sceneId));
		}

		public static SceneOrderingEntry before(String namespace, String sceneId) {
			return new SceneOrderingEntry(SceneOrderingType.BEFORE, new ResourceLocation(namespace, sceneId));
		}
	}

	public enum SceneOrderingType {
		BEFORE, AFTER
	}

}
