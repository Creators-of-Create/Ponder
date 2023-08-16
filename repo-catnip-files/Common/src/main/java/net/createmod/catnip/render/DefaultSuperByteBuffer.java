package net.createmod.catnip.render;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import javax.annotation.Nullable;

import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferBuilder.RenderedBuffer;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Matrix3f;
import com.mojang.math.Matrix4f;
import com.mojang.math.Quaternion;
import com.mojang.math.Vector3f;
import com.mojang.math.Vector4f;

import it.unimi.dsi.fastutil.longs.Long2IntMap;
import it.unimi.dsi.fastutil.longs.Long2IntOpenHashMap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

@SuppressWarnings("unchecked")
public class DefaultSuperByteBuffer implements SuperByteBuffer {

	protected ByteBuffer template;
	protected int formatSize;

	// Vertex Position
	protected PoseStack transforms;

	// Vertex Coloring
	protected boolean shouldColor;
	protected int r, g, b, a;
	protected boolean disableDiffuse;

	// Vertex Texture Coordinates
	@Nullable protected SpriteShiftFunc spriteShiftFunc;

	// Vertex Overlay Color
	protected boolean hasOverlay;
	protected int overlay = OverlayTexture.NO_OVERLAY;

	// Vertex Lighting
	protected boolean useWorldLight;
	@Nullable protected Matrix4f lightTransform;
	protected boolean hasCustomLight;
	protected int packedLightCoordinates;
	protected boolean hybridLight;

	// Vertex Normals
	protected boolean fullNormalTransform;

	// Temporary
	protected static final Long2IntMap WORLD_LIGHT_CACHE = new Long2IntOpenHashMap();


	public DefaultSuperByteBuffer(RenderedBuffer renderedBuffer) {
		ByteBuffer rendered = renderedBuffer.vertexBuffer();
		BufferBuilder.DrawState drawState = renderedBuffer.drawState();

		// Vanilla issue, endianness does not carry over into sliced buffers - fixed by forge only
		rendered.order(ByteOrder.nativeOrder());

		drawState.format().getVertexSize();
		formatSize = drawState.format().getVertexSize();
		int size = drawState.vertexCount() * formatSize;

		template = ByteBuffer.allocateDirect(size).order(ByteOrder.nativeOrder());
		template.order(rendered.order());
		template.limit(rendered.limit());
		template.put(rendered);

		transforms = new PoseStack();
		transforms.pushPose();
	}

	@Override
	public void renderInto(PoseStack ms, VertexConsumer consumer) {
		if (isEmpty())
			return;

		Matrix4f modelMatrix = ms.last().pose().copy();
		Matrix4f localTransforms = transforms.last().pose();
		modelMatrix.multiply(localTransforms);

		Matrix3f normalMatrix;
		if (fullNormalTransform) {
			normalMatrix = ms.last().normal().copy();
			normalMatrix.mul(transforms.last().normal());
		} else {
			normalMatrix = transforms.last().normal().copy();
		}

		for (int i = 0; i < vertexCount(); i++) {
			float x = getX(i);
			float y = getY(i);
			float z = getZ(i);

			float normalX = getNX(i);
			float normalY = getNY(i);
			float normalZ = getNZ(i);

			Vector4f pos = new Vector4f(x, y, z, 1F);
			Vector3f normal = new Vector3f(normalX, normalY, normalZ);
			Vector4f lightPos = new Vector4f(x, y, z, 1F);
			pos.transform(modelMatrix);
			normal.transform(normalMatrix);
			lightPos.transform(localTransforms);

			consumer.vertex(pos.x(), pos.y(), pos.z());

			byte r, g, b, a;
			if (shouldColor) {
				r = (byte) this.r;
				g = (byte) this.g;
				b = (byte) this.b;
				a = (byte) this.a;
			} else {
				r = getR(i);
				g = getG(i);
				b = getB(i);
				a = getA(i);
			}
			if (disableDiffuse) {
				consumer.color(r, g, b, a);
			} else {
				// missing flywheel's diffuse calc stuff
				consumer.color(r, g, b, a);
			}
			float u = getU(i);
			float v = getV(i);

			if (spriteShiftFunc != null) {
				spriteShiftFunc.shift(consumer, u, v);
			} else
				consumer.uv(u, v);

			int light;
			if (useWorldLight) {
				lightPos.set(((x - .5f) * 15 / 16f) + .5f, (y - .5f) * 15 / 16f + .5f, (z - .5f) * 15 / 16f + .5f, 1f);
				lightPos.transform(localTransforms);
				if (lightTransform != null) {
					lightPos.transform(lightTransform);
				}

				light = getLight(Minecraft.getInstance().level, lightPos);
				if (hasCustomLight) {
					light = SuperByteBuffer.maxLight(light, packedLightCoordinates);
				}
			} else if (hasCustomLight) {
				light = packedLightCoordinates;
			} else {
				light = getLight(i);
			}

			if (hybridLight) {
				consumer.uv2(SuperByteBuffer.maxLight(light, getLight(i)));
			} else {
				consumer.uv2(light);
			}

			consumer.normal(normal.x(), normal.y(), normal.z());

			consumer.endVertex();
		}

		reset();

	}

