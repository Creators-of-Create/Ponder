package net.createmod.catnip.outliner;

import javax.annotation.Nullable;

import net.createmod.catnip.math.AngleHelper;
import net.createmod.catnip.theme.Color;

import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Vector3d;
import org.joml.Vector3f;
import org.joml.Vector4f;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import dev.engine_room.flywheel.lib.transform.TransformStack;
import net.createmod.catnip.render.BindableTexture;
import net.createmod.catnip.render.SuperRenderTypeBuffer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

public abstract class Outline {

	protected final OutlineParams params;

	protected final Vector4f colorTemp = new Vector4f();
	protected final Vector3f diffPosTemp = new Vector3f();
	protected final Vector3f minPosTemp = new Vector3f();
	protected final Vector3f maxPosTemp = new Vector3f();
	protected final Vector4f posTransformTemp = new Vector4f();
	protected final Vector3f normalTransformTemp = new Vector3f();

	public Outline() {
		params = new OutlineParams();
	}

	public OutlineParams getParams() {
		return params;
	}

	public abstract void render(PoseStack ms, SuperRenderTypeBuffer buffer, Vec3 camera, float pt);

	public void tick() {}

	public void bufferCuboidLine(PoseStack poseStack, VertexConsumer consumer, Vec3 camera, Vector3d start, Vector3d end,
								 float width, Vector4f color, int lightmap, boolean disableNormals) {
		Vector3f diff = this.diffPosTemp;
		diff.set((float) (end.x - start.x), (float) (end.y - start.y), (float) (end.z - start.z));

		float length = Mth.sqrt(diff.x() * diff.x() + diff.y() * diff.y() + diff.z() * diff.z());
		float hAngle = AngleHelper.deg(Mth.atan2(diff.x(), diff.z()));
		float hDistance = Mth.sqrt(diff.x() * diff.x() + diff.z() * diff.z());
		float vAngle = AngleHelper.deg(Mth.atan2(hDistance, diff.y())) - 90;

		poseStack.pushPose();
		TransformStack.of(poseStack)
				.translate(start.x - camera.x, start.y - camera.y, start.z - camera.z)
				.rotateYDegrees(hAngle)
				.rotateXDegrees(vAngle);
		bufferCuboidLine(poseStack.last(), consumer, new Vector3f(), Direction.SOUTH, length, width, color, lightmap,
				disableNormals);
		poseStack.popPose();
	}

	public void bufferCuboidLine(PoseStack.Pose pose, VertexConsumer consumer, Vector3f origin, Direction direction,
								 float length, float width, Vector4f color, int lightmap, boolean disableNormals) {
		Vector3f minPos = minPosTemp;
		Vector3f maxPos = maxPosTemp;

		float halfWidth = width / 2;
		minPos.set(origin.x() - halfWidth, origin.y() - halfWidth, origin.z() - halfWidth);
		maxPos.set(origin.x() + halfWidth, origin.y() + halfWidth, origin.z() + halfWidth);

		switch (direction) {
			case DOWN -> {
				minPos.add(0, -length, 0);
			}
			case UP -> {
				maxPos.add(0, length, 0);
			}
			case NORTH -> {
				minPos.add(0, 0, -length);
			}
			case SOUTH -> {
				maxPos.add(0, 0, length);
			}
			case WEST -> {
				minPos.add(-length, 0, 0);
			}
			case EAST -> {
				maxPos.add(length, 0, 0);
			}
		}

		bufferCuboid(pose, consumer, minPos, maxPos, color, lightmap, disableNormals);
	}

