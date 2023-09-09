package net.createmod.ponder.command;

import java.util.Collection;

import com.google.common.collect.ImmutableList;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.ArgumentBuilder;

import net.createmod.catnip.net.ClientboundSimpleActionPacket;
import net.createmod.catnip.platform.CatnipServices;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.ResourceLocationArgument;
import net.minecraft.server.level.ServerPlayer;

public class PonderCommand {
	//public static final SuggestionProvider<CommandSourceStack> ITEM_PONDERS = SuggestionProviders.register(new ResourceLocation("all_ponders"), (iSuggestionProviderCommandContext, builder) -> SharedSuggestionProvider.suggestResource(PonderRegistry.ALL.keySet().stream(), builder));
	//TODO PonderRegistry can't be loaded on Server Dist

	static ArgumentBuilder<CommandSourceStack, ?> register() {
		return Commands.literal("ponder")
				.requires(cs -> cs.hasPermission(0))
				.executes(ctx -> openScene("ponder:tags", ctx.getSource().getPlayerOrException()))
				.then(Commands.literal("reload")
							  .executes(ctx -> reloadPonderIndex(ctx.getSource().getPlayerOrException()))
				)
				.then(Commands.literal("index")
							  .executes(ctx -> openScene("ponder:index", ctx.getSource().getPlayerOrException()))
				)
				.then(Commands.literal("tags")
							  .executes(ctx -> openScene("ponder:tags", ctx.getSource().getPlayerOrException()))
				)
				.then(Commands.argument("scene", ResourceLocationArgument.id())
							  //.suggests(ITEM_PONDERS)
							  .executes(ctx -> openScene(ResourceLocationArgument.getId(ctx, "scene").toString(),
														 ctx.getSource().getPlayerOrException()))
							  .then(Commands.argument("targets", EntityArgument.players())
											.requires(cs -> cs.hasPermission(2))
											.executes(ctx -> openScene(
													ResourceLocationArgument.getId(ctx, "scene").toString(),
													EntityArgument.getPlayers(ctx, "targets")))
							  )
				);

	}

	private static int openScene(String sceneId, ServerPlayer player) {
		return openScene(sceneId, ImmutableList.of(player));
	}

	private static int openScene(String sceneId, Collection<? extends ServerPlayer> players) {
		for (ServerPlayer player : players) {
			if (CatnipServices.HOOKS.isPlayerFake(player))
				continue;

			CatnipServices.NETWORK.sendToPlayer(
					player,
					new ClientboundSimpleActionPacket("openPonder", sceneId)
			);
		}
		return Command.SINGLE_SUCCESS;
	}

	private static int reloadPonderIndex(ServerPlayer player) {
		CatnipServices.NETWORK.simpleActionToClient(player, "reloadPonder", "");

		return Command.SINGLE_SUCCESS;
	}
}
