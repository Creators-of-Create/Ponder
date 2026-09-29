package net.createmod.catnip.impl.command.argument;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;

import net.createmod.catnip.api.config.ConfigId;
import net.minecraft.commands.CommandSourceStack;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public final class ConfigIdArgument implements ArgumentType<ConfigId> {
	public static final List<String> EXAMPLES = List.of("catnip:client", "create:server");

	public static ConfigIdArgument configId() {
		return new ConfigIdArgument();
	}

	public static ConfigId getConfigId(CommandContext<CommandSourceStack> context, String name) {
		return context.getArgument(name, ConfigId.class);
	}

	@Override
	public ConfigId parse(StringReader reader) throws CommandSyntaxException {
		return null;
	}

	@Override
	public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
		return ArgumentType.super.listSuggestions(context, builder);
	}

	@Override
	public Collection<String> getExamples() {
		return EXAMPLES;
	}
}
