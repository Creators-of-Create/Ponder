package net.createmod.catnip.registration;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

import com.google.common.collect.Multimaps;

import net.createmod.catnip.registration.builder.BlockBuilder;
import net.createmod.catnip.registration.builder.ItemBuilder;
import net.createmod.catnip.registration.holder.BaseHolder;
import net.createmod.catnip.registration.holder.BlockHolder;
import net.minecraft.core.Registry;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;

import org.jetbrains.annotations.ApiStatus.Internal;

import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;

public class CatnipRegistry {
	public final String modId;

	// registration order must be preserved so that delayed NeoForge registrations match Fabric's eager registration behaviour
	private static final Multimap<Registry<?>, Registration<?, ?, ?>> REGISTRATIONS = ArrayListMultimap.create();
	public static final Multimap<Registry<?>, Registration<?, ?, ?>> REGISTRATIONS_VIEW = Multimaps.unmodifiableMultimap(REGISTRATIONS);

	public CatnipRegistry(String modId) {
		this.modId = modId;
	}

	@Internal
	public static <R, T extends R, H extends BaseHolder<T>> void addToRegistrationMap(Registry<R> registry, Registration<R, T, H> registration) {
		REGISTRATIONS.put(registry, registration);
	}

	public <R extends Block> BlockBuilder<R> block(String id, Function<BlockBehaviour.Properties, R> factory) {
		return new BlockBuilder<>(this, id, factory);
	}

	public <R extends Item> ItemBuilder<R> item(String id, Function<Item.Properties, R> factory) {
		return new ItemBuilder<>(this, id, factory);
	}

	public <B extends Block> ItemBuilder<BlockItem> item(BlockHolder<B> block) {
		return item(block, BlockItem::new);
	}

	public <R extends BlockItem, B extends Block> ItemBuilder<R> item(BlockHolder<B> block, BiFunction<? super B, Item.Properties, R> factory) {
		return new ItemBuilder<>(this, block.getRegisteredNamePath(), properties -> factory.apply(block.value(), properties));
	}
}
