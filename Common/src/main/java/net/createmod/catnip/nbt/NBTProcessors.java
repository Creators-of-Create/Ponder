package net.createmod.catnip.nbt;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.function.UnaryOperator;

import javax.annotation.Nullable;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.item.EnchantedBookItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.SpawnerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public final class NBTProcessors {

	private static final Map<BlockEntityType<?>, UnaryOperator<CompoundTag>> processors = new HashMap<>();
	private static final Map<BlockEntityType<?>, UnaryOperator<CompoundTag>> survivalProcessors = new HashMap<>();

	public static synchronized void addProcessor(BlockEntityType<?> type, UnaryOperator<CompoundTag> processor) {
		processors.put(type, processor);
	}

	public static synchronized void addSurvivalProcessor(BlockEntityType<?> type,
		UnaryOperator<CompoundTag> processor) {
		survivalProcessors.put(type, processor);
	}

	// Triggered by block tag, not BE type
	private static final UnaryOperator<CompoundTag> signProcessor = data -> {
		for (String key : List.of("front_text", "back_text")) {
			CompoundTag textTag = data.getCompound(key);
			if (!textTag.contains("messages", Tag.TAG_LIST))
				continue;
			for (Tag tag : textTag.getList("messages", Tag.TAG_STRING))
				if (tag instanceof StringTag stringTag)
					if (textComponentHasClickEvent(stringTag.getAsString()))
						return null;
		}
		if (data.contains("front_item") || data.contains("back_item"))
			return null; // "Amendments" compat: sign data contains itemstacks
		return data;
	};

	public static UnaryOperator<CompoundTag> itemProcessor(String tagKey) {
		return data -> {
			CompoundTag compound = data.getCompound(tagKey);
			if (!compound.contains("tag", 10))
				return data;
			CompoundTag itemTag = compound.getCompound("tag");
			HashSet<String> keys = new HashSet<>(itemTag.getAllKeys());
			for (String key : keys)
				if (isUnsafeItemNBTKey(key))
					itemTag.remove(key);
			if (itemTag.isEmpty())
				compound.remove("tag");
			return data;
		};
	}

	public static ItemStack withUnsafeNBTDiscarded(ItemStack stack) {
		if (stack.getTag() == null)
			return stack;
		ItemStack copy = stack.copy();
		for (String key : stack.getTag().getAllKeys()) {
			if (isUnsafeItemNBTKey(key)) {
				copy.removeTagKey(key);
			}
		}
		return copy;
	}

	public static boolean isUnsafeItemNBTKey(String name) {
		if (name.equals(EnchantedBookItem.TAG_STORED_ENCHANTMENTS))
			return false;
		if (name.equals("Enchantments"))
			return false;
		if (name.contains("Potion"))
			return false;
		if (name.contains("Damage"))
			return false;
		if (name.equals("display"))
			return false;
		return true;
	}

	public static boolean textComponentHasClickEvent(String json) {
		return textComponentHasClickEvent(Component.Serializer.fromJson(json.isEmpty() ? "\"\"" : json));
	}

	public static boolean textComponentHasClickEvent(Component component) {
		for (Component sibling : component.getSiblings())
			if (textComponentHasClickEvent(sibling))
				return true;
		return component != null && component.getStyle() != null && component.getStyle()
			.getClickEvent() != null;
	}

	private NBTProcessors() {}

	@Nullable
	public static CompoundTag process(BlockState state, BlockEntity blockEntity, @Nullable CompoundTag compound, boolean survival) {
		if (compound == null)
			return null;
		BlockEntityType<?> type = blockEntity.getType();
		if (survival && survivalProcessors.containsKey(type))
			compound = survivalProcessors.get(type)
				.apply(compound);
		if (compound != null && processors.containsKey(type))
			return processors.get(type)
				.apply(compound);
		if (blockEntity instanceof SpawnerBlockEntity)
			return compound;
		if (state.is(BlockTags.ALL_SIGNS))
			return signProcessor.apply(compound);
		if (blockEntity.onlyOpCanSetNbt())
			return null;
		return compound;
	}

}
