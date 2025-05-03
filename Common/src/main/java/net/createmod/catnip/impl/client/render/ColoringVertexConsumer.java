package net.createmod.catnip.impl.client.render;

import com.mojang.blaze3d.vertex.VertexConsumer;

public record ColoringVertexConsumer(VertexConsumer delegate, float red, float green, float blue, float alpha) implements VertexConsumer {
	@Override
	public VertexConsumer vertex(double x, double y, double z) {
		delegate.vertex(x, y, z);
		return this;
	}

	@Override
	public VertexConsumer color(int r, int g, int b, int a) {
		delegate.color((int) (r * red), (int) (g * green), (int) (b * blue), (int) (a * alpha));
		return this;
	}

	@Override
	public VertexConsumer uv(float u, float v) {
		delegate.uv(u, v);
		return this;
	}

	@Override
	public VertexConsumer overlayCoords(int u, int v) {
		delegate.overlayCoords(u, v);
		return this;
	}

	@Override
	public VertexConsumer uv2(int u, int v) {
		delegate.uv2(u, v);
		return this;
	}

	@Override
	public VertexConsumer normal(float x, float y, float z) {
		delegate.normal(x, y, z);
		return this;
	}

	@Override
	public void endVertex() {
		delegate.endVertex();
	}

	@Override
	public void defaultColor(int r, int g, int b, int a) {
		delegate.defaultColor((int) (r * red), (int) (g * green), (int) (b * blue), (int) (a * alpha));
	}

	@Override
	public void unsetDefaultColor() {
		delegate.unsetDefaultColor();
	}
}
