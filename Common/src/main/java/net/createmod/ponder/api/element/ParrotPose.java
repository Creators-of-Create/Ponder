package net.createmod.ponder.api.element;

import com.mojang.blaze3d.platform.Window;

import net.createmod.catnip.utility.math.AngleHelper;
import net.createmod.ponder.Ponder;
import net.createmod.ponder.api.level.PonderLevel;
import net.createmod.ponder.foundation.PonderScene;
import net.createmod.ponder.foundation.ui.PonderUI;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.Parrot;
import net.minecraft.world.phys.Vec3;

public abstract class ParrotPose {

	private static final Parrot.Variant[] VARIANTS = new Parrot.Variant[]{
		Parrot.Variant.RED_BLUE,
		Parrot.Variant.GREEN,
		Parrot.Variant.YELLOW_BLUE,
		Parrot.Variant.GRAY,
	}; // blue parrots are kinda hard to see

	public abstract void tick(PonderScene scene, Parrot entity, Vec3 location);

	public Parrot create(PonderLevel world) {
		Parrot entity = new Parrot(EntityType.PARROT, world);
		int nextInt = Ponder.RANDOM.nextInt(VARIANTS.length);
		entity.setVariant(VARIANTS[nextInt]);
		return entity;
	}

	public static class DancePose extends ParrotPose {

		@Override
		public Parrot create(PonderLevel world) {
			Parrot entity = super.create(world);
			entity.setRecordPlayingNearby(BlockPos.ZERO, true);
			return entity;
		}

		@Override
		public void tick(PonderScene scene, Parrot entity, Vec3 location) {
			entity.yRotO = entity.getYRot();
			entity.setYRot(entity.getYRot() - 2);
		}

	}

	public static class FlappyPose extends ParrotPose {

		@Override
		public void tick(PonderScene scene, Parrot entity, Vec3 location) {
			double length = entity.position()
				.subtract(entity.xOld, entity.yOld, entity.zOld)
				.length();
			entity.setOnGround(false);
			double phase = Math.min(length * 15, 8);
			float f = (float) ((PonderUI.ponderTicks % 100) * phase);
			entity.flapSpeed = Mth.sin(f) + 1;
			if (length == 0)
				entity.flapSpeed = 0;
		}

	}

	public static abstract class FaceVecPose extends ParrotPose {

		@Override
		public void tick(PonderScene scene, Parrot entity, Vec3 location) {
			Vec3 p_200602_2_ = getFacedVec(scene);
			Vec3 Vector3d = location.add(entity.getEyePosition(0));
			double d0 = p_200602_2_.x - Vector3d.x;
			double d1 = p_200602_2_.y - Vector3d.y;
			double d2 = p_200602_2_.z - Vector3d.z;
			double d3 = Mth.sqrt((float) (d0 * d0 + d2 * d2));
			float targetPitch = Mth.wrapDegrees((float) -(Mth.atan2(d1, d3) * (double) (180F / (float) Math.PI)));
			float targetYaw = Mth.wrapDegrees((float) -(Mth.atan2(d2, d0) * (double) (180F / (float) Math.PI)) + 90);

			entity.setXRot(AngleHelper.angleLerp(.4f, entity.getXRot(), targetPitch));
			entity.setYRot(AngleHelper.angleLerp(.4f, entity.getYRot(), targetYaw));
		}

		protected abstract Vec3 getFacedVec(PonderScene scene);

	}

	public static class FacePointOfInterestPose extends FaceVecPose {

		@Override
		protected Vec3 getFacedVec(PonderScene scene) {
			return scene.getPointOfInterest();
		}

	}

	public static class FaceCursorPose extends FaceVecPose {

		@Override
		protected Vec3 getFacedVec(PonderScene scene) {
			Minecraft minecraft = Minecraft.getInstance();
			Window w = minecraft.getWindow();
			double mouseX = minecraft.mouseHandler.xpos() * w.getGuiScaledWidth() / w.getScreenWidth();
			double mouseY = minecraft.mouseHandler.ypos() * w.getGuiScaledHeight() / w.getScreenHeight();
			return scene.getTransform()
				.screenToScene(mouseX, mouseY, 300, 0);
		}

	}
}
