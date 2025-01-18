package net.createmod.catnip.platform.services;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.HitResult;

public interface ModHooksHelper {

	/**
	 * Attempts to place a single Block as a Player, and should fire according events
	 *
	 * @return True if the event got canceled or the Block was not be placed, False otherwise
	 */
	boolean playerPlaceSingleBlock(Player player, Level level, BlockPos pos, BlockState newState);

	default ItemStack getCloneItemFromBlockstate(BlockState state, HitResult target, BlockGetter level, BlockPos pos, Player player) {
		return state.getBlock().getCloneItemStack(level, pos, state);
	}

	boolean isPlayerFake(ServerPlayer player);

}
