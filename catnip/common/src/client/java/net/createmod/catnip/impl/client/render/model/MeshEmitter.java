package net.createmod.catnip.impl.client.render.model;

import org.jetbrains.annotations.UnknownNullability;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.ByteBufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.blaze3d.PrimitiveTopology;

import net.createmod.catnip.api.client.render.model.ShadeSeparatedResultConsumer;
import net.minecraft.client.renderer.chunk.ChunkSectionLayer;

// Modified from https://github.com/Engine-Room/Flywheel/blob/2f67f54c8898d91a48126c3c753eefa6cd224f84/fabric/src/lib/java/dev/engine_room/flywheel/lib/model/baked/MeshEmitter.java
class MeshEmitter {
	private final RenderPipeline pipeline;
	private final ByteBufferBuilder byteBufferBuilder;
	@UnknownNullability
	private BufferBuilder bufferBuilder;

	@UnknownNullability
	private ShadeSeparatedResultConsumer resultConsumer;
	private boolean currentShade;

	MeshEmitter(ChunkSectionLayer layer) {
		this.pipeline = layer.pipeline();
		this.byteBufferBuilder = new ByteBufferBuilder(layer.bufferSize());
	}

	public void prepare(ShadeSeparatedResultConsumer resultConsumer) {
		this.resultConsumer = resultConsumer;
	}

	public void end() {
		if (bufferBuilder != null) {
			emit();
		}
		resultConsumer = null;
	}

	public BufferBuilder getBuffer(boolean shade) {
		prepareForGeometry(shade);
		return bufferBuilder;
	}

	private void prepareForGeometry(boolean shade) {
		if (bufferBuilder == null) {
			bufferBuilder = new BufferBuilder(byteBufferBuilder, PrimitiveTopology.QUADS, DefaultVertexFormat.BLOCK);
		} else if (shade != currentShade) {
			emit();
			bufferBuilder = new BufferBuilder(byteBufferBuilder, PrimitiveTopology.QUADS, DefaultVertexFormat.BLOCK);
		}

		currentShade = shade;
	}

	private void emit() {
		var data = bufferBuilder.build();
		bufferBuilder = null;

		if (data != null) {
			resultConsumer.accept(pipeline, currentShade, data);
			data.close();
		}
	}
}
