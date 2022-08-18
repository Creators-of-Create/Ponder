package net.createmod.catnip.utility.outliner;

import java.util.Optional;

import javax.annotation.Nullable;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Vector3f;

import net.createmod.catnip.render.BindableTexture;
import net.createmod.catnip.render.CatnipRenderTypes;
import net.createmod.catnip.render.SuperRenderTypeBuffer;
import net.createmod.catnip.utility.VecHelper;
import net.createmod.catnip.utility.math.AngleHelper;
import net.createmod.catnip.utility.theme.Color;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

public abstract class Outline {

	protected OutlineParams params;

	public Outline() {
		params = new OutlineParams();
	}

	public abstract void render(PoseStack ms, SuperRenderTypeBuffer buffer, float pt);

	public void tick() {}

	public OutlineParams getParams() {
		return params;
	}

	public void renderCuboidLine(PoseStack ms, SuperRenderTypeBuffer buffer, Vec3 start, Vec3 end) {
		Vec3 diff = end.subtract(start);
		float hAngle = AngleHelper.deg(Mth.atan2(diff.x, diff.z));
		float hDistance = (float) diff.multiply(1, 0, 1)
			.length();
		float vAngle = AngleHelper.deg(Mth.atan2(hDistance, diff.y)) - 90;
		ms.pushPose();
		ms.translate(start.x, start.y, start.z);
		ms.mulPose(Vector3f.YP.rotationDegrees(hAngle));
		ms.mulPose(Vector3f.XP.rotationDegrees(vAngle));
		renderAACuboidLine(ms, buffer, Vec3.ZERO, new Vec3(0, 0, diff.length()));
		ms.popPose();
	}

	public void renderAACuboidLine(PoseStack ms, SuperRenderTypeBuffer buffer, Vec3 start, Vec3 end) {
		float lineWidth = params.getLineWidth();
		if (lineWidth == 0)
			return;

		VertexConsumer builder = buffer.getBuffer(CatnipRenderTypes.getOutlineSolid());

		Vec3 diff = end.subtract(start);
		if (diff.x + diff.y + diff.z < 0) {
			Vec3 temp = start;
			start = end;
			end = temp;
			diff = diff.scale(-1);
		}

		Vec3 extension = diff.normalize()
			.scale(lineWidth / 2);
		Vec3 plane = VecHelper.axisAlingedPlaneOf(diff);
		Direction face = Direction.getNearest(diff.x, diff.y, diff.z);
		Axis axis = face.getAxis();

		start = start.subtract(extension);
		end = end.add(extension);
		plane = plane.scale(lineWidth / 2);

		Vec3 a1 = plane.add(start);
		Vec3 b1 = plane.add(end);
		plane = VecHelper.rotate(plane, -90, axis);
		Vec3 a2 = plane.add(start);
		Vec3 b2 = plane.add(end);
		plane = VecHelper.rotate(plane, -90, axis);
		Vec3 a3 = plane.add(start);
		Vec3 b3 = plane.add(end);
		plane = VecHelper.rotate(plane, -90, axis);
		Vec3 a4 = plane.add(start);
		Vec3 b4 = plane.add(end);

		if (params.disableNormals) {
			face = Direction.UP;
			putQuad(ms, builder, b4, b3, b2, b1, face);
			putQuad(ms, builder, a1, a2, a3, a4, face);
			putQuad(ms, builder, a1, b1, b2, a2, face);
			putQuad(ms, builder, a2, b2, b3, a3, face);
			putQuad(ms, builder, a3, b3, b4, a4, face);
			putQuad(ms, builder, a4, b4, b1, a1, face);
			return;
		}

		putQuad(ms, builder, b4, b3, b2, b1, face);
		putQuad(ms, builder, a1, a2, a3, a4, face.getOpposite());
		Vec3 vec = a1.subtract(a4);
		face = Direction.getNearest(vec.x, vec.y, vec.z);
		putQuad(ms, builder, a1, b1, b2, a2, face);
		vec = VecHelper.rotate(vec, -90, axis);
		face = Direction.getNearest(vec.x, vec.y, vec.z);
		putQuad(ms, builder, a2, b2, b3, a3, face);
		vec = VecHelper.rotate(vec, -90, axis);
		face = Direction.getNearest(vec.x, vec.y, vec.z);
		putQuad(ms, builder, a3, b3, b4, a4, face);
		vec = VecHelper.rotate(vec, -90, axis);
		face = Direction.getNearest(vec.x, vec.y, vec.z);
		putQuad(ms, builder, a4, b4, b1, a1, face);
	}

	public void putQuad(PoseStack ms, VertexConsumer builder, Vec3 v1, Vec3 v2, Vec3 v3, Vec3 v4,
		Direction normal) {
		putQuadUV(ms, builder, v1, v2, v3, v4, 0, 0, 1, 1, normal);
	}

	public void putQuadUV(PoseStack ms, VertexConsumer builder, Vec3 v1, Vec3 v2, Vec3 v3, Vec3 v4, float minU,
		float minV, float maxU, float maxV, Direction normal) {
		putVertex(ms, builder, v1, minU, minV, normal);
		putVertex(ms, builder, v2, maxU, minV, normal);
		putVertex(ms, builder, v3, maxU, maxV, normal);
		putVertex(ms, builder, v4, minU, maxV, normal);
	}

	protected void putVertex(PoseStack ms, VertexConsumer builder, Vec3 pos, float u, float v, Direction normal) {
		putVertex(ms.last(), builder, (float) pos.x, (float) pos.y, (float) pos.z, u, v, normal);
	}

	protected void putVertex(PoseStack.Pose pose, VertexConsumer builder, float x, float y, float z, float u, float v, Direction normal) {
		Color rgb = params.rgb;

		builder.vertex(pose.pose(), x, y, z)
			.color(rgb.getRed(), rgb.getGreen(), rgb.getBlue(), ((int) (rgb.getAlpha() * params.alpha)))
			.uv(u, v)
			.overlayCoords(OverlayTexture.NO_OVERLAY)
			.uv2(params.lightMap)
			.normal(pose.normal(), normal.getStepX(), normal.getStepY(), normal.getStepZ())
			.endVertex();
	}

	public static class OutlineParams {

		@Nullable
		protected BindableTexture faceTexture = null;
		@Nullable
		protected BindableTexture highlightedFaceTexture = null;
		@Nullable
		protected Direction highlightedFace;
		protected boolean fadeLineWidth;
		protected boolean disableCull;
		protected boolean disableNormals;
		protected float alpha;
		protected int lightMap;
		protected Color rgb;
		private float lineWidth;

		public OutlineParams() {
			alpha = 1;
			lineWidth = 1 / 32f;
			fadeLineWidth = true;
			rgb = Color.WHITE;
			lightMap = LightTexture.FULL_BRIGHT;
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

		public OutlineParams lightMap(int light) {
			lightMap = light;
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

		public OutlineParams disableNormals() {
			disableNormals = true;
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

		@Nullable
		public Direction getHighlightedFace() {
			return highlightedFace;
		}

		public Optional<BindableTexture> getFaceTexture() {
			return Optional.ofNullable(faceTexture);
		}

		public Optional<BindableTexture> getHighlightedFaceTexture() {
			return Optional.ofNullable(highlightedFaceTexture);
		}
	}

}
