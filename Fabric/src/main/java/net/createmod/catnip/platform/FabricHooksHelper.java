package net.createmod.catnip.platform;

import net.createmod.catnip.platform.services.ModHooksHelper;
import net.fabricmc.fabric.api.block.BlockPickInteractionAware;
import net.fabricmc.fabric.api.entity.FakePlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.HitResult;

public class FabricHooksHelper implements ModHooksHelper {
	@Override
	public boolean playerPlaceSingleBlock(Player player, Level level, BlockPos pos, BlockState newState) {
		return false;
	}

	@Override
	public ItemStack getCloneItemFromBlockstate(BlockState state, HitResult target, LevelReader level, BlockPos pos, Player player) {
		if (state.getBlock() instanceof BlockPickInteractionAware blockPickInteractionAware)
			return blockPickInteractionAware.getPickedStack(state, level, pos, player, target);
		return ModHooksHelper.super.getCloneItemFromBlockstate(state, target, level, pos, player);
	}

	@Override
	public boolean isPlayerFake(ServerPlayer player) {
		return player instanceof FakePlayer;
	}
}
