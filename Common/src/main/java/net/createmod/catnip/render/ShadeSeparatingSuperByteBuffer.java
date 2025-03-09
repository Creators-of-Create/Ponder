package net.createmod.catnip.render;

import javax.annotation.ParametersAreNonnullByDefault;

import com.jcraft.jogg.Buffer;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.VertexFormat;

import net.caffeinemc.mods.sodium.api.util.ColorARGB;
import net.caffeinemc.mods.sodium.api.util.ColorMixer;
import net.caffeinemc.mods.sodium.api.util.NormI8;
import net.caffeinemc.mods.sodium.api.vertex.buffer.VertexBufferWriter;
import net.createmod.catnip.theme.Color;
import net.createmod.ponder.mixin.client.accessor.RenderSystemAccessor;

import net.irisshaders.iris.Iris;
import net.irisshaders.iris.vertices.IrisVertexFormats;
import net.irisshaders.iris.vertices.NormalHelper;

import org.jetbrains.annotations.Nullable;
import org.joml.Matrix3f;
import org.joml.Matrix3fc;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;
import org.joml.Quaternionfc;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.joml.Vector3fc;
import org.joml.Vector4f;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import dev.engine_room.flywheel.lib.util.ShadersModHelper;
import it.unimi.dsi.fastutil.longs.Long2IntMap;
import it.unimi.dsi.fastutil.longs.Long2IntOpenHashMap;
import net.createmod.catnip.theme.Color;
import net.createmod.ponder.mixin.client.accessor.RenderSystemAccessor;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockAndTintGetter;

import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;