	public void bufferCuboid(PoseStack.Pose pose, VertexConsumer consumer, Vector3f minPos, Vector3f maxPos,
							 Vector4f color, int lightmap, boolean disableNormals) {
		Vector4f posTransformTemp = this.posTransformTemp;
		Vector3f normalTransformTemp = this.normalTransformTemp;

		float minX = minPos.x();
		float minY = minPos.y();
		float minZ = minPos.z();
		float maxX = maxPos.x();
		float maxY = maxPos.y();
		float maxZ = maxPos.z();

		Matrix4f posMatrix = pose.pose();

		posTransformTemp.set(minX, minY, maxZ, 1);
		posTransformTemp.mul(posMatrix);
		float x0 = posTransformTemp.x();
		float y0 = posTransformTemp.y();
		float z0 = posTransformTemp.z();

		posTransformTemp.set(minX, minY, minZ, 1);
		posTransformTemp.mul(posMatrix);
		float x1 = posTransformTemp.x();
		float y1 = posTransformTemp.y();
		float z1 = posTransformTemp.z();

		posTransformTemp.set(maxX, minY, minZ, 1);
		posTransformTemp.mul(posMatrix);
		float x2 = posTransformTemp.x();
		float y2 = posTransformTemp.y();
		float z2 = posTransformTemp.z();

		posTransformTemp.set(maxX, minY, maxZ, 1);
		posTransformTemp.mul(posMatrix);
		float x3 = posTransformTemp.x();
		float y3 = posTransformTemp.y();
		float z3 = posTransformTemp.z();

		posTransformTemp.set(minX, maxY, minZ, 1);
		posTransformTemp.mul(posMatrix);
		float x4 = posTransformTemp.x();
		float y4 = posTransformTemp.y();
		float z4 = posTransformTemp.z();

		posTransformTemp.set(minX, maxY, maxZ, 1);
		posTransformTemp.mul(posMatrix);
		float x5 = posTransformTemp.x();
		float y5 = posTransformTemp.y();
		float z5 = posTransformTemp.z();

		posTransformTemp.set(maxX, maxY, maxZ, 1);
		posTransformTemp.mul(posMatrix);
		float x6 = posTransformTemp.x();
		float y6 = posTransformTemp.y();
		float z6 = posTransformTemp.z();

		posTransformTemp.set(maxX, maxY, minZ, 1);
		posTransformTemp.mul(posMatrix);
		float x7 = posTransformTemp.x();
		float y7 = posTransformTemp.y();
		float z7 = posTransformTemp.z();

		float r = color.x();
		float g = color.y();
		float b = color.z();
		float a = color.w();

		Matrix3f normalMatrix = pose.normal();

		// down

		if (disableNormals) {
			normalTransformTemp.set(0, 1, 0);
		} else {
			normalTransformTemp.set(0, -1, 0);
		}
		normalTransformTemp.mul(normalMatrix);
		float nx0 = normalTransformTemp.x();
		float ny0 = normalTransformTemp.y();
		float nz0 = normalTransformTemp.z();

		consumer.addVertex(x0, y0, z0)
				.setColor(r, g, b, a)
				.setUv(0, 0)
				.setOverlay(OverlayTexture.NO_OVERLAY)
				.setLight(lightmap)
				.setNormal(nx0, ny0, nz0)
				;

		consumer.addVertex(x1, y1, z1)
				.setColor(r, g, b, a)
				.setUv(0, 1)
				.setOverlay(OverlayTexture.NO_OVERLAY)
				.setLight(lightmap)
				.setNormal(nx0, ny0, nz0)
				;

		consumer.addVertex(x2, y2, z2)
				.setColor(r, g, b, a)
				.setUv(1, 1)
				.setOverlay(OverlayTexture.NO_OVERLAY)
				.setLight(lightmap)
				.setNormal(nx0, ny0, nz0)
				;

		consumer.addVertex(x3, y3, z3)
				.setColor(r, g, b, a)
				.setUv(1, 0)
				.setOverlay(OverlayTexture.NO_OVERLAY)
				.setLight(lightmap)
				.setNormal(nx0, ny0, nz0)
				;

		// up

		normalTransformTemp.set(0, 1, 0);
		normalTransformTemp.mul(normalMatrix);
		float nx1 = normalTransformTemp.x();
		float ny1 = normalTransformTemp.y();
		float nz1 = normalTransformTemp.z();

		consumer.addVertex(x4, y4, z4)
				.setColor(r, g, b, a)
				.setUv(0, 0)
				.setOverlay(OverlayTexture.NO_OVERLAY)
				.setLight(lightmap)
				.setNormal(nx1, ny1, nz1)
				;

		consumer.addVertex(x5, y5, z5)
				.setColor(r, g, b, a)
				.setUv(0, 1)
				.setOverlay(OverlayTexture.NO_OVERLAY)
				.setLight(lightmap)
				.setNormal(nx1, ny1, nz1)
				;

		consumer.addVertex(x6, y6, z6)
				.setColor(r, g, b, a)
				.setUv(1, 1)
				.setOverlay(OverlayTexture.NO_OVERLAY)
				.setLight(lightmap)
				.setNormal(nx1, ny1, nz1)
				;

		consumer.addVertex(x7, y7, z7)
				.setColor(r, g, b, a)
				.setUv(1, 0)
				.setOverlay(OverlayTexture.NO_OVERLAY)
				.setLight(lightmap)
				.setNormal(nx1, ny1, nz1)
				;

		// north

		if (disableNormals) {
			normalTransformTemp.set(0, 1, 0);
		} else {
			normalTransformTemp.set(0, 0, -1);
		}
		normalTransformTemp.mul(normalMatrix);
		float nx2 = normalTransformTemp.x();
		float ny2 = normalTransformTemp.y();
		float nz2 = normalTransformTemp.z();

		consumer.addVertex(x7, y7, z7)
				.setColor(r, g, b, a)
				.setUv(0, 0)
				.setOverlay(OverlayTexture.NO_OVERLAY)
				.setLight(lightmap)
				.setNormal(nx2, ny2, nz2)
				;

		consumer.addVertex(x2, y2, z2)
				.setColor(r, g, b, a)
				.setUv(0, 1)
				.setOverlay(OverlayTexture.NO_OVERLAY)
				.setLight(lightmap)
				.setNormal(nx2, ny2, nz2)
				;

		consumer.addVertex(x1, y1, z1)
				.setColor(r, g, b, a)
				.setUv(1, 1)
				.setOverlay(OverlayTexture.NO_OVERLAY)
				.setLight(lightmap)
				.setNormal(nx2, ny2, nz2)
				;

		consumer.addVertex(x4, y4, z4)
				.setColor(r, g, b, a)
				.setUv(1, 0)
				.setOverlay(OverlayTexture.NO_OVERLAY)
				.setLight(lightmap)
				.setNormal(nx2, ny2, nz2)
				;

		// south

		if (disableNormals) {
			normalTransformTemp.set(0, 1, 0);
		} else {
			normalTransformTemp.set(0, 0, 1);
		}
		normalTransformTemp.mul(normalMatrix);
		float nx3 = normalTransformTemp.x();
		float ny3 = normalTransformTemp.y();
		float nz3 = normalTransformTemp.z();

		consumer.addVertex(x5, y5, z5)
				.setColor(r, g, b, a)
				.setUv(0, 0)
				.setOverlay(OverlayTexture.NO_OVERLAY)
				.setLight(lightmap)
				.setNormal(nx3, ny3, nz3)
				;

		consumer.addVertex(x0, y0, z0)
				.setColor(r, g, b, a)
				.setUv(0, 1)
				.setOverlay(OverlayTexture.NO_OVERLAY)
				.setLight(lightmap)
				.setNormal(nx3, ny3, nz3)
				;

		consumer.addVertex(x3, y3, z3)
				.setColor(r, g, b, a)
				.setUv(1, 1)
				.setOverlay(OverlayTexture.NO_OVERLAY)
				.setLight(lightmap)
				.setNormal(nx3, ny3, nz3)
				;

		consumer.addVertex(x6, y6, z6)
				.setColor(r, g, b, a)
				.setUv(1, 0)
				.setOverlay(OverlayTexture.NO_OVERLAY)
				.setLight(lightmap)
				.setNormal(nx3, ny3, nz3)
				;

		// west

		if (disableNormals) {
			normalTransformTemp.set(0, 1, 0);
		} else {
			normalTransformTemp.set(-1, 0, 0);
		}
		normalTransformTemp.mul(normalMatrix);
		float nx4 = normalTransformTemp.x();
		float ny4 = normalTransformTemp.y();
		float nz4 = normalTransformTemp.z();

		consumer.addVertex(x4, y4, z4)
				.setColor(r, g, b, a)
				.setUv(0, 0)
				.setOverlay(OverlayTexture.NO_OVERLAY)
				.setLight(lightmap)
				.setNormal(nx4, ny4, nz4)
				;

		consumer.addVertex(x1, y1, z1)
				.setColor(r, g, b, a)
				.setUv(0, 1)
				.setOverlay(OverlayTexture.NO_OVERLAY)
				.setLight(lightmap)
				.setNormal(nx4, ny4, nz4)
				;

		consumer.addVertex(x0, y0, z0)
				.setColor(r, g, b, a)
				.setUv(1, 1)
				.setOverlay(OverlayTexture.NO_OVERLAY)
				.setLight(lightmap)
				.setNormal(nx4, ny4, nz4)
				;

		consumer.addVertex(x5, y5, z5)
				.setColor(r, g, b, a)
				.setUv(1, 0)
				.setOverlay(OverlayTexture.NO_OVERLAY)
				.setLight(lightmap)
				.setNormal(nx4, ny4, nz4)
				;

		// east

		if (disableNormals) {
			normalTransformTemp.set(0, 1, 0);
		} else {
			normalTransformTemp.set(1, 0, 0);
		}
		normalTransformTemp.mul(normalMatrix);
		float nx5 = normalTransformTemp.x();
		float ny5 = normalTransformTemp.y();
		float nz5 = normalTransformTemp.z();

		consumer.addVertex(x6, y6, z6)
				.setColor(r, g, b, a)
				.setUv(0, 0)
				.setOverlay(OverlayTexture.NO_OVERLAY)
				.setLight(lightmap)
				.setNormal(nx5, ny5, nz5)
				;

		consumer.addVertex(x3, y3, z3)
				.setColor(r, g, b, a)
				.setUv(0, 1)
				.setOverlay(OverlayTexture.NO_OVERLAY)
				.setLight(lightmap)
				.setNormal(nx5, ny5, nz5)
				;

		consumer.addVertex(x2, y2, z2)
				.setColor(r, g, b, a)
				.setUv(1, 1)
				.setOverlay(OverlayTexture.NO_OVERLAY)
				.setLight(lightmap)
				.setNormal(nx5, ny5, nz5)
				;

		consumer.addVertex(x7, y7, z7)
				.setColor(r, g, b, a)
				.setUv(1, 0)
				.setOverlay(OverlayTexture.NO_OVERLAY)
				.setLight(lightmap)
				.setNormal(nx5, ny5, nz5)
				;
	}

