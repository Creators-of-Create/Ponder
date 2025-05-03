package net.createmod.catnip.render;

import java.nio.ByteBuffer;

import com.mojang.blaze3d.vertex.BufferBuilder;

import net.minecraft.client.renderer.texture.OverlayTexture;

public class MutableTemplateMesh extends TemplateMesh {
	public MutableTemplateMesh() {
		this(0);
	}

	public MutableTemplateMesh(int vertexCount) {
		super(vertexCount);
	}

	public MutableTemplateMesh(TemplateMesh template) {
		super(0);
		copyFrom(0, template);
	}

	public MutableTemplateMesh(BufferBuilder.RenderedBuffer data) {
		super(0);
		copyFrom(0, data);
	}

	@Deprecated(forRemoval = true)
	public MutableTemplateMesh(int[] data) {
		super(data);
	}

	@Deprecated(forRemoval = true)
	public static void transferFromVertexData(int srcIndex, int dstIndex, int vertexCount, MutableTemplateMesh mutableMesh, ByteBuffer vertexBuffer, int stride) {
		mutableMesh.copyFrom(srcIndex, dstIndex, vertexCount, vertexBuffer, stride);
	}

	public void ensureCapacity(int vertexCount) {
		if (vertexCount > data.length / INT_STRIDE) {
			int[] newData = new int[vertexCount * INT_STRIDE];
			System.arraycopy(data, 0, newData, 0, data.length);
			data = newData;
		}
	}

	public void copyFrom(int index, TemplateMesh template) {
		if (index < 0 || index > vertexCount) {
			throw new IllegalArgumentException();
		}

		ensureCapacity(index + template.vertexCount);
		vertexCount = index + template.vertexCount;
		System.arraycopy(template.data, 0, data, index * INT_STRIDE, template.vertexCount * INT_STRIDE);
	}

	public void copyFrom(int srcIndex, int dstIndex, int vertexCount, ByteBuffer vertexBuffer, int stride) {
		if (dstIndex < 0 || dstIndex > this.vertexCount) {
			throw new IllegalArgumentException();
		}

		ensureCapacity(dstIndex + vertexCount);
		this.vertexCount = dstIndex + vertexCount;

		for (int i = 0; i < vertexCount; i++) {
			x(dstIndex + i, vertexBuffer.getFloat(srcIndex + i * stride));
			y(dstIndex + i, vertexBuffer.getFloat(srcIndex + i * stride + 4));
			z(dstIndex + i, vertexBuffer.getFloat(srcIndex + i * stride + 8));
			color(dstIndex + i, vertexBuffer.getInt(srcIndex + i * stride + 12));
			u(dstIndex + i, vertexBuffer.getFloat(srcIndex + i * stride + 16));
			v(dstIndex + i, vertexBuffer.getFloat(srcIndex + i * stride + 20));
			overlay(dstIndex + i, OverlayTexture.NO_OVERLAY);
			light(dstIndex + i, vertexBuffer.getInt(srcIndex + i * stride + 24));
			normal(dstIndex + i, vertexBuffer.getInt(srcIndex + i * stride + 28));
		}
	}

	public void copyFrom(int index, BufferBuilder.RenderedBuffer data) {
		int vertexCount = data.drawState().vertexCount();
		ByteBuffer vertexBuffer = data.vertexBuffer();
		int stride = data.drawState().format().getVertexSize();

		copyFrom(0, index, vertexCount, vertexBuffer, stride);
	}

	public void x(int index, float x) {
		data[index * INT_STRIDE + X_OFFSET] = Float.floatToRawIntBits(x);
	}

	public void y(int index, float y) {
		data[index * INT_STRIDE + Y_OFFSET] = Float.floatToRawIntBits(y);
	}

	public void z(int index, float z) {
		data[index * INT_STRIDE + Z_OFFSET] = Float.floatToRawIntBits(z);
	}

	public void color(int index, int color) {
		data[index * INT_STRIDE + COLOR_OFFSET] = color;
	}

	public void u(int index, float u) {
		data[index * INT_STRIDE + U_OFFSET] = Float.floatToRawIntBits(u);
	}

	public void v(int index, float v) {
		data[index * INT_STRIDE + V_OFFSET] = Float.floatToRawIntBits(v);
	}

	public void overlay(int index, int overlay) {
		data[index * INT_STRIDE + OVERLAY_OFFSET] = overlay;
	}

	public void light(int index, int light) {
		data[index * INT_STRIDE + LIGHT_OFFSET] = light;
	}

	public void normal(int index, int normal) {
		data[index * INT_STRIDE + NORMAL_OFFSET] = normal;
	}

	public TemplateMesh toImmutable() {
		int[] newData = new int[vertexCount * INT_STRIDE];
		System.arraycopy(data, 0, newData, 0, newData.length);
		return new TemplateMesh(newData);
	}

	public void clear() {
		vertexCount = 0;
	}
}
