package net.createmod.catnip.impl.fabric.service;

import com.mojang.brigadier.arguments.ArgumentType;

import net.createmod.catnip.api.commands.CommandArgumentTypeRegistry;
import net.fabricmc.fabric.api.command.v2.ArgumentTypeRegistry;
import net.minecraft.commands.synchronization.ArgumentTypeInfo;
import net.minecraft.resources.Identifier;

public final class FabricCommandArgumentTypeRegistry implements CommandArgumentTypeRegistry {
	@Override
	public <A extends ArgumentType<?>, T extends ArgumentTypeInfo.Template<A>, I extends ArgumentTypeInfo<A, T>> void register(Identifier id, Class<A> clazz, I serializer) {
		ArgumentTypeRegistry.registerArgumentType(id, clazz, serializer);
	}
}
