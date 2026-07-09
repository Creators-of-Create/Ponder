package net.createmod.catnip.api.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;

import net.createmod.catnip.api.client.platform.ClientFluidHelper;
import net.createmod.catnip.api.data.Iterate;
import net.createmod.catnip.api.platform.services.ModFluidHelper;
import net.createmod.catnip.api.client.render.MultiBufferSource;
import net.minecraft.client.renderer.OrderedSubmitNodeCollector;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Direction;
import net.minecraft.core.TypedInstance;
import net.minecraft.core.Vec3i;
import net.minecraft.util.Mth;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.Vec3;

public final class FluidRenderHelper {
	private FluidRenderHelper() {}

	public static void submitFluidBox(FluidState fluid, float xMin, float yMin, float zMin, float xMax, float yMax, float zMax,
									  OrderedSubmitNodeCollector submitNode, PoseStack ms, int light, boolean renderBottom, boolean invertGasses) {
		submitFluidBox(submitNode, ModFluidHelper.INSTANCE.instanceFor(fluid), xMin, yMin, zMin, xMax, yMax, zMax, ms, light, renderBottom, invertGasses);
	}

	public static void submitFluidBox(OrderedSubmitNodeCollector submitNode, TypedInstance<Fluid> fluid,
									  float xMin, float yMin, float zMin, float xMax, float yMax, float zMax,
									  PoseStack ms, int light, boolean renderBottom, boolean invertGasses) {
		Vec3 center = new Vec3(xMin + (xMax - xMin) / 2, yMin + (yMax - yMin) / 2, zMin + (zMax - zMin) / 2);
		ms.pushPose();
		if (invertGasses && ModFluidHelper.INSTANCE.isLighterThanAir(fluid)) {
			ms.translate(center.x, center.y, center.z);
			ms.mulPose(Axis.XP.rotationDegrees(180));
			ms.translate(-center.x, -center.y, -center.z);
		}

		submitNode.submitCustomGeometry(ms, RenderTypes.translucentMovingBlock(), (pose, vertexConsumer) -> {
			renderFluidBox(fluid, xMin, yMin, zMin, xMax, yMax, zMax, vertexConsumer, pose, light, renderBottom);
		});

		ms.popPose();
	}

	public static void renderFluidBox(FluidState fluid, float xMin, float yMin, float zMin, float xMax, float yMax, float zMax,
									  MultiBufferSource buffer, PoseStack ms, int light, boolean renderBottom, boolean invertGasses) {
		renderFluidBox(fluid, xMin, yMin, zMin, xMax, yMax, zMax, getFluidBuilder(buffer), ms, light, renderBottom, invertGasses);
	}

	public static void renderFluidBox(FluidState fluid, float xMin, float yMin, float zMin, float xMax,
									  float yMax, float zMax, VertexConsumer builder, PoseStack ms, int light, boolean renderBottom, boolean invertGasses) {
		renderFluidBox(ModFluidHelper.INSTANCE.instanceFor(fluid), xMin, yMin, zMin, xMax, yMax, zMax, builder, ms, light, renderBottom, invertGasses);
	}

	public static void renderFluidBox(TypedInstance<Fluid> fluid, float xMin, float yMin, float zMin, float xMax,
									  float yMax, float zMax, MultiBufferSource buffer, PoseStack ms, int light, boolean renderBottom, boolean invertGasses) {
		renderFluidBox(fluid, xMin, yMin, zMin, xMax, yMax, zMax, getFluidBuilder(buffer), ms, light, renderBottom, invertGasses);
	}

	public static void renderFluidBox(TypedInstance<Fluid> fluid, float xMin, float yMin, float zMin, float xMax, float yMax, float zMax,
									  VertexConsumer builder, PoseStack ms, int light, boolean renderBottom, boolean invertGasses) {
		Vec3 center = new Vec3(xMin + (xMax - xMin) / 2, yMin + (yMax - yMin) / 2, zMin + (zMax - zMin) / 2);
		ms.pushPose();
		if (invertGasses && ModFluidHelper.INSTANCE.isLighterThanAir(fluid)) {
			ms.translate(center.x, center.y, center.z);
			ms.mulPose(Axis.XP.rotationDegrees(180));
			ms.translate(-center.x, -center.y, -center.z);
		}

		renderFluidBox(fluid, xMin, yMin, zMin, xMax, yMax, zMax, builder, ms.last(), light, renderBottom);

		ms.popPose();
	}

