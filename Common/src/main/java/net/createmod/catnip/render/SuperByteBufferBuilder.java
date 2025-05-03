package net.createmod.catnip.render;

import com.mojang.blaze3d.vertex.BufferBuilder;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;

public class SuperByteBufferBuilder {
	protected final MutableTemplateMesh mesh = new MutableTemplateMesh();
	protected final IntList shadeSwapVertices = new IntArrayList();

	protected boolean currentShade;

	public void prepare() {
		mesh.clear();
		shadeSwapVertices.clear();
		currentShade = true;
	}

	public void add(BufferBuilder.RenderedBuffer data, boolean shaded) {
		if (shaded != currentShade) {
			shadeSwapVertices.add(mesh.vertexCount());
			currentShade = shaded;
		}

		mesh.copyFrom(mesh.vertexCount(), data);
	}

	public SuperByteBuffer build() {
		return new ShadeSeparatingSuperByteBuffer(mesh.toImmutable(), shadeSwapVertices.toIntArray());
	}
}
