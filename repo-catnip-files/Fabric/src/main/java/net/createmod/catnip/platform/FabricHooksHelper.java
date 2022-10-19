package net.createmod.catnip.platform;

import net.createmod.catnip.platform.services.ModHooksHelper;
import net.fabricmc.fabric.api.block.BlockPickInteractionAware;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.HitResult;

public class FabricHooksHelper implements ModHooksHelper {
	@Override
	public boolean playerPlaceSingleBlock(Player player, Level level, BlockPos pos, BlockState newState) {
		return false;
	}

	@Override
	public ItemStack getCloneItemFromBlockstate(BlockState state, HitResult target, BlockGetter level, BlockPos pos, Player player) {
		if (state.getBlock() instanceof BlockPickInteractionAware blockPickInteractionAware)
			return blockPickInteractionAware.getPickedStack(state, level, pos, player, target);
		return ModHooksHelper.super.getCloneItemFromBlockstate(state, target, level, pos, player);
	}

	@Override // TODO: fabric doesn't support fake players
	public boolean isPlayerFake(ServerPlayer player) {
		return false;
	}

	@Override
	public CompoundTag getExtraEntityData(Entity entity) {
		return entity.getExtraCustomData();
	}
}
