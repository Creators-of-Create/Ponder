package net.createmod.catnip.platform.services;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public interface ModHooksHelper {

	/**
	 * Attempts to place a single Block as a Player, and should fire according events
	 *
	 * @return True if the event got canceled or the Block was not be placed, False otherwise
	 */
	boolean playerPlaceSingleBlock(Player player, Level level, BlockPos pos, BlockState newState);

	CompoundTag getExtraEntityData(Entity entity);

}