	@Override
	public DefaultSuperByteBuffer reset() {
		while (!transforms.clear())
			transforms.popPose();

		transforms.pushPose();

		shouldColor = false;
		r = 0;
		g = 0;
		b = 0;
		a = 0;
		disableDiffuse = false;
		spriteShiftFunc = null;
		hasOverlay = false;
		overlay = OverlayTexture.NO_OVERLAY;
		useWorldLight = false;
		lightTransform = null;
		hasCustomLight = false;
		packedLightCoordinates = 0;
		hybridLight = false;
		fullNormalTransform = false;

		WORLD_LIGHT_CACHE.clear();

		return this;
	}

	@Override
	public boolean isEmpty() {
		return template.limit() == 0;
	}

	@Override
	public PoseStack getTransforms() {
		return transforms;
	}

	@Override
	public SuperByteBuffer translate(double x, double y, double z) {
		transforms.translate(x, y, z);
		return this;
	}

	@Override
	public SuperByteBuffer multiply(Quaternion quaternion) {
		transforms.mulPose(quaternion);
		return this;
	}

	@Override
	public DefaultSuperByteBuffer scale(float factorX, float factorY, float factorZ) {
		transforms.scale(factorX, factorY, factorZ);

		return this;
	}

	@Override
	public DefaultSuperByteBuffer pushPose() {
		transforms.pushPose();
		return this;
	}

	@Override
	public DefaultSuperByteBuffer popPose() {
		transforms.popPose();
		return this;
	}

	@Override
	public DefaultSuperByteBuffer mulPose(Matrix4f pose) {
		transforms.last().pose().multiply(pose);
		return this;
	}

	@Override
	public DefaultSuperByteBuffer mulNormal(Matrix3f normal) {
		transforms.last().normal().mul(normal);
		return this;
	}

	@Override
	public DefaultSuperByteBuffer transform(PoseStack ms) {
		transforms.last()
				.pose()
				.multiply(ms.last().pose());
		transforms.last()
				.normal()
				.mul(ms.last().normal());
		return this;
	}

	@Override
	public DefaultSuperByteBuffer color(int color) {
		shouldColor = true;
		r = ((color >> 16) & 0xFF);
		g = ((color >> 8) & 0xFF);
		b = (color & 0xFF);
		a = 255;
		return this;
	}

	@Override
	public DefaultSuperByteBuffer color(int r, int g, int b, int a) {
		shouldColor = true;
		this.r = r;
		this.g = g;
		this.b = b;
		this.a = a;
		return this;
	}

	@Override
	public DefaultSuperByteBuffer disableDiffuse() {
		disableDiffuse = true;
		return this;
	}

	@Override
	public DefaultSuperByteBuffer shiftUV(SpriteShiftEntry entry) {
		this.spriteShiftFunc = (builder, u, v) -> builder.uv(entry.getTargetU(u), entry.getTargetV(v));
		return this;
	}

