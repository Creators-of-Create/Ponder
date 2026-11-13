package net.createmod.catnip.impl.neoforge.service;

import java.util.ArrayList;
import java.util.List;

import com.mojang.brigadier.arguments.ArgumentType;

import net.createmod.catnip.api.commands.CommandArgumentTypeRegistry;
import net.minecraft.commands.synchronization.ArgumentTypeInfo;
import net.minecraft.commands.synchronization.ArgumentTypeInfo.Template;
import net.minecraft.commands.synchronization.ArgumentTypeInfos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.registries.RegisterEvent;

@EventBusSubscriber
public final class NeoForgeCommandArgumentTypeRegistry implements CommandArgumentTypeRegistry {
	private static final List<Registration<?, ?, ?>> registrations = new ArrayList<>();

	@Override
	public synchronized <A extends ArgumentType<?>, T extends ArgumentTypeInfo.Template<A>, I extends ArgumentTypeInfo<A, T>> void register(Identifier id, Class<A> clazz, I serializer) {
		registrations.add(new Registration<>(id, clazz, serializer));
		ArgumentTypeInfos.registerByClass(clazz, serializer);
	}

	@SubscribeEvent
	public static void registerEvent(RegisterEvent event) {
		if (event.getRegistryKey().equals(Registries.COMMAND_ARGUMENT_TYPE)) {
			registrations.forEach(registration -> event.register(Registries.COMMAND_ARGUMENT_TYPE, registration.id(), registration::serializer));
		}
	}

	private record Registration<A extends ArgumentType<?>, T extends Template<A>, I extends ArgumentTypeInfo<A, T>>(Identifier id, Class<A> clazz, I serializer) {}
}