@SuppressWarnings("unchecked")
@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class ShadeSeparatingSuperByteBuffer implements SuperByteBuffer {
	private static final Long2IntMap WORLD_LIGHT_CACHE = new Long2IntOpenHashMap();

	private final TemplateMesh template;
	private final int[] shadeSwapVertices;

	// Vertex Position and Normals
	private final PoseStack transforms = new PoseStack();

	// Vertex Coloring
	private int vertex_color;
	private boolean disableDiffuse;

	// Vertex Texture Coords
	@Nullable
	private SpriteShiftFunc spriteShiftFunc;

	// Vertex Overlay
	private boolean hasCustomOverlay;
	private int overlay;

	// Vertex Light
	private boolean hasCustomLight;
	private int packedLight;
	private boolean useLevelLight;
	@Nullable
	private BlockAndTintGetter levelWithLight;
	@Nullable
	private Matrix4f lightTransform;
	private boolean invertFakeDiffuseNormal;

	// Reused objects
	private final Matrix4f modelMat = new Matrix4f();
	private final Matrix3f normalMat = new Matrix3f();
	private final Vector4f pos = new Vector4f();
	private final Vector3f normal = new Vector3f();
	private final Vector3f lightDir0 = new Vector3f();
	private final Vector3f lightDir1 = new Vector3f();
	private final ShiftOutput shiftOutput = new ShiftOutput();
	private final Vector4f lightPos = new Vector4f();
	private static final int BUFFER_VERTEX_COUNT = 48;
	private static final MemoryStack STACK = MemoryStack.create();
	private static final long SCRATCH_BUFFER = MemoryUtil.nmemAlignedAlloc(64, BUFFER_VERTEX_COUNT * TerrainVertex.STRIDE);
	private static long BUFFER_PTR = SCRATCH_BUFFER;
	private static int BUFFED_VERTEX = 0;

	// Iris Buff
	private final Vector3f[] pos4 = new Vector3f[]{new Vector3f(), new Vector3f(), new Vector3f(), new Vector3f()};
	private final Vector2f[] uv4 = new Vector2f[]{new Vector2f(), new Vector2f(), new Vector2f(), new Vector2f()};

	public ShadeSeparatingSuperByteBuffer(TemplateMesh template, int[] shadeSwapVertices, boolean invertFakeDiffuseNormal) {
		this.template = template;
		this.shadeSwapVertices = shadeSwapVertices;
		this.invertFakeDiffuseNormal = invertFakeDiffuseNormal;
		reset();
	}

	public ShadeSeparatingSuperByteBuffer(TemplateMesh template, int[] shadeSwapVertices) {
		this(template, shadeSwapVertices, false);
	}

	public ShadeSeparatingSuperByteBuffer(TemplateMesh template) {
		this(template, new int[0]);
	}

	private static boolean isBufferMax() {
		return BUFFED_VERTEX >= BUFFER_VERTEX_COUNT;
	}

	private static void flush(VertexBufferWriter writer, boolean force, VertexFormat format) {
		if (BUFFED_VERTEX == 0) return;
		if (!force && !isBufferMax()) {
			return;
		}
		STACK.push();
		writer.push(STACK, SCRATCH_BUFFER, BUFFED_VERTEX, format);
		STACK.pop();
		BUFFER_PTR = SCRATCH_BUFFER;
		BUFFED_VERTEX = 0;
	}

	private void irisPathRenderInto(PoseStack input, VertexBufferWriter writer) {
		Matrix4f modelMat = this.modelMat.set(input.last()
			.pose());
		Matrix4f localTransforms = transforms.last()
			.pose();
		modelMat.mul(localTransforms);

		Matrix3f normalMat = this.normalMat.set(input.last()
			.normal());
		Matrix3f localNormalTransforms = transforms.last()
			.normal();
		normalMat.mul(localNormalTransforms);

		Vector3f normal = this.normal;
		ShiftOutput shiftOutput = this.shiftOutput;
		Vector3f lightDir0 = this.lightDir0;
		Vector3f lightDir1 = this.lightDir1;

		boolean applyDiffuse = !disableDiffuse && !ShadersModHelper.isShaderPackInUse();
		boolean shaded = true;
		int shadeSwapIndex = 0;
		int nextShadeSwapVertex = shadeSwapIndex < shadeSwapVertices.length ? shadeSwapVertices[shadeSwapIndex] : -1;
		int unshadedDiffuse = 255;
		if (applyDiffuse) {
			lightDir0.set(RenderSystemAccessor.catnip$getShaderLightDirections()[0]).normalize();
			lightDir1.set(RenderSystemAccessor.catnip$getShaderLightDirections()[1]).normalize();
			if (shadeSwapVertices.length > 0) {
				// Pretend unshaded faces always point up to get the correct max diffuse value for the current level.
				normal.set(0, invertFakeDiffuseNormal ? -1 : 1, 0);
				// Don't apply the normal matrix since that would cause upside down objects to be dark.
				unshadedDiffuse = (int) (255 * calculateDiffuse(normal, lightDir0, lightDir1));
			}
		}

		int vertexCount = template.vertexCount();
		int quadCount = vertexCount / 4;
		for (int i = 0; i < quadCount * 4; i += 4) {
			if (i >= nextShadeSwapVertex) {
				shaded = !shaded;
				shadeSwapIndex++;
				nextShadeSwapVertex = shadeSwapIndex < shadeSwapVertices.length ? shadeSwapVertices[shadeSwapIndex] : -1;
			}

			int packedNormal = template.normal(i);
			float normalX = ((byte) (packedNormal & 0xFF)) / 127.0f;
			float normalY = ((byte) ((packedNormal >>> 8) & 0xFF)) / 127.0f;
			float normalZ = ((byte) ((packedNormal >>> 16) & 0xFF)) / 127.0f;
			normal.set(normalX, normalY, normalZ);
			normal.mul(normalMat);

			pos4[0].set(template.x(i), template.y(i), template.z(i)).mulPosition(modelMat);
			pos4[1].set(template.x(i + 1), template.y(i + 1), template.z(i + 1)).mulPosition(modelMat);
			pos4[2].set(template.x(i + 2), template.y(i + 2), template.z(i + 2)).mulPosition(modelMat);
			pos4[3].set(template.x(i + 3), template.y(i + 3), template.z(i + 3)).mulPosition(modelMat);

			if (RenderSystem.getModelViewMatrix().m32() == 0) // do backface culling
			{
				Vector3f view = new Vector3f((pos4[0].x + pos4[2].x) * 0.5f, (pos4[0].y + pos4[2].y) * 0.5f, (pos4[0].z + pos4[2].z) * 0.5f).normalize();

				if (view.dot(normal) > 0)
					continue;
			}

			int n = NormI8.pack(normal);
			if (spriteShiftFunc != null) {
				spriteShiftFunc.shift(template.u(i), template.v(i), shiftOutput);
				uv4[0].set(shiftOutput.u, shiftOutput.v);

				spriteShiftFunc.shift(template.u(i + 1), template.v(i + 1), shiftOutput);
				uv4[1].set(shiftOutput.u, shiftOutput.v);

				spriteShiftFunc.shift(template.u(i + 2), template.v(i + 2), shiftOutput);
				uv4[2].set(shiftOutput.u, shiftOutput.v);

				spriteShiftFunc.shift(template.u(i + 3), template.v(i + 3), shiftOutput);
				uv4[3].set(shiftOutput.u, shiftOutput.v);
			} else {
				uv4[0].set(template.u(i), template.v(i));
				uv4[1].set(template.u(i + 1), template.v(i + 1));
				uv4[2].set(template.u(i + 2), template.v(i + 2));
				uv4[3].set(template.u(i + 3), template.v(i + 3));
			}

			float mid_u = (uv4[0].x + uv4[1].x + uv4[2].x + uv4[3].x) / 4;
			float mid_v = (uv4[0].y + uv4[1].y + uv4[2].y + uv4[3].y) / 4;

			int tangent = NormalHelper.computeTangent(null, normal.x(), normal.y(), normal.z(),
				pos4[0].x, pos4[0].y, pos4[0].z, uv4[0].x, uv4[0].y,
				pos4[1].x, pos4[1].y, pos4[1].z, uv4[1].x, uv4[1].y,
				pos4[2].x, pos4[2].y, pos4[2].z, uv4[2].x, uv4[2].y);

			int color = ColorMixer.mulComponentWise(template.color(i), this.vertex_color);
			if (applyDiffuse) {
				int factor = shaded ? (int) (255.0F * calculateDiffuse(normal, lightDir0, lightDir1)) : unshadedDiffuse;
				color = ColorARGB.mulRGB(color, factor);
			}

			int light0 = hasCustomLight ? SuperByteBuffer.maxLight(template.light(i), packedLight) : template.light(i);
			int light1 = hasCustomLight ? SuperByteBuffer.maxLight(template.light(i + 1), packedLight) : template.light(i + 1);
			int light2 = hasCustomLight ? SuperByteBuffer.maxLight(template.light(i + 2), packedLight) : template.light(i + 2);
			int light3 = hasCustomLight ? SuperByteBuffer.maxLight(template.light(i + 3), packedLight) : template.light(i + 3);

			if (useLevelLight) {
				normal.set(((template.x(i) - .5f) * 15 / 16f) + .5f, (template.y(i) - .5f) * 15 / 16f + .5f, (template.z(i) - .5f) * 15 / 16f + .5f).mulPosition(localTransforms);
				light0 = SuperByteBuffer.maxLight(light0, getLight(levelWithLight, lightTransform == null ? normal : normal.mulPosition(lightTransform)));
				normal.set(((template.x(i + 1) - .5f) * 15 / 16f) + .5f, (template.y(i + 1) - .5f) * 15 / 16f + .5f, (template.z(i + 1) - .5f) * 15 / 16f + .5f).mulPosition(localTransforms);
				light1 = SuperByteBuffer.maxLight(light1, getLight(levelWithLight, lightTransform == null ? normal : normal.mulPosition(lightTransform)));
				normal.set(((template.x(i + 2) - .5f) * 15 / 16f) + .5f, (template.y(i + 2) - .5f) * 15 / 16f + .5f, (template.z(i + 2) - .5f) * 15 / 16f + .5f).mulPosition(localTransforms);
				light2 = SuperByteBuffer.maxLight(light2, getLight(levelWithLight, lightTransform == null ? normal : normal.mulPosition(lightTransform)));
				normal.set(((template.x(i + 3) - .5f) * 15 / 16f) + .5f, (template.y(i + 3) - .5f) * 15 / 16f + .5f, (template.z(i + 3) - .5f) * 15 / 16f + .5f).mulPosition(localTransforms);
				light3 = SuperByteBuffer.maxLight(light3, getLight(levelWithLight, lightTransform == null ? normal : normal.mulPosition(lightTransform)));
			}
			TerrainVertex.write(BUFFER_PTR, pos4[0].x, pos4[0].y, pos4[0].z, color, uv4[0].x, uv4[0].y, mid_u, mid_v, light0, n, tangent);
			BUFFER_PTR += TerrainVertex.STRIDE;

			TerrainVertex.write(BUFFER_PTR, pos4[1].x, pos4[1].y, pos4[1].z, color, uv4[1].x, uv4[1].y, mid_u, mid_v, light1, n, tangent);
			BUFFER_PTR += TerrainVertex.STRIDE;

			TerrainVertex.write(BUFFER_PTR, pos4[2].x, pos4[2].y, pos4[2].z, color, uv4[2].x, uv4[2].y, mid_u, mid_v, light2, n, tangent);
			BUFFER_PTR += TerrainVertex.STRIDE;

			TerrainVertex.write(BUFFER_PTR, pos4[3].x, pos4[3].y, pos4[3].z, color, uv4[3].x, uv4[3].y, mid_u, mid_v, light3, n, tangent);
			BUFFER_PTR += TerrainVertex.STRIDE;

			BUFFED_VERTEX += 4;
			flush(writer, false, IrisVertexFormats.TERRAIN);
		}

		flush(writer, true, IrisVertexFormats.TERRAIN);
	}

	private void sodiumPathRenderInto(PoseStack input, VertexBufferWriter writer) {
		Matrix4f modelMat = this.modelMat.set(input.last()
			.pose());
		Matrix4f localTransforms = transforms.last()
			.pose();
		modelMat.mul(localTransforms);

		Matrix3f normalMat = this.normalMat.set(input.last()
			.normal());
		Matrix3f localNormalTransforms = transforms.last()
			.normal();
		normalMat.mul(localNormalTransforms);

		Vector4f pos = this.pos;
		Vector3f normal = this.normal;
		ShiftOutput shiftOutput = this.shiftOutput;
		Vector3f lightDir0 = this.lightDir0;
		Vector3f lightDir1 = this.lightDir1;
		Vector4f lightPos = this.lightPos;

		boolean applyDiffuse = !disableDiffuse && !ShadersModHelper.isShaderPackInUse();
		boolean shaded = true;
		int shadeSwapIndex = 0;
		int nextShadeSwapVertex = shadeSwapIndex < shadeSwapVertices.length ? shadeSwapVertices[shadeSwapIndex] : -1;
		int unshadedDiffuse = 255;
		if (applyDiffuse) {
			lightDir0.set(RenderSystemAccessor.catnip$getShaderLightDirections()[0]).normalize();
			lightDir1.set(RenderSystemAccessor.catnip$getShaderLightDirections()[1]).normalize();
			if (shadeSwapVertices.length > 0) {
				// Pretend unshaded faces always point up to get the correct max diffuse value for the current level.
				normal.set(0, invertFakeDiffuseNormal ? -1 : 1, 0);
				// Don't apply the normal matrix since that would cause upside down objects to be dark.
				unshadedDiffuse = (int) (255 * calculateDiffuse(normal, lightDir0, lightDir1));
			}
		}

		int vertexCount = template.vertexCount();
		int quadCount = vertexCount / 4;
		for (int q = 0; q < quadCount; q++) {
			int i = q * 4;
			int packedNormal = template.normal(i);
			float normalX = ((byte) (packedNormal & 0xFF)) / 127.0f;
			float normalY = ((byte) ((packedNormal >>> 8) & 0xFF)) / 127.0f;
			float normalZ = ((byte) ((packedNormal >>> 16) & 0xFF)) / 127.0f;
			normal.set(normalX, normalY, normalZ);
			normal.mul(normalMat);

			for (; i < q * 4 + 4; i++) {
				if (i == nextShadeSwapVertex) {
					shaded = !shaded;
					shadeSwapIndex++;
					nextShadeSwapVertex = shadeSwapIndex < shadeSwapVertices.length ? shadeSwapVertices[shadeSwapIndex] : -1;
				}

				float x = template.x(i);
				float y = template.y(i);
				float z = template.z(i);
				pos.set(x, y, z, 1.0f);
				pos.mul(modelMat);

				int color = ColorMixer.mulComponentWise(template.color(i), this.vertex_color);
				if (applyDiffuse) {
					int factor = shaded ? (int) (255.0F * calculateDiffuse(normal, lightDir0, lightDir1)) : unshadedDiffuse;
					color = ColorARGB.mulRGB(color, factor);
				}

				float u = template.u(i);
				float v = template.v(i);
				if (spriteShiftFunc != null) {
					spriteShiftFunc.shift(u, v, shiftOutput);
					u = shiftOutput.u;
					v = shiftOutput.v;
				}

				int light = template.light(i);
				if (hasCustomLight) {
					light = SuperByteBuffer.maxLight(light, packedLight);
				}
				if (useLevelLight) {
					lightPos.set(((x - .5f) * 15 / 16f) + .5f, (y - .5f) * 15 / 16f + .5f, (z - .5f) * 15 / 16f + .5f, 1f);
					lightPos.mul(localTransforms);
					if (lightTransform != null) {
						lightPos.mul(lightTransform);
					}
					light = SuperByteBuffer.maxLight(light, getLight(levelWithLight, lightPos));
				}

				BlockVertex.write(BUFFER_PTR, pos.x, pos.y, pos.z, color, u, v, light, NormI8.pack(normal));
				BUFFED_VERTEX++;
				BUFFER_PTR += BlockVertex.STRIDE;
				flush(writer, false, BlockVertex.FORMAT);
			}
		}

		flush(writer, true, BlockVertex.FORMAT);
	}

	private void defaultPathRenderInto(PoseStack input, VertexConsumer builder) {
		if (useLevelLight) {
			WORLD_LIGHT_CACHE.clear();
		}

		Matrix4f modelMat = this.modelMat.set(input.last()
			.pose());
		Matrix4f localTransforms = transforms.last()
			.pose();
		modelMat.mul(localTransforms);

		Matrix3f normalMat = this.normalMat.set(input.last()
			.normal());
		Matrix3f localNormalTransforms = transforms.last()
			.normal();
		normalMat.mul(localNormalTransforms);

		Vector4f pos = this.pos;
		Vector3f normal = this.normal;
		ShiftOutput shiftOutput = this.shiftOutput;
		Vector3f lightDir0 = this.lightDir0;
		Vector3f lightDir1 = this.lightDir1;
		Vector4f lightPos = this.lightPos;

		boolean applyDiffuse = !disableDiffuse && !ShadersModHelper.isShaderPackInUse();
		boolean shaded = true;
		int shadeSwapIndex = 0;
		int nextShadeSwapVertex = shadeSwapIndex < shadeSwapVertices.length ? shadeSwapVertices[shadeSwapIndex] : -1;
		int unshadedDiffuse = 255;
		if (applyDiffuse) {
			lightDir0.set(RenderSystemAccessor.catnip$getShaderLightDirections()[0]).normalize();
			lightDir1.set(RenderSystemAccessor.catnip$getShaderLightDirections()[1]).normalize();
			if (shadeSwapVertices.length > 0) {
				// Pretend unshaded faces always point up to get the correct max diffuse value for the current level.
				normal.set(0, invertFakeDiffuseNormal ? -1 : 1, 0);
				// Don't apply the normal matrix since that would cause upside down objects to be dark.
				unshadedDiffuse = (int) (255 * calculateDiffuse(normal, lightDir0, lightDir1));
			}
		}

		int vertexCount = template.vertexCount();
		for (int i = 0; i < vertexCount; i++) {
			if (i == nextShadeSwapVertex) {
				shaded = !shaded;
				shadeSwapIndex++;
				nextShadeSwapVertex = shadeSwapIndex < shadeSwapVertices.length ? shadeSwapVertices[shadeSwapIndex] : -1;
			}

			float x = template.x(i);
			float y = template.y(i);
			float z = template.z(i);
			pos.set(x, y, z, 1.0f);
			pos.mul(modelMat);

			int packedNormal = template.normal(i);
			float normalX = ((byte) (packedNormal & 0xFF)) / 127.0f;
			float normalY = ((byte) ((packedNormal >>> 8) & 0xFF)) / 127.0f;
			float normalZ = ((byte) ((packedNormal >>> 16) & 0xFF)) / 127.0f;
			normal.set(normalX, normalY, normalZ);
			normal.mul(normalMat);

			int color = ColorMixer.mulComponentWise(template.color(i), this.vertex_color);
			if (applyDiffuse) {
				int factor = shaded ? (int) (255.0F * calculateDiffuse(normal, lightDir0, lightDir1)) : unshadedDiffuse;
				color = ColorARGB.mulRGB(color, factor);
			}
			float r = (color & 0xFF) / 255.0f;
			float g = ((color >>> 8) & 0xFF) / 255.0f;
			float b = ((color >>> 16) & 0xFF) / 255.0f;
			float a = ((color >>> 24) & 0xFF) / 255.0f;

			float u = template.u(i);
			float v = template.v(i);
			if (spriteShiftFunc != null) {
				spriteShiftFunc.shift(u, v, shiftOutput);
				u = shiftOutput.u;
				v = shiftOutput.v;
			}

			int overlay;
			if (hasCustomOverlay) {
				overlay = this.overlay;
			} else {
				overlay = template.overlay(i);
			}

			int light = template.light(i);
			if (hasCustomLight) {
				light = SuperByteBuffer.maxLight(light, packedLight);
			}
			if (useLevelLight) {
				lightPos.set(((x - .5f) * 15 / 16f) + .5f, (y - .5f) * 15 / 16f + .5f, (z - .5f) * 15 / 16f + .5f, 1f);
				lightPos.mul(localTransforms);
				if (lightTransform != null) {
					lightPos.mul(lightTransform);
				}
				light = SuperByteBuffer.maxLight(light, getLight(levelWithLight, lightPos));
			}

			builder.addVertex(pos.x(), pos.y(), pos.z()).setColor(r, g, b, a).setUv(u, v).setOverlay(overlay).setLight(light).setNormal(normal.x(), normal.y(), normal.z());
		}

	}

	public void renderInto(PoseStack input, VertexConsumer builder) {
		if (isEmpty()) {
			return;
		}

		if (useLevelLight) {
			WORLD_LIGHT_CACHE.clear();
		}
		if (builder instanceof BufferBuilder bb) {
			VertexBufferWriter writer = VertexBufferWriter.tryOf(builder);
			if (writer != null) {
				if (bb.format == TerrainVertex.FORMAT) {
					irisPathRenderInto(input, writer);
				} else if (bb.format == BlockVertex.FORMAT) {
					sodiumPathRenderInto(input, writer);
				} else {
					defaultPathRenderInto(input, builder);
				}
			}
		} else {
			defaultPathRenderInto(input, builder);
		}
		reset();
	}

	public SuperByteBuffer reset() {
		while (!transforms.clear())
			transforms.popPose();
		transforms.pushPose();

		vertex_color = 0xffffffff;
		disableDiffuse = false;
		spriteShiftFunc = null;
		hasCustomOverlay = false;
		overlay = OverlayTexture.NO_OVERLAY;
		hasCustomLight = false;
		packedLight = 0;
		useLevelLight = false;
		levelWithLight = null;
		lightTransform = null;
		return this;
	}

	public boolean isEmpty() {
		return template.isEmpty();
	}

	public PoseStack getTransforms() {
		return transforms;
	}

	@Override
	public SuperByteBuffer scale(float factorX, float factorY, float factorZ) {
		transforms.scale(factorX, factorY, factorZ);
		return this;
	}

	@Override
	public SuperByteBuffer rotate(Quaternionfc quaternion) {
		var last = transforms.last();
		last.pose().rotate(quaternion);
		last.normal().rotate(quaternion);
		return this;
	}

	@Override
	public SuperByteBuffer translate(float x, float y, float z) {
		transforms.translate(x, y, z);
		return this;
	}

	@Override
	public SuperByteBuffer mulPose(Matrix4fc pose) {
		transforms.last()
			.pose()
			.mul(pose);
		return this;
	}

	@Override
	public SuperByteBuffer mulNormal(Matrix3fc normal) {
		transforms.last()
			.normal()
			.mul(normal);
		return this;
	}

	@Override
	public SuperByteBuffer pushPose() {
		transforms.pushPose();
		return this;
	}

	@Override
	public SuperByteBuffer popPose() {
		transforms.popPose();
		return this;
	}

	public SuperByteBuffer color(float r, float g, float b, float a) {
		color((int) (r / 255.0f), (int) (r / 255.0f), (int) (r / 255.0f), (int) (r / 255.0f));
		return this;
	}

	public SuperByteBuffer color(int r, int g, int b, int a) {
		this.vertex_color = ColorARGB.pack(r, g, b, a);
		return this;
	}

	public SuperByteBuffer color(int color) {
		this.vertex_color = 0xff000000 | (color & 0xffffff);
		return this;
	}

	public SuperByteBuffer color(Color c) {
		return color(c.getRGB());
	}

	public SuperByteBuffer disableDiffuse() {
		disableDiffuse = true;
		return this;
	}

	public SuperByteBuffer shiftUV(SpriteShiftEntry entry) {
		spriteShiftFunc = (u, v, output) -> {
			output.accept(entry.getTargetU(u), entry.getTargetV(v));
		};
		return this;
	}

	public SuperByteBuffer shiftUVScrolling(SpriteShiftEntry entry, float scrollV) {
		return shiftUVScrolling(entry, 0, scrollV);
	}

	public SuperByteBuffer shiftUVScrolling(SpriteShiftEntry entry, float scrollU, float scrollV) {
		spriteShiftFunc = (u, v, output) -> {
			float targetU = u - entry.getOriginal()
				.getU0() + entry.getTarget()
				.getU0()
				+ scrollU;
			float targetV = v - entry.getOriginal()
				.getV0() + entry.getTarget()
				.getV0()
				+ scrollV;
			output.accept(targetU, targetV);
		};
		return this;
	}

	public SuperByteBuffer shiftUVtoSheet(SpriteShiftEntry entry, float uTarget, float vTarget, int sheetSize) {
		spriteShiftFunc = (u, v, output) -> {
			float targetU = entry.getTarget()
				.getU((SpriteShiftEntry.getUnInterpolatedU(entry.getOriginal(), u) / sheetSize) + uTarget);
			float targetV = entry.getTarget()
				.getV((SpriteShiftEntry.getUnInterpolatedV(entry.getOriginal(), v) / sheetSize) + vTarget);
			output.accept(targetU, targetV);
		};
		return this;
	}

	public SuperByteBuffer overlay(int overlay) {
		hasCustomOverlay = true;
		this.overlay = overlay;
		return this;
	}

	public SuperByteBuffer light(int packedLight) {
		hasCustomLight = true;
		this.packedLight = packedLight;
		return this;
	}

	@Override
	public SuperByteBuffer useLevelLight(BlockAndTintGetter level) {
		useLevelLight = true;
		levelWithLight = level;
		return this;
	}

	@Override
	public SuperByteBuffer useLevelLight(BlockAndTintGetter level, Matrix4f lightTransform) {
		useLevelLight = true;
		levelWithLight = level;
		this.lightTransform = lightTransform;
		return this;
	}

	// Adapted from minecraft:shaders/include/light.glsl
	private static float calculateDiffuse(Vector3fc normal, Vector3fc lightDir0, Vector3fc lightDir1) {
		float light0 = Math.max(0.0f, lightDir0.dot(normal));
		float light1 = Math.max(0.0f, lightDir1.dot(normal));
		return Math.min(1.0f, (light0 + light1) * 0.6f + 0.4f);
	}

	private static int getLight(BlockAndTintGetter world, Vector4f lightPos) {
		BlockPos pos = BlockPos.containing(lightPos.x(), lightPos.y(), lightPos.z());
		return WORLD_LIGHT_CACHE.computeIfAbsent(pos.asLong(), $ -> LevelRenderer.getLightColor(world, pos));
	}

	private static int getLight(BlockAndTintGetter world, Vector3f lightPos) {
		BlockPos pos = BlockPos.containing(lightPos.x(), lightPos.y(), lightPos.z());
		return WORLD_LIGHT_CACHE.computeIfAbsent(pos.asLong(), $ -> LevelRenderer.getLightColor(world, pos));
	}
}