	private static void renderFluidBox(TypedInstance<Fluid> fluid, float xMin, float yMin, float zMin, float xMax, float yMax, float zMax,
									   VertexConsumer builder, PoseStack.Pose peek, int light, boolean renderBottom) {
		TextureAtlasSprite fluidTexture = ClientFluidHelper.INSTANCE.getStillTextureOrMissing(fluid);
		int color = ClientFluidHelper.INSTANCE.getColor(fluid, null, null);

		int blockLightIn = (light >> 4) & 0xF;
		int luminosity = Math.max(blockLightIn, ModFluidHelper.INSTANCE.getLuminosity(fluid));
		light = (light & 0xF00000) | luminosity << 4;

		for (Direction side : Iterate.directions) {
			if (side == Direction.DOWN && !renderBottom)
				continue;

			boolean positive = side.getAxisDirection() == Direction.AxisDirection.POSITIVE;
			if (side.getAxis()
				.isHorizontal()) {
				if (side.getAxis() == Direction.Axis.X) {
					renderStillTiledFace(side, zMin, yMin, zMax, yMax, positive ? xMax : xMin,
						builder, peek, light, color, fluidTexture);
				} else {
					renderStillTiledFace(side, xMin, yMin, xMax, yMax, positive ? zMax : zMin,
						builder, peek, light, color, fluidTexture);
				}
			} else {
				renderStillTiledFace(side, xMin, zMin, xMax, zMax, positive ? yMax : yMin,
					builder, peek, light, color, fluidTexture);
			}
		}
	}

	public static VertexConsumer getFluidBuilder(MultiBufferSource buffer) {
		return buffer.getBuffer(RenderTypes.translucentMovingBlock());
	}

	public static void renderStillTiledFace(Direction dir, float left, float down, float right, float up,
											float depth, VertexConsumer builder, PoseStack.Pose peek, int light, int color, TextureAtlasSprite texture) {
		renderTiledFace(dir, left, down, right, up, depth, builder, peek, light, color, texture, 1);
	}

	public static void renderTiledFace(Direction dir, float left, float down, float right, float up,
									   float depth, VertexConsumer builder, PoseStack.Pose peek, int light, int color, TextureAtlasSprite texture,
									   float textureScale) {
		boolean positive = dir.getAxisDirection() == Direction.AxisDirection.POSITIVE;
		boolean horizontal = dir.getAxis().isHorizontal();
		boolean x = dir.getAxis() == Direction.Axis.X;

		float f;
		float x2;
		float y2;
		float u1, u2;
		float v1, v2;
		for (float x1 = left; x1 < right; x1 = x2) {
			f = Mth.floor(x1);
			x2 = Math.min(f + 1, right);
			if (dir == Direction.NORTH || dir == Direction.EAST) {
				f = Mth.ceil(x2);
				u1 = texture.getU((f - x2) * textureScale);
				u2 = texture.getU((f - x1) * textureScale);
			} else {
				u1 = texture.getU((x1 - f) * textureScale);
				u2 = texture.getU((x2 - f) * textureScale);
			}

			for (float y1 = down; y1 < up; y1 = y2) {
				f = Mth.floor(y1);
				y2 = Math.min(f + 1, up);
				if (dir == Direction.UP) {
					v1 = texture.getV((y1 - f) * textureScale);
					v2 = texture.getV((y2 - f) * textureScale);
				} else {
					f = Mth.ceil(y2);
					v1 = texture.getV((f - y2) * textureScale);
					v2 = texture.getV((f - y1) * textureScale);
				}

				if (horizontal) {
					if (x) {
						putVertex(builder, peek, depth, y2, positive ? x2 : x1, color, u1, v1, dir, light);
						putVertex(builder, peek, depth, y1, positive ? x2 : x1, color, u1, v2, dir, light);
						putVertex(builder, peek, depth, y1, positive ? x1 : x2, color, u2, v2, dir, light);
						putVertex(builder, peek, depth, y2, positive ? x1 : x2, color, u2, v1, dir, light);
					} else {
						putVertex(builder, peek, positive ? x1 : x2, y2, depth, color, u1, v1, dir, light);
						putVertex(builder, peek, positive ? x1 : x2, y1, depth, color, u1, v2, dir, light);
						putVertex(builder, peek, positive ? x2 : x1, y1, depth, color, u2, v2, dir, light);
						putVertex(builder, peek, positive ? x2 : x1, y2, depth, color, u2, v1, dir, light);
					}
				} else {
					putVertex(builder, peek, x1, depth, positive ? y1 : y2, color, u1, v1, dir, light);
					putVertex(builder, peek, x1, depth, positive ? y2 : y1, color, u1, v2, dir, light);
					putVertex(builder, peek, x2, depth, positive ? y2 : y1, color, u2, v2, dir, light);
					putVertex(builder, peek, x2, depth, positive ? y1 : y2, color, u2, v1, dir, light);
				}
			}
		}
	}

	private static void putVertex(VertexConsumer builder, PoseStack.Pose peek, float x, float y, float z, int color, float u,
								  float v, Direction face, int light) {

		Vec3i normal = face.getUnitVec3i();
		int a = color >> 24 & 0xff;
		int r = color >> 16 & 0xff;
		int g = color >> 8 & 0xff;
		int b = color & 0xff;

		builder.addVertex(peek.pose(), x, y, z)
			.setColor(r, g, b, a)
			.setUv(u, v)
			//.overlayCoords(OverlayTexture.NO_OVERLAY)
			.setLight(light)
			.setNormal(peek.copy(), normal.getX(), normal.getY(), normal.getZ())
		;
	}
}
