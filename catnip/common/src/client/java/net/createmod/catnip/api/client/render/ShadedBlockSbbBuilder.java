package net.createmod.catnip.api.client.render;

import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.ByteBufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.MeshData;
import com.mojang.blaze3d.vertex.PoseStack.Pose;
import com.mojang.blaze3d.vertex.QuadInstance;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.PrimitiveTopology;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import net.minecraft.client.resources.model.geometry.BakedQuad;

@Deprecated(forRemoval = true)
public class ShadedBlockSbbBuilder implements VertexConsumer {
	protected static final ByteBufferBuilder BYTE_BUFFER_BUILDER = new ByteBufferBuilder(512);
	protected BufferBuilder bufferBuilder;
	protected final IntList shadeSwapVertices = new IntArrayList();
	protected boolean currentShade;
	protected boolean invertFakeNormal;

	public static ShadedBlockSbbBuilder create() {
		return new ShadedBlockSbbBuilder();
	}

	public static ShadedBlockSbbBuilder createForPonder() {
		ShadedBlockSbbBuilder builder = new ShadedBlockSbbBuilder();
		builder.invertFakeNormal = true;
		return builder;
	}

	public void begin() {
		bufferBuilder = new BufferBuilder(BYTE_BUFFER_BUILDER, PrimitiveTopology.QUADS, DefaultVertexFormat.BLOCK);
		shadeSwapVertices.clear();
		currentShade = true;
	}

	public SuperByteBuffer end() {
		MeshData data = bufferBuilder.build();
		TemplateMesh mesh;

		if (data != null) {
			mesh = new MutableTemplateMesh(data).toImmutable();
			data.close();
		} else {
			mesh = new TemplateMesh(0);
		}

		return new ShadeSeparatingSuperByteBuffer(mesh, shadeSwapVertices.toIntArray(), invertFakeNormal);
	}

	public BufferBuilder unwrap(boolean shade) {
		prepareForGeometry(shade);
		return bufferBuilder;
	}

	protected void prepareForGeometry(boolean shade) {
		if (shade != currentShade) {
			shadeSwapVertices.add(0);
			currentShade = shade;
		}
	}

	protected void prepareForGeometry(BakedQuad quad) {
		prepareForGeometry(quad.materialInfo().shade());
	}

	@Override
	public void putBakedQuad(Pose pose, BakedQuad quad, QuadInstance instance) {
		this.prepareForGeometry(quad);
		this.bufferBuilder.putBakedQuad(pose, quad, instance);
	}

	@Override
	public void putBlockBakedQuad(float x, float y, float z, BakedQuad quad, QuadInstance instance) {
		this.prepareForGeometry(quad);
		this.bufferBuilder.putBlockBakedQuad(x, y, z, quad, instance);
	}

	@Override
	public VertexConsumer addVertex(float x, float y, float z) {
		throw new UnsupportedOperationException("ShadedBlockSbbBuilder only supports putBulkData!");
	}

	@Override
	public VertexConsumer setColor(int red, int green, int blue, int alpha) {
		throw new UnsupportedOperationException("ShadedBlockSbbBuilder only supports putBulkData!");
	}

	@Override
	public VertexConsumer setColor(int color) {
		throw new UnsupportedOperationException("ShadedBlockSbbBuilder only supports putBulkData!");
	}

	@Override
	public VertexConsumer setUv(float u, float v) {
		throw new UnsupportedOperationException("ShadedBlockSbbBuilder only supports putBulkData!");
	}

	@Override
	public VertexConsumer setUv1(int u, int v) {
		throw new UnsupportedOperationException("ShadedBlockSbbBuilder only supports putBulkData!");
	}

	@Override
	public VertexConsumer setUv2(int u, int v) {
		throw new UnsupportedOperationException("ShadedBlockSbbBuilder only supports putBulkData!");
	}

	@Override
	public VertexConsumer setNormal(float x, float y, float z) {
		throw new UnsupportedOperationException("ShadedBlockSbbBuilder only supports putBulkData!");
	}

	@Override
	public VertexConsumer setLineWidth(float p_456188_) {
		throw new UnsupportedOperationException("ShadedBlockSbbBuilder only supports putBulkData!");
	}
}
