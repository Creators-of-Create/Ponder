package net.createmod.catnip.platform;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import com.mojang.blaze3d.vertex.VertexFormat;

import net.caffeinemc.mods.sodium.api.math.MatrixHelper;
import net.caffeinemc.mods.sodium.api.util.ColorARGB;
import net.caffeinemc.mods.sodium.api.util.ColorMixer;
import net.caffeinemc.mods.sodium.api.util.NormI8;
import net.caffeinemc.mods.sodium.api.vertex.buffer.VertexBufferWriter;
import net.createmod.catnip.render.compat.EntityVertex;
import net.createmod.catnip.render.compat.IrisEntityVertex;
import net.createmod.ponder.mixin.client.accessor.RenderSystemAccessor;
import net.createmod.catnip.platform.services.ExternalRenderHelper;
import net.createmod.catnip.render.ShadeSeparatingSuperByteBuffer;
import net.createmod.catnip.render.SuperByteBuffer;
import net.createmod.catnip.render.TemplateMesh;
import net.createmod.catnip.render.compat.BlockVertex;
import net.createmod.catnip.render.compat.IrisTerrainVertex;

import net.irisshaders.iris.vertices.NormalHelper;

import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;

import static net.createmod.catnip.render.ShadeSeparatingSuperByteBuffer.calculateDiffuse;

public class NeoforgeExternalRenderHelper implements ExternalRenderHelper {
	private static final int BUFFER_VERTEX_COUNT = 48;
	private static final MemoryStack STACK = MemoryStack.create();
	private static final int BUFFER_SIZE = BUFFER_VERTEX_COUNT * IrisEntityVertex.STRIDE;
	private static final long SCRATCH_BUFFER = MemoryUtil.nmemAlignedAlloc(64, BUFFER_SIZE);
	private static long BUFFER_PTR = SCRATCH_BUFFER;
	private static int BUFFED_VERTEX = 0;

	// Reused objects
	private static final Matrix4f modelMat = new Matrix4f();
	private static final Matrix3f normalMat = new Matrix3f();
	private static final Vector3f float3 = new Vector3f();
	private static final Vector3f lightDir0 = new Vector3f();
	private static final Vector3f lightDir1 = new Vector3f();
	private static final SuperByteBuffer.ShiftOutput shiftOutput = new SuperByteBuffer.ShiftOutput();
	private static final Vector3f pos0 = new Vector3f();
	private static final Vector3f pos1 = new Vector3f();
	private static final Vector3f pos2 = new Vector3f();
	private static final Vector3f pos3 = new Vector3f();
	private static final Vector2f uv0 = new Vector2f();
	private static final Vector2f uv1 = new Vector2f();
	private static final Vector2f uv2 = new Vector2f();
	private static final Vector2f uv3 = new Vector2f();

	private static boolean isBufferMax() {
		return BUFFED_VERTEX >= BUFFER_VERTEX_COUNT;
	}

	private static void flush(VertexBufferWriter writer, boolean force, VertexFormat format) {
		if (!force && !isBufferMax()) {
			return;
		}
		if (BUFFED_VERTEX == 0) return;
		STACK.push();
		writer.push(STACK, SCRATCH_BUFFER, BUFFED_VERTEX, format);
		STACK.pop();
		BUFFER_PTR = SCRATCH_BUFFER;
		BUFFED_VERTEX = 0;
	}

	private static boolean isPerspectiveProjection() {
		return RenderSystem.getModelViewMatrix().m32() == 0;
	}

