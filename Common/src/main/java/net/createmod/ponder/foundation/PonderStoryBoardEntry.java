package net.createmod.ponder.foundation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import net.createmod.ponder.foundation.api.scene.PonderStoryBoard;
import net.minecraft.resources.ResourceLocation;

public class PonderStoryBoardEntry {

	private final PonderStoryBoard board;
	private final String namespace;
	private final ResourceLocation schematicLocation;
	private final ResourceLocation component;
	private final List<PonderTag> tags;

	public PonderStoryBoardEntry(PonderStoryBoard board, String namespace, ResourceLocation schematicLocation, ResourceLocation component) {
		this.board = board;
		this.namespace = namespace;
		this.schematicLocation = schematicLocation;
		this.component = component;
		this.tags = new ArrayList<>();
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

	public List<PonderTag> getTags() {
		return tags;
	}

	// Builder start

	public PonderStoryBoardEntry highlightTag(PonderTag tag) {
		tags.add(tag);
		return this;
	}

	public PonderStoryBoardEntry highlightTags(PonderTag... tags) {
		Collections.addAll(this.tags, tags);
		return this;
	}

	public PonderStoryBoardEntry highlightAllTags() {
		tags.add(PonderTag.Highlight.ALL);
		return this;
	}

}
