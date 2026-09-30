package net.createmod.ponder.impl.command;

import static net.minecraft.commands.Commands.argument;
import static net.minecraft.commands.Commands.literal;

import java.util.List;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;

import net.createmod.catnip.api.network.NetworkHelper;
import net.createmod.ponder.impl.packet.OpenPonderPacket;
import net.createmod.ponder.impl.packet.ReloadPonderPacket;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.IdentifierArgument;
import net.minecraft.server.level.ServerPlayer;

public final class PonderCommand {
	//public static final SuggestionProvider<CommandSourceStack> ITEM_PONDERS = SuggestionProviders.register(new Identifier("all_ponders"), (iSuggestionProviderCommandContext, builder) -> SharedSuggestionProvider.suggestResource(PonderRegistry.ALL.keySet().stream(), builder));
	//TODO PonderRegistry can't be loaded on Server Dist

	static LiteralArgumentBuilder<CommandSourceStack> build() {
		return literal("ponder")
			.executes(ctx -> send(OpenPonderPacket.TAGS, ctx.getSource().getPlayerOrException()))
			.then(
				literal("reload").executes(
					ctx -> reloadPonderIndex(ctx.getSource().getPlayerOrException())
				)
			).then(
				literal("index").executes(
					ctx -> send(OpenPonderPacket.INDEX, ctx.getSource().getPlayerOrException())
				)
			).then(
				literal("tags").executes(
					ctx -> send(OpenPonderPacket.TAGS, ctx.getSource().getPlayerOrException())
				)
			).then(
				argument("scenes", IdentifierArgument.id())
					//.suggests(ITEM_PONDERS)
					.executes(ctx -> {
						OpenPonderPacket packet = OpenPonderPacket.scenes(IdentifierArgument.getId(ctx, "scenes"));
						return send(packet, ctx.getSource().getPlayerOrException());
					})
					.then(
						argument("targets", EntityArgument.players())
							.requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))
							.executes(ctx -> send(
								OpenPonderPacket.scenes(IdentifierArgument.getId(ctx, "scene")),
								EntityArgument.getPlayers(ctx, "targets")
							))
					)
			);

	}

	private static int send(OpenPonderPacket packet, ServerPlayer player) {
		return send(packet, List.of(player));
	}

	private static int send(OpenPonderPacket packet, Iterable<ServerPlayer> players) {
		NetworkHelper.INSTANCE.sendToClients(players, packet);
		return Command.SINGLE_SUCCESS;
	}

	private static int reloadPonderIndex(ServerPlayer player) {
		NetworkHelper.INSTANCE.sendToClient(player, ReloadPonderPacket.INSTANCE);
		return Command.SINGLE_SUCCESS;
	}
}
