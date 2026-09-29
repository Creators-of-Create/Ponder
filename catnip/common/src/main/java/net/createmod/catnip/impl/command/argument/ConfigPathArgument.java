package net.createmod.catnip.impl.command.argument;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;

import net.createmod.catnip.api.config.ConfigPath;
import net.createmod.catnip.api.platform.services.PlatformHelper;
import net.createmod.catnip.impl.config.ConfigHelper;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

public class ConfigPathArgument implements ArgumentType<ConfigPath> {
	public static final SimpleCommandExceptionType PARSE_ERROR = new SimpleCommandExceptionType(
		Component.literal("Unable to parse ConfigPath")
	);

	public static final List<String> EXAMPLES = List.of("comfyReading", "kinetics.disableStress");

	public static ConfigPathArgument path() {
		return new ConfigPathArgument();
	}

	public static ConfigPath getPath(CommandContext<CommandSourceStack> context, String name) {
		return context.getArgument(name, ConfigPath.class);
	}

	@Override
	public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
		String[] split = builder.getRemaining().split(":");

		if (split.length > 1 || builder.getRemaining().endsWith(":")) {
			List<String> suggestions = new ArrayList<>();
//			for (String side : BASE_SUGGESTIONS) {
//				suggestions.add(split[0] + ":" + side);
//			}

			return SharedSuggestionProvider.suggest(suggestions, builder);
		}

		List<String> matchingMods = new ArrayList<>();
		for (String mod : PlatformHelper.INSTANCE.getLoadedModIds()) {
			if (mod.startsWith(split[0])) {
				matchingMods.add(mod + ":");
			}
		}

		if (matchingMods.isEmpty()) {
//			return SharedSuggestionProvider.suggest(BASE_SUGGESTIONS, builder);
		}

		return SharedSuggestionProvider.suggest(matchingMods, builder);
	}

	@Override
	public Collection<String> getExamples() {
		return EXAMPLES;
	}

	@Override
	public ConfigPath parse(StringReader reader) throws CommandSyntaxException {
		int i = reader.getCursor();

		while (reader.canRead() && Identifier.isAllowedInIdentifier(Character.toLowerCase(reader.peek()))) {
			reader.skip();
		}

		String path = reader.getString().substring(i, reader.getCursor());

//		try {
//			return ConfigPath.parse(path);
//		} catch (NullPointerException e) {
			reader.setCursor(i);
			throw PARSE_ERROR.createWithContext(reader);
//		}
	}
}
