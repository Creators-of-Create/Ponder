package net.createmod.catnip.registration;

import net.createmod.catnip.registration.holder.BlockHolder;
import net.createmod.catnip.registration.holder.ItemHolder;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.level.block.RedstoneLampBlock;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;

public class TestRegistration {
	public static CatnipRegistry REGISTRY = new CatnipRegistry("create");

	public static BlockHolder<RedstoneLampBlock> BLOCK = REGISTRY.block("test", RedstoneLampBlock::new)
		.properties(Properties::air)
		.register();

	public static ItemHolder<BlockItem> BLOCK_ITEM = REGISTRY.item(BLOCK)
		.properties(p -> p.rarity(Rarity.EPIC))
		.register();

	public static void init() {}
}
