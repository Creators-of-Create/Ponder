package net.createmod.ponder.foundation;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import javax.annotation.Nullable;

import net.createmod.catnip.outliner.Outline.OutlineParams;
import net.createmod.catnip.outliner.Outliner;
import net.createmod.ponder.api.scene.Selection;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class SelectionImpl {

	public static Selection of(BoundingBox bb) {
		return new Simple(bb);
	}

	private static class Compound implements Selection {
		private final Set<BlockPos> posSet;
		@Nullable
		private Vec3 center;

		public Compound(Simple initial) {
			posSet = new HashSet<>();
			add(initial);
		}

		private Compound(Set<BlockPos> template) {
			posSet = new HashSet<>(template);
		}

		@Override
		public boolean test(BlockPos t) {
			return posSet.contains(t);
		}

		@Override
		public Selection add(Selection other) {
			other.forEach(p -> posSet.add(p.immutable()));
			center = null;
			return this;
		}

		@Override
		public Selection substract(Selection other) {
			other.forEach(p -> posSet.remove(p.immutable()));
			center = null;
			return this;
		}

		@Override
		public OutlineParams makeOutline(Outliner outliner, Object slot) {
			return outliner.showCluster(slot, posSet);
		}

		@Override
		public Vec3 getCenter() {
			return center == null ? center = evalCenter() : center;
		}

		private Vec3 evalCenter() {
			Vec3 center = Vec3.ZERO;
			if (posSet.isEmpty())
				return center;
			for (BlockPos blockPos : posSet)
				center = center.add(Vec3.atLowerCornerOf(blockPos));
			center = center.scale(1f / posSet.size());
			return center.add(new Vec3(.5, .5, .5));
		}

		@Override
		public Selection copy() {
			return new Compound(posSet);
		}

		@Override
		public Iterator<BlockPos> iterator() {
			return posSet.iterator();
		}
	}

	private static class Simple implements Selection {
		private final BoundingBox bb;
		private final AABB aabb;
		private final Iterable<BlockPos> iterable;

		public Simple(BoundingBox bb) {
			this.bb = bb;
			this.aabb = new AABB(bb.minX(), bb.minY(), bb.minZ(), bb.maxX() + 1, bb.maxY() + 1, bb.maxZ() + 1);
			iterable = BlockPos.betweenClosed(Math.min(bb.minX(), bb.maxX()), Math.min(bb.minY(), bb.maxY()), Math.min(bb.minZ(), bb.maxZ()), Math.max(bb.minX(), bb.maxX()), Math.max(bb.minY(), bb.maxY()), Math.max(bb.minZ(), bb.maxZ()));
		}

		@Override
		public boolean test(BlockPos t) {
			return bb.isInside(t);
		}

		@Override
		public Selection add(Selection other) {
			return new Compound(this).add(other);
		}

		@Override
		public Selection substract(Selection other) {
			return new Compound(this).substract(other);
		}

		@Override
		public Vec3 getCenter() {
			return aabb.getCenter();
		}

		@Override
		public OutlineParams makeOutline(Outliner outliner, Object slot) {
			return outliner.showAABB(slot, aabb);
		}

		@Override
		public Selection copy() {
			return new Simple(new BoundingBox(bb.minX(), bb.minY(), bb.minZ(), bb.maxX(), bb.maxY(), bb.maxZ()));
		}

		@Override
		public Iterator<BlockPos> iterator() {
			return iterable.iterator();
		}
	}

}
