package net.createmod.catnip.platform;

import net.createmod.catnip.platform.services.ModHooksHelper;
import net.createmod.catnip.placement.IPlacementHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.HitResult;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.common.util.BlockSnapshot;
import net.neoforged.neoforge.common.util.FakePlayer;
import net.neoforged.neoforge.event.level.BlockEvent;

public class NeoForgeHooksHelper implements ModHooksHelper {

	@Override
	public boolean playerPlaceSingleBlock(Player player, Level level, BlockPos pos, BlockState newState) {
		BlockSnapshot snapshot = BlockSnapshot.create(level.dimension(), level, pos);
		level.setBlockAndUpdate(pos, newState);

		BlockEvent.EntityPlaceEvent event = new BlockEvent.EntityPlaceEvent(snapshot, IPlacementHelper.ID, player);
		if (NeoForge.EVENT_BUS.post(event).isCanceled()) {
			snapshot.restore(Block.UPDATE_CLIENTS); // TODO - Checkover
			return true;
		}

		return false;
	}

	@Override
	public ItemStack getCloneItemFromBlockstate(BlockState state, HitResult target, LevelReader level, BlockPos pos, Player player) {
		return state.getCloneItemStack(target, level, pos, player);
	}

	@Override
	public boolean isPlayerFake(ServerPlayer player) {
		return player instanceof FakePlayer;
	}
}
