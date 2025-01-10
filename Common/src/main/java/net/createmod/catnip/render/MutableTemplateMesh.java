package net.createmod.catnip.render;

import java.nio.ByteBuffer;

import com.mojang.blaze3d.vertex.BufferBuilder;

import net.minecraft.client.renderer.texture.OverlayTexture;

public class MutableTemplateMesh extends TemplateMesh {
	public MutableTemplateMesh(int[] data) {
		super(data);
	}

	public MutableTemplateMesh(int vertexCount) {
		super(vertexCount);
	}

	public void copyFrom(int index, TemplateMesh template) {
		System.arraycopy(template.data, 0, data, index * INT_STRIDE, template.data.length);
	}

	public MutableTemplateMesh(BufferBuilder.RenderedBuffer data) {
		this(data.drawState().vertexCount());
		int vertexCount = data.drawState().vertexCount();
		ByteBuffer vertexBuffer = data.vertexBuffer();
		int stride = data.drawState().format().getVertexSize();

		transferFromVertexData(0, 0, vertexCount, this, vertexBuffer, stride);
	}

	public static void transferFromVertexData(int srcIndex, int dstIndex, int vertexCount, MutableTemplateMesh mutableMesh, ByteBuffer vertexBuffer, int stride) {
		for (int i = 0; i < vertexCount; i++) {
			mutableMesh.x(dstIndex + i, vertexBuffer.getFloat(srcIndex + i * stride));
			mutableMesh.y(dstIndex + i, vertexBuffer.getFloat(srcIndex + i * stride + 4));
			mutableMesh.z(dstIndex + i, vertexBuffer.getFloat(srcIndex + i * stride + 8));
			mutableMesh.color(dstIndex + i, vertexBuffer.getInt(srcIndex + i * stride + 12));
			mutableMesh.u(dstIndex + i, vertexBuffer.getFloat(srcIndex + i * stride + 16));
			mutableMesh.v(dstIndex + i, vertexBuffer.getFloat(srcIndex + i * stride + 20));
			mutableMesh.overlay(dstIndex + i, OverlayTexture.NO_OVERLAY);
			mutableMesh.light(dstIndex + i, vertexBuffer.getInt(srcIndex + i * stride + 24));
			mutableMesh.normal(dstIndex + i, vertexBuffer.getInt(srcIndex + i * stride + 28));
		}
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
		return new TemplateMesh(data);
	}
}
