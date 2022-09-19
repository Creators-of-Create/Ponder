package net.createmod.catnip.render;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import javax.annotation.Nullable;

import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.datafixers.util.Pair;
import com.mojang.math.Matrix4f;
import com.mojang.math.Vector4f;

import it.unimi.dsi.fastutil.longs.Long2IntMap;
import it.unimi.dsi.fastutil.longs.Long2IntOpenHashMap;
import net.createmod.catnip.platform.CatnipClientServices;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
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

	// Vertex Texture Coordinates
	@Nullable protected SpriteShiftFunc spriteShiftFunc;

	// Vertex Lighting
	protected boolean shouldLight;
	protected int packedLightCoordinates;
	@Nullable protected Matrix4f lightTransform;

	// Temporary
	private static final Long2IntMap WORLD_LIGHT_CACHE = new Long2IntOpenHashMap();


	public DefaultSuperByteBuffer(BufferBuilder buf) {
		Pair<BufferBuilder.DrawState, ByteBuffer> state = buf.popNextBuffer();
		ByteBuffer rendered = state.getSecond();
		// Vanilla issue, endianness does not carry over into sliced buffers - fixed by forge only
		rendered.order(ByteOrder.nativeOrder());

		formatSize = CatnipClientServices.CLIENT_HOOKS.getFormatFromBufferBuilder(buf).getVertexSize();
		int size = state.getFirst().vertexCount() * formatSize;

		template = ByteBuffer.allocateDirect(size).order(ByteOrder.nativeOrder());
		template.order(rendered.order());
		template.limit(rendered.limit());
		template.put(rendered);
		//template.rewind();

		transforms = new PoseStack();
		transforms.pushPose();
	}

	@Override
	public void renderInto(PoseStack ms, VertexConsumer consumer) {
		if (isEmpty())
			return;
		//buffer.rewind();

		Matrix4f t = ms.last()
				.pose()
				.copy();
		Matrix4f localTransforms = transforms.last()
				.pose();
		t.multiply(localTransforms);

		for (int i = 0; i < vertexCount(); i++) {
			float x = getX(i);
			float y = getY(i);
			float z = getZ(i);

			Vector4f pos = new Vector4f(x, y, z, 1F);
			Vector4f lightPos = new Vector4f(x, y, z, 1F);
			pos.transform(t);
			lightPos.transform(localTransforms);

			consumer.vertex(pos.x(), pos.y(), pos.z());

			byte r = getR(i);
			byte g = getG(i);
			byte b = getB(i);
			byte a = getA(i);

			if (shouldColor) {
				float lum = (r < 0 ? 255 + r : r) / 256f;
				consumer.color((int) (this.r * lum), (int) (this.g * lum), (int) (this.b * lum), this.a);
			} else
				consumer.color(r, g, b, a);

			float u = getU(i);
			float v = getV(i);

			if (spriteShiftFunc != null) {
				spriteShiftFunc.shift(consumer, u, v);
			} else
				consumer.uv(u, v);

			if (shouldLight) {
				int light = packedLightCoordinates;
				if (lightTransform != null) {
					lightPos.transform(lightTransform);
					light = getLight(Minecraft.getInstance().level, lightPos);
				}
				consumer.uv2(light);
			} else
				consumer.uv2(getLight(i));

			consumer.normal(getNX(i), getNY(i), getNZ(i))
					.endVertex();
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
		spriteShiftFunc = null;
		shouldLight = false;
		lightTransform = null;
		packedLightCoordinates = 0;

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
	public DefaultSuperByteBuffer translate(float x, float y, float z) {
		transforms.translate(x, y, z);
		return this;
	}

	@Override
	public DefaultSuperByteBuffer rotate(Direction axis, float radians) {
		if (radians == 0)
			return this;

		transforms.mulPose(axis.step().rotation(radians));

		return this;
	}

	@Override
	public DefaultSuperByteBuffer scale(float factorX, float factorY, float factorZ) {
		transforms.scale(factorX, factorY, factorZ);

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
	public DefaultSuperByteBuffer light(int packedLight) {
		shouldLight = true;
		packedLightCoordinates = packedLight;
		return this;
	}

	@Override
	public DefaultSuperByteBuffer light(Matrix4f lightTransform) {
		shouldLight = true;
		this.lightTransform = lightTransform;
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
