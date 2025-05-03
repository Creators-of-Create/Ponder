package net.createmod.catnip.impl.client.render.model;

import org.jetbrains.annotations.UnknownNullability;

import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;

import net.createmod.catnip.client.render.model.ShadeSeparatedResultConsumer;
import net.minecraft.client.renderer.RenderType;

// Modified from https://github.com/Engine-Room/Flywheel/blob/2f67f54c8898d91a48126c3c753eefa6cd224f84/fabric/src/lib/java/dev/engine_room/flywheel/lib/model/baked/MeshEmitter.java
class MeshEmitter {
	private final RenderType renderType;
	private final BufferBuilder bufferBuilder;

	@UnknownNullability
	private ShadeSeparatedResultConsumer resultConsumer;
	private boolean currentShade;

	MeshEmitter(RenderType renderType) {
		this.renderType = renderType;
		this.bufferBuilder = new BufferBuilder(renderType.bufferSize());
	}

	public void prepare(ShadeSeparatedResultConsumer resultConsumer) {
		this.resultConsumer = resultConsumer;
	}

	public void end() {
		if (bufferBuilder.building()) {
			emit();
		}
		resultConsumer = null;
	}

	public BufferBuilder getBuffer(boolean shade) {
		prepareForGeometry(shade);
		return bufferBuilder;
	}

	private void prepareForGeometry(boolean shade) {
		if (!bufferBuilder.building()) {
			bufferBuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.BLOCK);
		} else if (shade != currentShade) {
			emit();
			bufferBuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.BLOCK);
		}

		currentShade = shade;
	}

	private void emit() {
		var renderedBuffer = bufferBuilder.endOrDiscardIfEmpty();

		if (renderedBuffer != null) {
			resultConsumer.accept(renderType, currentShade, renderedBuffer);
			renderedBuffer.release();
		}
	}
}
