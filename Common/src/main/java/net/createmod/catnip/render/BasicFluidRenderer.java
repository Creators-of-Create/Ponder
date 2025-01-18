package net.createmod.catnip.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;

import net.createmod.catnip.platform.CatnipServices;
import net.createmod.catnip.data.Iterate;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.util.Mth;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.phys.Vec3;

import org.jetbrains.annotations.Nullable;

public class BasicFluidRenderer {

	public static VertexConsumer getFluidBuilder(MultiBufferSource buffer) {
		return buffer.getBuffer(PonderRenderTypes.fluid());
	}

	public static void renderFluidBox(Fluid fluid, long amount, float xMin, float yMin, float zMin, float xMax,
									  float yMax, float zMax, MultiBufferSource buffer, PoseStack ms, int light, boolean renderBottom, boolean invertGasses) {
		renderFluidBox(fluid, amount, xMin, yMin, zMin, xMax, yMax, zMax, getFluidBuilder(buffer), ms, light, renderBottom, invertGasses, null);
	}

	public static void renderFluidBox(Fluid fluid, long amount, float xMin, float yMin, float zMin, float xMax,
									  float yMax, float zMax, VertexConsumer builder, PoseStack ms, int light, boolean renderBottom, boolean invertGasses) {
		renderFluidBox(fluid, amount, xMin, yMin, zMin, xMax, yMax, zMax, builder, ms, light, renderBottom, invertGasses, null);
	}

	public static void renderFluidBox(Fluid fluid, long amount, float xMin, float yMin, float zMin, float xMax,
									  float yMax, float zMax, MultiBufferSource buffer, PoseStack ms, int light, boolean renderBottom, boolean invertGasses, @Nullable DataComponentPatch fluidData) {
		renderFluidBox(fluid, amount, xMin, yMin, zMin, xMax, yMax, zMax, getFluidBuilder(buffer), ms, light, renderBottom, invertGasses, fluidData);
	}

	public static void renderFluidBox(Fluid fluid, long amount, float xMin, float yMin, float zMin, float xMax,
									  float yMax, float zMax, VertexConsumer builder, PoseStack ms, int light, boolean renderBottom, boolean invertGasses, @Nullable DataComponentPatch fluidData) {
		TextureAtlasSprite fluidTexture = Minecraft.getInstance()
			.getTextureAtlas(InventoryMenu.BLOCK_ATLAS)
			.apply(CatnipServices.FLUID_HELPER.getStillTexture(fluid, amount, fluidData));

		int color = CatnipServices.FLUID_HELPER.getColor(fluid, amount, fluidData);
		int blockLightIn = (light >> 4) & 0xF;
		int luminosity = Math.max(blockLightIn, CatnipServices.FLUID_HELPER.getLuminosity(fluid, amount, fluidData));
		light = (light & 0xF00000) | luminosity << 4;

		Vec3 center = new Vec3(xMin + (xMax - xMin) / 2, yMin + (yMax - yMin) / 2, zMin + (zMax - zMin) / 2);
		ms.pushPose();
		if (invertGasses && CatnipServices.FLUID_HELPER.isLighterThanAir(fluid)) {
			ms.translate(center.x, center.y, center.z);
			ms.mulPose(Axis.XP.rotationDegrees(180));
			ms.translate(-center.x, -center.y, -center.z);
		}

		for (Direction side : Iterate.directions) {
			if (side == Direction.DOWN && !renderBottom)
				continue;

			boolean positive = side.getAxisDirection() == Direction.AxisDirection.POSITIVE;
			if (side.getAxis()
				.isHorizontal()) {
				if (side.getAxis() == Direction.Axis.X) {
					renderStillTiledFace(side, zMin, yMin, zMax, yMax, positive ? xMax : xMin,
						builder, ms, light, color, fluidTexture);
				} else {
					renderStillTiledFace(side, xMin, yMin, xMax, yMax, positive ? zMax : zMin,
						builder, ms, light, color, fluidTexture);
				}
			} else {
				renderStillTiledFace(side, xMin, zMin, xMax, zMax, positive ? yMax : yMin,
					builder, ms, light, color, fluidTexture);
			}
		}

		ms.popPose();
	}

	public static void renderStillTiledFace(Direction dir, float left, float down, float right, float up,
											float depth, VertexConsumer builder, PoseStack ms, int light, int color, TextureAtlasSprite texture) {
		renderTiledFace(dir, left, down, right, up, depth, builder, ms, light, color, texture, 1);
	}

	public static void renderTiledFace(Direction dir, float left, float down, float right, float up,
									   float depth, VertexConsumer builder, PoseStack ms, int light, int color, TextureAtlasSprite texture,
									   float textureScale) {
		boolean positive = dir.getAxisDirection() == Direction.AxisDirection.POSITIVE;
		boolean horizontal = dir.getAxis().isHorizontal();
		boolean x = dir.getAxis() == Direction.Axis.X;

		float shrink = texture.uvShrinkRatio() * 0.25f * textureScale;
		float centerU = texture.getU0() + (texture.getU1() - texture.getU0()) * 0.5f * textureScale;
		float centerV = texture.getV0() + (texture.getV1() - texture.getV0()) * 0.5f * textureScale;

		float f;
		float x2 = 0;
		float y2 = 0;
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
			u1 = Mth.lerp(shrink, u1, centerU);
			u2 = Mth.lerp(shrink, u2, centerU);
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
				v1 = Mth.lerp(shrink, v1, centerV);
				v2 = Mth.lerp(shrink, v2, centerV);

				if (horizontal) {
					if (x) {
						putVertex(builder, ms, depth, y2, positive ? x2 : x1, color, u1, v1, dir, light);
						putVertex(builder, ms, depth, y1, positive ? x2 : x1, color, u1, v2, dir, light);
						putVertex(builder, ms, depth, y1, positive ? x1 : x2, color, u2, v2, dir, light);
						putVertex(builder, ms, depth, y2, positive ? x1 : x2, color, u2, v1, dir, light);
					} else {
						putVertex(builder, ms, positive ? x1 : x2, y2, depth, color, u1, v1, dir, light);
						putVertex(builder, ms, positive ? x1 : x2, y1, depth, color, u1, v2, dir, light);
						putVertex(builder, ms, positive ? x2 : x1, y1, depth, color, u2, v2, dir, light);
						putVertex(builder, ms, positive ? x2 : x1, y2, depth, color, u2, v1, dir, light);
					}
				} else {
					putVertex(builder, ms, x1, depth, positive ? y1 : y2, color, u1, v1, dir, light);
					putVertex(builder, ms, x1, depth, positive ? y2 : y1, color, u1, v2, dir, light);
					putVertex(builder, ms, x2, depth, positive ? y2 : y1, color, u2, v2, dir, light);
					putVertex(builder, ms, x2, depth, positive ? y1 : y2, color, u2, v1, dir, light);
				}
			}
		}
	}

	protected static void putVertex(VertexConsumer builder, PoseStack ms, float x, float y, float z, int color, float u,
									float v, Direction face, int light) {

		Vec3i normal = face.getNormal();
		PoseStack.Pose peek = ms.last();
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