	private static void IrisRenderInto(ShadeSeparatingSuperByteBuffer byteBuffer, PoseStack input, VertexBufferWriter writer, VertexFormat format) {
		PoseStack transforms = byteBuffer.getTransforms();
		modelMat.set(input.last().pose());
		Matrix4f localTransforms = transforms.last().pose();
		modelMat.mul(localTransforms);

		normalMat.set(input.last().normal());
		Matrix3f localNormalTransforms = transforms.last().normal();
		normalMat.mul(localNormalTransforms);

		SuperByteBuffer.SpriteShiftFunc spriteShiftFunc = byteBuffer.getSpriteShiftFunc();
		boolean useLevelLight = byteBuffer.isUsingLevelLight();
		boolean hasCustomLight = byteBuffer.hasCustomLight();
		boolean isTerrain = (format == IrisTerrainVertex.FORMAT);
		boolean isPerspectiveProjection = isPerspectiveProjection();

		TemplateMesh template = byteBuffer.getTemplateMesh();
		int vertexCount = template.vertexCount();
		for (int i = 0; i < vertexCount; i += 4) {
			int packedNormal = template.normal(i);
			float unpackedX = NormI8.unpackX(packedNormal);
			float unpackedY = NormI8.unpackY(packedNormal);
			float unpackedZ = NormI8.unpackZ(packedNormal);
			float nx = MatrixHelper.transformNormalX(normalMat, unpackedX, unpackedY, unpackedZ);
			float ny = MatrixHelper.transformNormalY(normalMat, unpackedX, unpackedY, unpackedZ);
			float nz = MatrixHelper.transformNormalZ(normalMat, unpackedX, unpackedY, unpackedZ);

			pos0.set(template.x(i), template.y(i), template.z(i)).mulPosition(modelMat);
			pos2.set(template.x(i + 2), template.y(i + 2), template.z(i + 2)).mulPosition(modelMat);
			if (isPerspectiveProjection) { // do backface culling
				if (nx * (pos0.x + pos2.x) + ny * (pos0.y + pos2.y) + nz * (pos0.z + pos2.z) > 0) continue;
			}
			pos1.set(template.x(i + 1), template.y(i + 1), template.z(i + 1)).mulPosition(modelMat);
			pos3.set(template.x(i + 3), template.y(i + 3), template.z(i + 3)).mulPosition(modelMat);

			int normal = NormI8.pack(nx, ny, nz);
			int tangent = NormalHelper.computeTangent(null, nx, ny, nz, pos0.x, pos0.y, pos0.z, uv0.x, uv0.y, pos1.x, pos1.y, pos1.z, uv1.x, uv1.y, pos2.x, pos2.y, pos2.z, uv2.x, uv2.y);

			if (spriteShiftFunc != null) {
				spriteShiftFunc.shift(template.u(i), template.v(i), shiftOutput);
				uv0.set(shiftOutput.u, shiftOutput.v);

				spriteShiftFunc.shift(template.u(i + 1), template.v(i + 1), shiftOutput);
				uv1.set(shiftOutput.u, shiftOutput.v);

				spriteShiftFunc.shift(template.u(i + 2), template.v(i + 2), shiftOutput);
				uv2.set(shiftOutput.u, shiftOutput.v);

				spriteShiftFunc.shift(template.u(i + 3), template.v(i + 3), shiftOutput);
				uv3.set(shiftOutput.u, shiftOutput.v);
			} else {
				uv0.set(template.u(i), template.v(i));
				uv1.set(template.u(i + 1), template.v(i + 1));
				uv2.set(template.u(i + 2), template.v(i + 2));
				uv3.set(template.u(i + 3), template.v(i + 3));
			}

			float mid_u = (uv0.x + uv1.x + uv2.x + uv3.x) / 4;
			float mid_v = (uv0.y + uv1.y + uv2.y + uv3.y) / 4;

			int vertexColor = byteBuffer.getVertexColor();
			int color = ColorMixer.mulComponentWise(template.color(i), vertexColor);

			int light0 = template.light(i);
			int light1 = template.light(i + 1);
			int light2 = template.light(i + 2);
			int light3 = template.light(i + 3);
			if (hasCustomLight) {
				int packedLight = byteBuffer.getPackedLight();
				light0 = SuperByteBuffer.maxLight(light0, packedLight);
				light1 = SuperByteBuffer.maxLight(light1, packedLight);
				light2 = SuperByteBuffer.maxLight(light2, packedLight);
				light3 = SuperByteBuffer.maxLight(light3, packedLight);
			}

			if (useLevelLight) {
				float3.set(((template.x(i) - .5f) * 15 / 16f) + .5f, (template.y(i) - .5f) * 15 / 16f + .5f, (template.z(i) - .5f) * 15 / 16f + .5f).mulPosition(localTransforms);
				light0 = SuperByteBuffer.maxLight(light0, byteBuffer.getLight(float3));
				float3.set(((template.x(i + 1) - .5f) * 15 / 16f) + .5f, (template.y(i + 1) - .5f) * 15 / 16f + .5f, (template.z(i + 1) - .5f) * 15 / 16f + .5f).mulPosition(localTransforms);
				light1 = SuperByteBuffer.maxLight(light1, byteBuffer.getLight(float3));
				float3.set(((template.x(i + 2) - .5f) * 15 / 16f) + .5f, (template.y(i + 2) - .5f) * 15 / 16f + .5f, (template.z(i + 2) - .5f) * 15 / 16f + .5f).mulPosition(localTransforms);
				light2 = SuperByteBuffer.maxLight(light2, byteBuffer.getLight(float3));
				float3.set(((template.x(i + 3) - .5f) * 15 / 16f) + .5f, (template.y(i + 3) - .5f) * 15 / 16f + .5f, (template.z(i + 3) - .5f) * 15 / 16f + .5f).mulPosition(localTransforms);
				light3 = SuperByteBuffer.maxLight(light3, byteBuffer.getLight(float3));
			}

			if (isTerrain) { // IrisTerrainVertex.FORMAT
				IrisTerrainVertex.write(BUFFER_PTR, pos0.x, pos0.y, pos0.z, color, uv0.x, uv0.y, mid_u, mid_v, light0, normal, tangent);
				BUFFER_PTR += IrisTerrainVertex.STRIDE;

				IrisTerrainVertex.write(BUFFER_PTR, pos1.x, pos1.y, pos1.z, color, uv1.x, uv1.y, mid_u, mid_v, light1, normal, tangent);
				BUFFER_PTR += IrisTerrainVertex.STRIDE;

				IrisTerrainVertex.write(BUFFER_PTR, pos2.x, pos2.y, pos2.z, color, uv2.x, uv2.y, mid_u, mid_v, light2, normal, tangent);
				BUFFER_PTR += IrisTerrainVertex.STRIDE;

				IrisTerrainVertex.write(BUFFER_PTR, pos3.x, pos3.y, pos3.z, color, uv3.x, uv3.y, mid_u, mid_v, light3, normal, tangent);
				BUFFER_PTR += IrisTerrainVertex.STRIDE;
			} else { // IrisEntityVertex.FORMAT
				int overlay0, overlay1, overlay2, overlay3;
				if (byteBuffer.hasCustomOverlay()) {
					overlay0 = overlay1 = overlay2 = overlay3 = byteBuffer.getOverlay();
				} else {
					overlay0 = template.overlay(i);
					overlay1 = template.overlay(i + 1);
					overlay2 = template.overlay(i + 2);
					overlay3 = template.overlay(i + 3);
				}
				IrisEntityVertex.write(BUFFER_PTR, pos0.x, pos0.y, pos0.z, color, uv0.x, uv0.y, mid_u, mid_v, overlay0, light0, normal, tangent);
				BUFFER_PTR += IrisEntityVertex.STRIDE;

				IrisEntityVertex.write(BUFFER_PTR, pos1.x, pos1.y, pos1.z, color, uv1.x, uv1.y, mid_u, mid_v, overlay1, light1, normal, tangent);
				BUFFER_PTR += IrisEntityVertex.STRIDE;

				IrisEntityVertex.write(BUFFER_PTR, pos2.x, pos2.y, pos2.z, color, uv2.x, uv2.y, mid_u, mid_v, overlay2, light2, normal, tangent);
				BUFFER_PTR += IrisEntityVertex.STRIDE;

				IrisEntityVertex.write(BUFFER_PTR, pos3.x, pos3.y, pos3.z, color, uv3.x, uv3.y, mid_u, mid_v, overlay3, light3, normal, tangent);
				BUFFER_PTR += IrisEntityVertex.STRIDE;
			}
			BUFFED_VERTEX += 4;
			flush(writer, false, format);
		}

		flush(writer, true, format);
	}

