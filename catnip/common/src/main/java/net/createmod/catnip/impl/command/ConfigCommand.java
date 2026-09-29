package net.createmod.catnip.impl.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;

import net.createmod.catnip.api.Catnip;
import net.createmod.catnip.impl.command.argument.ModIdArgument;
import net.createmod.catnip.impl.config.ConfigHelper;
import net.createmod.catnip.api.config.ConfigId;
import net.createmod.catnip.api.config.ConfigPath;
import net.createmod.catnip.api.config.ConfigSide;
import net.createmod.catnip.api.config.ConfigValueId;
import net.createmod.catnip.impl.command.argument.ConfigIdArgument;
import net.createmod.catnip.impl.command.argument.ConfigPathArgument;
import net.createmod.catnip.api.network.NetworkHelper;
import net.createmod.catnip.impl.config.packet.OpenConfigScreenPacket;
import net.createmod.catnip.impl.config.packet.ClientboundSetConfigValuePacket;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

import java.util.Optional;
import java.util.function.BiFunction;

import static net.minecraft.commands.Commands.argument;
import static net.minecraft.commands.Commands.literal;

/// Examples:
/// - `/catnip config` - open Catnip's root config screen
/// - `/catnip config ponder` - open Ponder's root config screen
/// - `/catnip config catnip:client` - open Catnip's config screen with the client config already selected
/// - `/catnip config create:client rainbowDebug set false` - disable Create's rainbow debug for the sender
public class ConfigCommand {
	public static final Component SET_REQUESTED = Component.translatable("command.catnip.config.set.request");

	public static final DynamicCommandExceptionType SET_FAILED = new DynamicCommandExceptionType(
		value -> Component.translatable("command.catnip.config.set.failure", value)
	);

	public static ArgumentBuilder<CommandSourceStack, ?> register() {
		return literal("config").executes(withPlayer((_, player) -> {
			NetworkHelper.INSTANCE.sendToClient(player, OpenConfigScreenPacket.forMod(Catnip.ID));
			return Command.SINGLE_SUCCESS;
		})).then(
			argument("mod", ModIdArgument.modId()).executes(withPlayer((ctx, player) -> {
				String modId = ModIdArgument.getModId(ctx, "mod");
				NetworkHelper.INSTANCE.sendToClient(player, OpenConfigScreenPacket.forMod(modId));
				return Command.SINGLE_SUCCESS;
			}))
		).then(
			argument("config", ConfigIdArgument.configId()).executes(withPlayer((ctx, player) -> {
				ConfigId config = ConfigIdArgument.getConfigId(ctx, "config");
				NetworkHelper.INSTANCE.sendToClient(player, OpenConfigScreenPacket.forConfig(config));
				return Command.SINGLE_SUCCESS;
			})).then(
				argument("path", ConfigPathArgument.path()).executes(withPlayer((ctx, player) -> {
					ConfigValueId valueId = getValueId(ctx);
					NetworkHelper.INSTANCE.sendToClient(player, OpenConfigScreenPacket.forValue(valueId));
					return Command.SINGLE_SUCCESS;
				})).then(literal("set").requires(Commands.hasPermission(ConfigHelper.CAN_CHANGE_CONFIGS)).then(
					argument("value", StringArgumentType.string()).executes(ctx -> {
						ConfigValueId valueId = getValueId(ctx);
						String value = StringArgumentType.getString(ctx, "value");

						if (valueId.config().side() == ConfigSide.CLIENT) {
							ServerPlayer player = ctx.getSource().getPlayerOrException();
							NetworkHelper.INSTANCE.sendToClient(player, new ClientboundSetConfigValuePacket(valueId, value));
							ctx.getSource().sendSuccess(() -> SET_REQUESTED, false);
							return Command.SINGLE_SUCCESS;
						}

						Optional<String> error = ConfigHelper.trySetValue(valueId, value);
						if (error.isPresent()) {
							throw SET_FAILED.create(error.get());
						}

						ctx.getSource().sendSuccess(() -> createSetSuccessMessage(valueId, value), true);
						return Command.SINGLE_SUCCESS;
					})
				))
			)
		);
	}

	private static Command<CommandSourceStack> withPlayer(BiFunction<CommandContext<CommandSourceStack>, ServerPlayer, Integer> command) {
		return ctx -> {
			ServerPlayer player = ctx.getSource().getPlayerOrException();
			return command.apply(ctx, player);
		};
	}

	private static ConfigValueId getValueId(CommandContext<CommandSourceStack> ctx) {
		ConfigId config = ConfigIdArgument.getConfigId(ctx, "config");
		ConfigPath path = ConfigPathArgument.getPath(ctx, "path");
		return new ConfigValueId(config, path);
	}

	private static Component createSetSuccessMessage(ConfigValueId value, String serializedValue) {
		return Component.translatableEscape("command.catnip.config.set.success", value, serializedValue);
	}
}