	public void bufferQuad(PoseStack.Pose pose, VertexConsumer consumer, Vector3f pos0, Vector3f pos1, Vector3f pos2,
						   Vector3f pos3, Vector4f color, int lightmap, Vector3f normal) {
		bufferQuad(pose, consumer, pos0, pos1, pos2, pos3, color, 0, 0, 1, 1, lightmap, normal);
	}

	public void bufferQuad(PoseStack.Pose pose, VertexConsumer consumer, Vector3f pos0, Vector3f pos1, Vector3f pos2,
						   Vector3f pos3, Vector4f color, float minU, float minV, float maxU, float maxV, int lightmap, Vector3f normal) {
		Vector4f posTransformTemp = this.posTransformTemp;
		Vector3f normalTransformTemp = this.normalTransformTemp;

		Matrix4f posMatrix = pose.pose();

		posTransformTemp.set(pos0.x(), pos0.y(), pos0.z(), 1);
		posTransformTemp.mul(posMatrix);
		float x0 = posTransformTemp.x();
		float y0 = posTransformTemp.y();
		float z0 = posTransformTemp.z();

		posTransformTemp.set(pos1.x(), pos1.y(), pos1.z(), 1);
		posTransformTemp.mul(posMatrix);
		float x1 = posTransformTemp.x();
		float y1 = posTransformTemp.y();
		float z1 = posTransformTemp.z();

		posTransformTemp.set(pos2.x(), pos2.y(), pos2.z(), 1);
		posTransformTemp.mul(posMatrix);
		float x2 = posTransformTemp.x();
		float y2 = posTransformTemp.y();
		float z2 = posTransformTemp.z();

		posTransformTemp.set(pos3.x(), pos3.y(), pos3.z(), 1);
		posTransformTemp.mul(posMatrix);
		float x3 = posTransformTemp.x();
		float y3 = posTransformTemp.y();
		float z3 = posTransformTemp.z();

		float r = color.x();
		float g = color.y();
		float b = color.z();
		float a = color.w();

		normalTransformTemp.set(normal);
		normalTransformTemp.mul(pose.normal());
		float nx = normalTransformTemp.x();
		float ny = normalTransformTemp.y();
		float nz = normalTransformTemp.z();

		consumer.addVertex(x0, y0, z0)
				.setColor(r, g, b, a)
				.setUv(minU, minV)
				.setOverlay(OverlayTexture.NO_OVERLAY)
				.setLight(lightmap)
				.setNormal(nx, ny, nz)
				;

		consumer.addVertex(x1, y1, z1)
				.setColor(r, g, b, a)
				.setUv(minU, maxV)
				.setOverlay(OverlayTexture.NO_OVERLAY)
				.setLight(lightmap)
				.setNormal(nx, ny, nz)
				;

		consumer.addVertex(x2, y2, z2)
				.setColor(r, g, b, a)
				.setUv(maxU, maxV)
				.setOverlay(OverlayTexture.NO_OVERLAY)
				.setLight(lightmap)
				.setNormal(nx, ny, nz)
				;

		consumer.addVertex(x3, y3, z3)
				.setColor(r, g, b, a)
				.setUv(maxU, minV)
				.setOverlay(OverlayTexture.NO_OVERLAY)
				.setLight(lightmap)
				.setNormal(nx, ny, nz)
				;
	}

