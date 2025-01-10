package net.createmod.catnip.command;

import java.util.Collections;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.tree.CommandNode;
import com.mojang.brigadier.tree.LiteralCommandNode;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;

public class CatnipCommands {

	public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {

		LiteralCommandNode<CommandSourceStack> util = buildUtilityCommands();

		LiteralCommandNode<CommandSourceStack> catnipRoot = Commands.literal("catnip")
				.requires(cs -> cs.hasPermission(0))
				.then(ConfigCommand.register())
				.then(util)
				.build();

		catnipRoot.addChild(buildRedirect("u", util));

		dispatcher.getRoot().addChild(catnipRoot);

		//add all of Catnip's commands to /c if it already exists, otherwise create the shortcut
		createOrAddToShortcut(dispatcher, "c", catnipRoot);
	}

	private static LiteralCommandNode<CommandSourceStack> buildUtilityCommands() {

		return Commands.literal("util")
				.then(FlySpeedCommand.register())
				.build();

	}

	/**
	 * *****
	 * <a href="https://github.com/VelocityPowered/Velocity/blob/8abc9c80a69158ebae0121fda78b55c865c0abad/proxy/src/main/java/com/velocitypowered/proxy/util/BrigadierUtils.java#L38">Source</a>
	 * *****
	 * <p>
	 * Returns a literal node that redirects its execution to
	 * the given destination node.
	 *
	 * @param alias       the command alias
	 * @param destination the destination node
	 *
	 * @return the built node
	 */
	public static LiteralCommandNode<CommandSourceStack> buildRedirect(final String alias, final LiteralCommandNode<CommandSourceStack> destination) {
		// Redirects only work for nodes with children, but break the top argument-less command.
		// Manually adding the root command after setting the redirect doesn't fix it.
		// See https://github.com/Mojang/brigadier/issues/46). Manually clone the node instead.
		LiteralArgumentBuilder<CommandSourceStack> builder = LiteralArgumentBuilder
				.<CommandSourceStack>literal(alias)
				.requires(destination.getRequirement())
				.forward(destination.getRedirect(), destination.getRedirectModifier(), destination.isFork())
				.executes(destination.getCommand());
		for (CommandNode<CommandSourceStack> child : destination.getChildren()) {
			builder.then(child);
		}
		return builder.build();
	}

	public static void createOrAddToShortcut(CommandDispatcher<CommandSourceStack> dispatcher, String shortcut, LiteralCommandNode<CommandSourceStack> createRoot) {
		CommandNode<CommandSourceStack> node = dispatcher.findNode(Collections.singleton(shortcut));
		if (node != null) {
			for (CommandNode<CommandSourceStack> child : createRoot.getChildren()) {
				node.addChild(child);
			}
			return;
		}

		dispatcher.getRoot().addChild(CatnipCommands.buildRedirect(shortcut, createRoot));
	}
}
