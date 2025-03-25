package net.createmod.catnip.nbt;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.function.UnaryOperator;

import javax.annotation.Nullable;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
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

	// Remove the first layer of commands while preserving the styles.
	// Since recursive commands won't be executed, there's no need to handle them.
	private static final UnaryOperator<CompoundTag> signProcessor = data -> {
		var front_text = data.getCompound("front_text").getList("messages", Tag.TAG_STRING);
		var back_text = data.getCompound("back_text").getList("messages", Tag.TAG_STRING);
		ListTag front_text2 = removeCommands(front_text);
		ListTag back_text2 = removeCommands(back_text);
		data.getCompound("front_text").put("messages",front_text2);
		data.getCompound("back_text").put("messages",back_text2);
		return data;
	};

	private static ListTag removeCommands(ListTag inList) {
		ListTag result = new ListTag();
		inList.stream()
			.map(t->
				{
					var component = Component.Serializer.fromJson(t.getAsString());
					if(component != null) {
						return StringTag.valueOf(Component.Serializer.toJson(
							removeCommand(component)
						));
					}
					return StringTag.valueOf("");
				}
			)
			.forEach(result::add);
		return result;
	}

	private static MutableComponent removeCommand(MutableComponent textComponent){
		textComponent.setStyle(textComponent.getStyle().withClickEvent(null));
		for(Component component : textComponent.getSiblings())
		{
			if(component instanceof MutableComponent textComponent2)
				removeCommand(textComponent2);
		}
		return textComponent;
	}
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