	public static class OutlineParams {
		@Nullable protected BindableTexture faceTexture;
		@Nullable protected BindableTexture highlightedFaceTexture;
		@Nullable Direction highlightedFace;
		protected boolean fadeLineWidth;
		protected boolean disableCull;
		protected boolean disableLineNormals;
		protected float alpha;
		protected int lightmap;
		protected Color rgb;
		private float lineWidth;

		public OutlineParams() {
			faceTexture = highlightedFaceTexture = null;
			alpha = 1;
			lineWidth = 1 / 32f;
			fadeLineWidth = true;
			rgb = Color.WHITE;
			lightmap = LightTexture.FULL_BRIGHT;
		}

		// builder

		public OutlineParams colored(int color) {
			rgb = new Color(color, false);
			return this;
		}

		public OutlineParams colored(Color c) {
			rgb = c.copy();
			return this;
		}

		public OutlineParams lightmap(int light) {
			lightmap = light;
			return this;
		}

		public OutlineParams lineWidth(float width) {
			this.lineWidth = width;
			return this;
		}

		public OutlineParams withFaceTexture(@Nullable BindableTexture texture) {
			this.faceTexture = texture;
			return this;
		}

		public OutlineParams clearTextures() {
			return this.withFaceTextures(null, null);
		}

		public OutlineParams withFaceTextures(@Nullable BindableTexture texture, @Nullable BindableTexture highlightTexture) {
			this.faceTexture = texture;
			this.highlightedFaceTexture = highlightTexture;
			return this;
		}

		public OutlineParams highlightFace(@Nullable Direction face) {
			highlightedFace = face;
			return this;
		}

		public OutlineParams disableLineNormals() {
			disableLineNormals = true;
			return this;
		}

		public OutlineParams disableCull() {
			disableCull = true;
			return this;
		}

		// getter

		public float getLineWidth() {
			return fadeLineWidth ? alpha * lineWidth : lineWidth;
		}

		@Nullable public Direction getHighlightedFace() {
			return highlightedFace;
		}

		public void loadColor(Vector4f vec) {
			vec.set(rgb.getRedAsFloat(), rgb.getGreenAsFloat(), rgb.getBlueAsFloat(), rgb.getAlphaAsFloat() * alpha);
		}
	}

}
