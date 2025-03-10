package net.createmod.catnip.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;

import net.createmod.catnip.config.ui.ConfigHelper;
import net.createmod.catnip.net.ClientboundConfigPacket;
import net.createmod.catnip.net.ClientboundSimpleActionPacket;
import net.createmod.catnip.net.ConfigPathArgument;
import net.createmod.catnip.platform.CatnipServices;
import net.createmod.ponder.Ponder;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.fml.config.ModConfig;

/**
 * Examples:
 * /catnip config client - to open Catnip's ConfigGui with the client config already selected
 * /catnip config "botania:common" - to open Catnip's ConfigGui with Botania's common config already selected
 * /catnip config "create:client.client.rainbowDebug" set false - to disable Create's rainbow debug for the sender
 */
public class ConfigCommand {

	public static ArgumentBuilder<CommandSourceStack, ?> register() {
		return Commands.literal("config")
				.executes(ctx -> {
					ServerPlayer player = ctx.getSource().getPlayerOrException();
					CatnipServices.NETWORK.sendToPlayer(player,
							new ClientboundSimpleActionPacket("configScreen", ""));

					return Command.SINGLE_SUCCESS;
				})
				.then(Commands.argument("path", ConfigPathArgument.path())
						.executes(ctx -> {
							ServerPlayer player = ctx.getSource().getPlayerOrException();

							CatnipServices.NETWORK.sendToPlayer(player,
									new ClientboundSimpleActionPacket("configScreen", ConfigPathArgument.getPath(ctx, "path").toString()));

							return Command.SINGLE_SUCCESS;
						})
						.then(Commands.literal("set")
								.requires(cs -> cs.hasPermission(2))
								.then(Commands.argument("value", StringArgumentType.string())
										.executes(ctx -> {
											ConfigHelper.ConfigPath path = ConfigPathArgument.getPath(ctx, "path");
											String value = StringArgumentType.getString(ctx, "value");

											if (path.getType() == ModConfig.Type.CLIENT) {
												ServerPlayer player = ctx.getSource().getPlayerOrException();

												CatnipServices.NETWORK.sendToPlayer(player,
														new ClientboundConfigPacket(path.toString(), value));

												return Command.SINGLE_SUCCESS;
											}

											try {
												ConfigHelper.setConfigValue(path, value);
												ctx.getSource().sendSuccess(() -> Component.literal("Great Success!"), false);
												return Command.SINGLE_SUCCESS;
											} catch (ConfigHelper.InvalidValueException e) {
												ctx.getSource().sendFailure(Component.literal("Config could not be set the the specified value!"));
												return 0;
											} catch (Exception e) {
												ctx.getSource().sendFailure(Component.literal("Something went wrong while trying to set config value. Check the server logs for more information"));
												Ponder.LOGGER.warn("Exception during server-side config value set:", e);
												return 0;
											}
										})
								)
						)
				);
	}

}
