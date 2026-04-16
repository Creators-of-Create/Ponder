package net.createmod.catnip.registration;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

import com.google.common.collect.Multimaps;

import net.createmod.catnip.data.Pair;
import net.createmod.catnip.registration.builder.BlockBuilder;
import net.createmod.catnip.registration.builder.ItemBuilder;
import net.createmod.catnip.registration.callback.FusedBiCallback;
import net.createmod.catnip.registration.callback.QuadConsumer;
import net.createmod.catnip.registration.callback.TriConsumer;
import net.createmod.catnip.registration.holder.BaseHolder;
import net.createmod.catnip.registration.holder.BlockHolder;
import net.minecraft.core.Registry;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;

import org.jetbrains.annotations.ApiStatus.Internal;

import java.util.function.BiConsumer;
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

	public static <A> void onceRegistered(BaseHolder<A> a, Consumer<? super A> consumer) {
		a.onceRegistered(consumer);
	}

	public static <A, B> void onceRegistered(BaseHolder<A> a, BaseHolder<B> b, BiConsumer<? super A, ? super B> consumer) {
		FusedBiCallback<A, B> ab = new FusedBiCallback<>();
		a.onceRegistered(ab::provideA);
		b.onceRegistered(ab::provideB);
		ab.addListener(consumer);
	}

	public static <A, B, C> void onceRegistered(BaseHolder<A> a, BaseHolder<B> b, BaseHolder<C> c, TriConsumer<? super A, ? super B, ? super C> consumer) {
		FusedBiCallback<A, B> ab = new FusedBiCallback<>();
		a.onceRegistered(ab::provideA);
		b.onceRegistered(ab::provideB);

		FusedBiCallback<Pair<A, B>, C> abc = ab.extend();
		c.onceRegistered(abc::provideB);

		abc.addListener(($ab, $c) -> consumer.accept($ab.getFirst(), $ab.getSecond(), $c));
	}

	public static <A, B, C, D> void onceRegistered(BaseHolder<A> a, BaseHolder<B> b, BaseHolder<C> c, BaseHolder<D> d, QuadConsumer<? super A, ? super B, ? super C, ? super D> consumer) {
		FusedBiCallback<A, B> ab = new FusedBiCallback<>();
		a.onceRegistered(ab::provideA);
		b.onceRegistered(ab::provideB);

		FusedBiCallback<C, D> cd = new FusedBiCallback<>();
		c.onceRegistered(cd::provideA);
		d.onceRegistered(cd::provideB);

		FusedBiCallback.combine(ab, cd).addListener(($ab, $cd) ->
			consumer.accept($ab.getFirst(), $ab.getSecond(), $cd.getFirst(), $cd.getSecond()));
	}

	@SuppressWarnings({"unchecked", "rawtypes"})
	public static void onceRegistered(Runnable cb, BaseHolder<?>... holders) {
		if (holders.length == 0) {
			cb.run();
		} else if (holders.length == 1) {
			holders[0].onceRegistered($ -> cb.run());
		} else {
			FusedBiCallback fcb = new FusedBiCallback<>();
			holders[0].onceRegistered(fcb::provideA);
			holders[1].onceRegistered(fcb::provideB);
			for (int i = 2; i < holders.length; i++) {
				fcb = fcb.extend();
				holders[i].onceRegistered(fcb::provideB);
			}
			fcb.addListener(($, $$) -> cb.run());
		}
	}
}
