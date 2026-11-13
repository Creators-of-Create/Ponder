package net.createmod.catnip.api.commands;

import com.mojang.brigadier.arguments.ArgumentType;

import net.createmod.catnip.api.platform.ServiceHelper;
import net.minecraft.commands.synchronization.ArgumentTypeInfo;
import net.minecraft.resources.Identifier;

public interface CommandArgumentTypeRegistry {
	CommandArgumentTypeRegistry INSTANCE = ServiceHelper.load(CommandArgumentTypeRegistry.class);

	<A extends ArgumentType<?>, T extends ArgumentTypeInfo.Template<A>, I extends ArgumentTypeInfo<A, T>> void register(Identifier id, Class<A> clazz, I serializer);
}
