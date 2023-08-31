package net.createmod.catnip.utility;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.core.Vec3i;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.lwjgl.system.MemoryStack;

import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;

/*
 * https://github.com/Fabricators-of-Create/Porting-Lib/blob/2b136dfa25d1b364c15dd2cca1a4fefb3050c4fc/src/main/java/io/github/fabricators_of_create/porting_lib/util/client/VertexUtils.java
 * */
public class VertexUtils {
	// Copy of putBulkData, but enables tinting and per-vertex alpha
	public static void putBulkData(VertexConsumer builder, PoseStack.Pose poseStack, BakedQuad bakedQuad, float red, float green, float blue, int packedLight, int packedOverlay, boolean readExistingColor) {
		putBulkData(builder, poseStack, bakedQuad, red, green, blue, 1.0f, packedLight, packedOverlay, readExistingColor);
	}

	// Copy of putBulkData with alpha support
	public static void putBulkData(VertexConsumer builder, PoseStack.Pose pose, BakedQuad bakedQuad, float red, float green, float blue, float alpha, int packedLight, int packedOverlay) {
		putBulkData(builder, pose, bakedQuad, new float[]{1.0F, 1.0F, 1.0F, 1.0F}, red, green, blue, alpha, new int[]{packedLight, packedLight, packedLight, packedLight}, packedOverlay, false);
	}

	// Copy of putBulkData with alpha support
	public static void putBulkData(VertexConsumer builder, PoseStack.Pose pose, BakedQuad bakedQuad, float red, float green, float blue, float alpha, int packedLight, int packedOverlay, boolean readExistingColor) {
		putBulkData(builder, pose, bakedQuad, new float[]{1.0F, 1.0F, 1.0F, 1.0F}, red, green, blue, alpha, new int[]{packedLight, packedLight, packedLight, packedLight}, packedOverlay, readExistingColor);
	}

	// Copy of putBulkData with alpha support
	public static void putBulkData(VertexConsumer builder, PoseStack.Pose pose, BakedQuad bakedQuad, float[] baseBrightness, float red, float green, float blue, float alpha, int[] lightmap, int packedOverlay, boolean readExistingColor) {
		int[] aint = bakedQuad.getVertices();
		Vec3i faceNormal = bakedQuad.getDirection().getNormal();
		Vector3f normal = new Vector3f((float)faceNormal.getX(), (float)faceNormal.getY(), (float)faceNormal.getZ());
		Matrix4f matrix4f = pose.pose();
		normal.mul(pose.normal());
		int intSize = DefaultVertexFormat.BLOCK.getIntegerSize();
		int vertexCount = aint.length / intSize;

		try (MemoryStack memorystack = MemoryStack.stackPush()) {
			ByteBuffer bytebuffer = memorystack.malloc(DefaultVertexFormat.BLOCK.getVertexSize());
			IntBuffer intbuffer = bytebuffer.asIntBuffer();

			for(int v = 0; v < vertexCount; ++v) {
				((Buffer)intbuffer).clear();
				intbuffer.put(aint, v * 8, 8);
				float f = bytebuffer.getFloat(0);
				float f1 = bytebuffer.getFloat(4);
				float f2 = bytebuffer.getFloat(8);
				float cr;
				float cg;
				float cb;
				float ca;
				if (readExistingColor) {
					float r = (float)(bytebuffer.get(12) & 255) / 255.0F;
					float g = (float)(bytebuffer.get(13) & 255) / 255.0F;
					float b = (float)(bytebuffer.get(14) & 255) / 255.0F;
					float a = (float)(bytebuffer.get(15) & 255) / 255.0F;
					cr = r * baseBrightness[v] * red;
					cg = g * baseBrightness[v] * green;
					cb = b * baseBrightness[v] * blue;
					ca = a * alpha;
				} else {
					cr = baseBrightness[v] * red;
					cg = baseBrightness[v] * green;
					cb = baseBrightness[v] * blue;
					ca = alpha;
				}

				int lightmapCoord = applyBakedLighting(lightmap[v], bytebuffer);
				float f9 = bytebuffer.getFloat(16);
				float f10 = bytebuffer.getFloat(20);
				Vector4f pos = new Vector4f(f, f1, f2, 1.0F);
				pos.mul(matrix4f);
				applyBakedNormals(normal, bytebuffer, pose.normal());
				builder.vertex(pos.x(), pos.y(), pos.z(), cr, cg, cb, ca, f9, f10, packedOverlay, lightmapCoord, normal.x(), normal.y(), normal.z());
			}
		}
	}

	public static int applyBakedLighting(int packedLight, ByteBuffer data) {
		int bl = packedLight&0xFFFF;
		int sl = (packedLight>>16)&0xFFFF;
		int offset = 6 * 4; // int offset for vertex 0 * 4 bytes per int
		int blBaked = Short.toUnsignedInt(data.getShort(offset));
		int slBaked = Short.toUnsignedInt(data.getShort(offset + 2));
		bl = Math.max(bl, blBaked);
		sl = Math.max(sl, slBaked);
		return bl | (sl<<16);
	}

	public static void applyBakedNormals(Vector3f generated, ByteBuffer data, Matrix3f normalTransform) {
		byte nx = data.get(28);
		byte ny = data.get(29);
		byte nz = data.get(30);
		if (nx != 0 || ny != 0 || nz != 0) {
			generated.set(nx / 127f, ny / 127f, nz / 127f);
			generated.mul(normalTransform);
		}
	}
}