	private static void SodiumRenderInto(ShadeSeparatingSuperByteBuffer byteBuffer, PoseStack input, VertexBufferWriter writer, VertexFormat format) {
		PoseStack transforms = byteBuffer.getTransforms();
		modelMat.set(input.last().pose());
		Matrix4f localTransforms = transforms.last().pose();
		modelMat.mul(localTransforms);

		normalMat.set(input.last().normal());
		Matrix3f localNormalTransforms = transforms.last().normal();
		normalMat.mul(localNormalTransforms);

		boolean shaded = true;
		int shadeSwapIndex = 0;
		int[] shadeSwapVertices = byteBuffer.getShadeSwapVertices();
		int nextShadeSwapVertex = shadeSwapIndex < shadeSwapVertices.length ? shadeSwapVertices[shadeSwapIndex] : Integer.MAX_VALUE;
		int unshadedDiffuse = 255;
		boolean applyDiffuse = !byteBuffer.isDisableDiffuse();
		if (!byteBuffer.isDisableDiffuse()) {
			lightDir0.set(RenderSystemAccessor.catnip$getShaderLightDirections()[0]).normalize();
			lightDir1.set(RenderSystemAccessor.catnip$getShaderLightDirections()[1]).normalize();
			if (shadeSwapVertices.length > 0) {
				// Pretend unshaded faces always point up to get the correct max diffuse value for the current level.
				float3.set(0, byteBuffer.isInvertFakeDiffuseNormal() ? -1 : 1, 0);
				// Don't apply the normal matrix since that would cause upside down objects to be dark.
				unshadedDiffuse = (int) (255 * calculateDiffuse(float3, lightDir0, lightDir1));
			}
		}

		SuperByteBuffer.SpriteShiftFunc spriteShiftFunc = byteBuffer.getSpriteShiftFunc();
		boolean useLevelLight = byteBuffer.isUsingLevelLight();
		boolean hasCustomLight = byteBuffer.hasCustomLight();
		boolean isPerspectiveProjection = isPerspectiveProjection();

		TemplateMesh template = byteBuffer.getTemplateMesh();
		int vertexCount = template.vertexCount();
		for (int i = 0; i < vertexCount; i += 4) {
			if (i >= nextShadeSwapVertex) {
				shaded = !shaded;
				shadeSwapIndex++;
				nextShadeSwapVertex = shadeSwapIndex < shadeSwapVertices.length ? shadeSwapVertices[shadeSwapIndex] : Integer.MAX_VALUE;
			}

			int packedNormal = template.normal(i);
			float unpackedX = NormI8.unpackX(packedNormal);
			float unpackedY = NormI8.unpackY(packedNormal);
			float unpackedZ = NormI8.unpackZ(packedNormal);
			float nx = MatrixHelper.transformNormalX(normalMat, unpackedX, unpackedY, unpackedZ);
			float ny = MatrixHelper.transformNormalY(normalMat, unpackedX, unpackedY, unpackedZ);
			float nz = MatrixHelper.transformNormalZ(normalMat, unpackedX, unpackedY, unpackedZ);

			pos0.set(template.x(i), template.y(i), template.z(i)).mulPosition(modelMat);
			pos2.set(template.x(i + 2), template.y(i + 2), template.z(i + 2)).mulPosition(modelMat);
			if (isPerspectiveProjection) { // do backface culling
				if (nx * (pos0.x + pos2.x) + ny * (pos0.y + pos2.y) + nz * (pos0.z + pos2.z) > 0) continue;
			}
			int normal = NormI8.pack(nx, ny, nz);
			pos1.set(template.x(i + 1), template.y(i + 1), template.z(i + 1)).mulPosition(modelMat);
			pos3.set(template.x(i + 3), template.y(i + 3), template.z(i + 3)).mulPosition(modelMat);

			if (spriteShiftFunc != null) {
				spriteShiftFunc.shift(template.u(i), template.v(i), shiftOutput);
				uv0.set(shiftOutput.u, shiftOutput.v);

				spriteShiftFunc.shift(template.u(i + 1), template.v(i + 1), shiftOutput);
				uv1.set(shiftOutput.u, shiftOutput.v);

				spriteShiftFunc.shift(template.u(i + 2), template.v(i + 2), shiftOutput);
				uv2.set(shiftOutput.u, shiftOutput.v);

				spriteShiftFunc.shift(template.u(i + 3), template.v(i + 3), shiftOutput);
				uv3.set(shiftOutput.u, shiftOutput.v);
			} else {
				uv0.set(template.u(i), template.v(i));
				uv1.set(template.u(i + 1), template.v(i + 1));
				uv2.set(template.u(i + 2), template.v(i + 2));
				uv3.set(template.u(i + 3), template.v(i + 3));
			}

			int vertexColor = byteBuffer.getVertexColor();
			int color = ColorMixer.mulComponentWise(template.color(i), vertexColor);
			if (applyDiffuse) {
				float3.set(nx, ny, nz);
				int factor = shaded ? (int) (255.0F * calculateDiffuse(float3, lightDir0, lightDir1)) : unshadedDiffuse;
				color = ColorARGB.mulRGB(color, factor);
			}

			int light0 = template.light(i);
			int light1 = template.light(i + 1);
			int light2 = template.light(i + 2);
			int light3 = template.light(i + 3);
			if (hasCustomLight) {
				int packedLight = byteBuffer.getPackedLight();
				light0 = SuperByteBuffer.maxLight(light0, packedLight);
				light1 = SuperByteBuffer.maxLight(light1, packedLight);
				light2 = SuperByteBuffer.maxLight(light2, packedLight);
				light3 = SuperByteBuffer.maxLight(light3, packedLight);
			}

			if (useLevelLight) {
				float3.set(((template.x(i) - .5f) * 15 / 16f) + .5f, (template.y(i) - .5f) * 15 / 16f + .5f, (template.z(i) - .5f) * 15 / 16f + .5f).mulPosition(localTransforms);
				light0 = SuperByteBuffer.maxLight(light0, byteBuffer.getLight(float3));
				float3.set(((template.x(i + 1) - .5f) * 15 / 16f) + .5f, (template.y(i + 1) - .5f) * 15 / 16f + .5f, (template.z(i + 1) - .5f) * 15 / 16f + .5f).mulPosition(localTransforms);
				light1 = SuperByteBuffer.maxLight(light1, byteBuffer.getLight(float3));
				float3.set(((template.x(i + 2) - .5f) * 15 / 16f) + .5f, (template.y(i + 2) - .5f) * 15 / 16f + .5f, (template.z(i + 2) - .5f) * 15 / 16f + .5f).mulPosition(localTransforms);
				light2 = SuperByteBuffer.maxLight(light2, byteBuffer.getLight(float3));
				float3.set(((template.x(i + 3) - .5f) * 15 / 16f) + .5f, (template.y(i + 3) - .5f) * 15 / 16f + .5f, (template.z(i + 3) - .5f) * 15 / 16f + .5f).mulPosition(localTransforms);
				light3 = SuperByteBuffer.maxLight(light3, byteBuffer.getLight(float3));
			}

			if (format == BlockVertex.FORMAT) { // BlockVertex.FORMAT
				BlockVertex.write(BUFFER_PTR, pos0.x, pos0.y, pos0.z, color, uv0.x, uv0.y, light0, normal);
				BUFFER_PTR += BlockVertex.STRIDE;

				BlockVertex.write(BUFFER_PTR, pos1.x, pos1.y, pos1.z, color, uv1.x, uv1.y, light1, normal);
				BUFFER_PTR += BlockVertex.STRIDE;

				BlockVertex.write(BUFFER_PTR, pos2.x, pos2.y, pos2.z, color, uv2.x, uv2.y, light2, normal);
				BUFFER_PTR += BlockVertex.STRIDE;

				BlockVertex.write(BUFFER_PTR, pos3.x, pos3.y, pos3.z, color, uv3.x, uv3.y, light3, normal);
				BUFFER_PTR += BlockVertex.STRIDE;
			} else { // EntityVertex.FORMAT
				int overlay0, overlay1, overlay2, overlay3;
				if (byteBuffer.hasCustomOverlay()) {
					overlay0 = overlay1 = overlay2 = overlay3 = byteBuffer.getOverlay();
				} else {
					overlay0 = template.overlay(i);
					overlay1 = template.overlay(i + 1);
					overlay2 = template.overlay(i + 2);
					overlay3 = template.overlay(i + 3);
				}
				EntityVertex.write(BUFFER_PTR, pos0.x, pos0.y, pos0.z, color, uv0.x, uv0.y, overlay0, light0, normal);
				BUFFER_PTR += EntityVertex.STRIDE;

				EntityVertex.write(BUFFER_PTR, pos1.x, pos1.y, pos1.z, color, uv1.x, uv1.y, overlay1, light1, normal);
				BUFFER_PTR += EntityVertex.STRIDE;

				EntityVertex.write(BUFFER_PTR, pos2.x, pos2.y, pos2.z, color, uv2.x, uv2.y, overlay2, light2, normal);
				BUFFER_PTR += EntityVertex.STRIDE;

				EntityVertex.write(BUFFER_PTR, pos3.x, pos3.y, pos3.z, color, uv3.x, uv3.y, overlay3, light3, normal);
				BUFFER_PTR += EntityVertex.STRIDE;
			}

			BUFFED_VERTEX += 4;
			flush(writer, false, format);
		}

		flush(writer, true, format);
	}

	@Override
	public boolean renderInto(ShadeSeparatingSuperByteBuffer byteBuffer, PoseStack input, VertexConsumer builder) {
		VertexBufferWriter writer = VertexBufferWriter.tryOf(builder);
		if (writer == null) return false;
		if (builder instanceof BufferBuilder bb) {
			if (bb.format == IrisTerrainVertex.FORMAT || bb.format == IrisEntityVertex.FORMAT) {
				IrisRenderInto(byteBuffer, input, writer, bb.format);
			} else if (bb.format == BlockVertex.FORMAT || bb.format == EntityVertex.FORMAT) {
				SodiumRenderInto(byteBuffer, input, writer, bb.format);
			} else {
				return false;
			}
		}

		return true;
	}
}
