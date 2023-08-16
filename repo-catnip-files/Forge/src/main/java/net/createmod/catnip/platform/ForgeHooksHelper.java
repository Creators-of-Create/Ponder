package net.createmod.catnip.platform;

import net.createmod.catnip.platform.services.ModHooksHelper;
import net.createmod.catnip.utility.placement.IPlacementHelper;
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
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.BlockSnapshot;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.event.level.BlockEvent;

public class ForgeHooksHelper implements ModHooksHelper {

	@Override
	public boolean playerPlaceSingleBlock(Player player, Level level, BlockPos pos, BlockState newState) {
		BlockSnapshot snapshot = BlockSnapshot.create(level.dimension(), level, pos);
		level.setBlockAndUpdate(pos, newState);

		BlockEvent.EntityPlaceEvent event = new BlockEvent.EntityPlaceEvent(snapshot, IPlacementHelper.ID, player);
		if (MinecraftForge.EVENT_BUS.post(event)) {
			snapshot.restore(true, false);
			return true;
		}

		return false;
	}

	@Override
	public ItemStack getCloneItemFromBlockstate(BlockState state, HitResult target, BlockGetter level, BlockPos pos, Player player) {
		return state.getCloneItemStack(target, level, pos, player);
	}

	@Override
	public boolean isPlayerFake(ServerPlayer player) {
		return player instanceof FakePlayer;
	}

	@Override
	public CompoundTag getExtraEntityData(Entity entity) {
		return entity.getPersistentData();
	}
}