	@Override
	public DefaultSuperByteBuffer shiftUVScrolling(SpriteShiftEntry entry, float scrollU, float scrollV) {
		this.spriteShiftFunc = (builder, u, v) -> {
			float targetU = u - entry.getOriginal()
					.getU0() + entry.getTarget()
					.getU0()
					+ scrollU;
			float targetV = v - entry.getOriginal()
					.getV0() + entry.getTarget()
					.getV0()
					+ scrollV;
			builder.uv(targetU, targetV);
		};
		return this;
	}

	@Override
	public DefaultSuperByteBuffer shiftUVtoSheet(SpriteShiftEntry entry, float uTarget, float vTarget, int sheetSize) {
		this.spriteShiftFunc = (builder, u, v) -> {
			float targetU = entry.getTarget()
					.getU((SpriteShiftEntry.getUnInterpolatedU(entry.getOriginal(), u) / sheetSize) + uTarget * 16);
			float targetV = entry.getTarget()
					.getV((SpriteShiftEntry.getUnInterpolatedV(entry.getOriginal(), v) / sheetSize) + vTarget * 16);
			builder.uv(targetU, targetV);
		};
		return this;
	}

	@Override
	public DefaultSuperByteBuffer overlay() {
		hasOverlay = true;
		return this;
	}

	@Override
	public DefaultSuperByteBuffer overlay(int overlay) {
		hasOverlay = true;
		this.overlay = overlay;
		return this;
	}

	@Override
	public DefaultSuperByteBuffer light() {
		useWorldLight = true;
		return this;
	}

	@Override
	public DefaultSuperByteBuffer light(Matrix4f lightTransform) {
		useWorldLight = true;
		this.lightTransform = lightTransform;
		return this;
	}

	@Override
	public DefaultSuperByteBuffer light(int packedLight) {
		hasCustomLight = true;
		this.packedLightCoordinates = packedLight;
		return this;
	}

	@Override
	public DefaultSuperByteBuffer hybridLight() {
		hybridLight = true;
		return this;
	}

	@Override
	public DefaultSuperByteBuffer fullNormalTransform() {
		fullNormalTransform = true;
		return this;
	}

	//

	protected int vertexCount() {
		return template.limit() / formatSize;
	}

	protected int getBufferPosition(int vertexIndex) {
		return vertexIndex * formatSize;
	}

	protected float getX(int index) {
		return template.getFloat(getBufferPosition(index));
	}

	protected float getY(int index) {
		return template.getFloat(getBufferPosition(index) + 4);
	}

	protected float getZ(int index) {
		return template.getFloat(getBufferPosition(index) + 8);
	}

	protected byte getR(int index) {
		return template.get(getBufferPosition(index) + 12);
	}

	protected byte getG(int index) {
		return template.get(getBufferPosition(index) + 13);
	}

	protected byte getB(int index) {
		return template.get(getBufferPosition(index) + 14);
	}

	protected byte getA(int index) {
		return template.get(getBufferPosition(index) + 15);
	}

	protected float getU(int index) {
		return template.getFloat(getBufferPosition(index) + 16);
	}

	protected float getV(int index) {
		return template.getFloat(getBufferPosition(index) + 20);
	}

	protected int getLight(int index) {
		return template.getInt(getBufferPosition(index) + 24);
	}

	protected byte getNX(int index) {
		return template.get(getBufferPosition(index) + 28);
	}

	protected byte getNY(int index) {
		return template.get(getBufferPosition(index) + 29);
	}

	protected byte getNZ(int index) {
		return template.get(getBufferPosition(index) + 30);
	}

	private static int getLight(Level world, Vector4f lightPos) {
		BlockPos pos = new BlockPos(lightPos.x(), lightPos.y(), lightPos.z());
		return WORLD_LIGHT_CACHE.computeIfAbsent(pos.asLong(), $ -> LevelRenderer.getLightColor(world, pos));
	}
}
