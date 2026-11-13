package net.createmod.catnip.api.placement;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;

import net.createmod.catnip.api.data.Iterate;
import net.createmod.catnip.api.data.Pair;
import net.createmod.catnip.api.math.VecHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

public interface IPlacementHelper {

	/**
	 * used as an identifier in SuperGlueHandler to skip blocks placed by helpers
	 */
	BlockState ID = new BlockState(Blocks.AIR, null, null);

	/**
	 * @return a predicate that gets tested with the items held in the players hands<br>
	 * should return true if this placement helper is active with the given item
	 */
	Predicate<ItemStack> getItemPredicate();

	/**
	 * @return a predicate that gets tested with the blockstate the player is looking at<br>
	 * should return true if this placement helper is active with the given blockstate
	 */
	Predicate<BlockState> getStatePredicate();

	/**
	 *
	 * @param player The {@link Player} that activated the placement helper
	 * @param level  The {@link Level} that the placement helper got activated in
	 * @param state  The {@link BlockState} that the player is looking at or clicked on
	 * @param pos    The {@link BlockPos} the player is looking at or clicked on
	 * @param ray    The exact {@link BlockHitResult}
	 * @return the PlacementOffset object describing where to place the new block.<br>
	 * Use {@link PlacementOffset#fail} when no new position could be found.<br>
	 * Use {@link PlacementOffset#success(Vec3i)} with the new BlockPos to indicate a success
	 * and call {@link PlacementOffset#withTransform(Function)} if the blocks default state has to be modified before it is placed
	 */
	PlacementOffset getOffset(Player player, Level level, BlockState state, BlockPos pos, BlockHitResult ray);

	//sets the offset's ghost state with the default state of the held block item, this is used in PlacementHelpers and can be ignored in most cases
	default PlacementOffset getOffset(Player player, Level level, BlockState state, BlockPos pos, BlockHitResult ray, ItemStack heldItem) {
		PlacementOffset offset = getOffset(player, level, state, pos, ray);
		if (heldItem.getItem() instanceof BlockItem blockItem) {
			offset = offset.withGhostState(blockItem.getBlock().defaultBlockState());
		}
		return offset;
	}

	static List<Direction> orderedByDistanceOnlyAxis(BlockPos pos, Vec3 hit, Direction.Axis axis) {
		return orderedByDistance(pos, hit, dir -> dir.getAxis() == axis);
	}

	static List<Direction> orderedByDistanceOnlyAxis(BlockPos pos, Vec3 hit, Direction.Axis axis, Predicate<Direction> includeDirection) {
		return orderedByDistance(pos, hit, ((Predicate<Direction>) dir -> dir.getAxis() == axis).and(includeDirection));
	}

	static List<Direction> orderedByDistanceExceptAxis(BlockPos pos, Vec3 hit, Direction.Axis axis) {
		return orderedByDistance(pos, hit, dir -> dir.getAxis() != axis);
	}

	static List<Direction> orderedByDistanceExceptAxis(BlockPos pos, Vec3 hit, Direction.Axis axis, Predicate<Direction> includeDirection) {
		return orderedByDistance(pos, hit, ((Predicate<Direction>) dir -> dir.getAxis() != axis).and(includeDirection));
	}

	static List<Direction> orderedByDistanceExceptAxis(BlockPos pos, Vec3 hit, Direction.Axis first, Direction.Axis second) {
		return orderedByDistanceExceptAxis(pos, hit, first, d -> d.getAxis() != second);
	}

	static List<Direction> orderedByDistanceExceptAxis(BlockPos pos, Vec3 hit, Direction.Axis first, Direction.Axis second, Predicate<Direction> includeDirection) {
		return orderedByDistanceExceptAxis(pos, hit, first, ((Predicate<Direction>) d -> d.getAxis() != second).and(includeDirection));
	}

	static List<Direction> orderedByDistance(BlockPos pos, Vec3 hit) {
		return orderedByDistance(pos, hit, _$ -> true);
	}

	static List<Direction> orderedByDistance(BlockPos pos, Vec3 hit, Predicate<Direction> includeDirection) {
		List<Direction> directions = new ArrayList<>();

		for (Direction dir : Iterate.directions) {
			if (includeDirection.test(dir)) {
				directions.add(dir);
			}
		}

		return orderedByDistance(pos, hit, directions);
	}

	static List<Direction> orderedByDistance(BlockPos pos, Vec3 hit, Collection<Direction> directions) {
		Vec3 centerToHit = hit.subtract(VecHelper.getCenterOf(pos));

		List<Pair<Direction, Double>> distances = new ArrayList<>();
		for (Direction dir : directions) {
			distances.add(Pair.of(dir, dir.getUnitVec3().distanceTo(centerToHit)));
		}

		distances.sort(Comparator.comparingDouble(Pair::getSecond));

		List<Direction> sortedDirections = new ArrayList<>();
		for (Pair<Direction, Double> p : distances) {
			sortedDirections.add(p.getFirst());
		}

		return sortedDirections;
	}

	default boolean matchesItem(ItemStack item) {
		return getItemPredicate().test(item);
	}

	default boolean matchesState(BlockState state) {
		return getStatePredicate().test(state);
	}
}
