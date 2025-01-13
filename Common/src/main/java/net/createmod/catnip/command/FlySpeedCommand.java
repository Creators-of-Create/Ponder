package net.createmod.catnip.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.createmod.ponder.Ponder;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.protocol.game.ClientboundPlayerAbilitiesPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Abilities;

public class FlySpeedCommand {

	public static ArgumentBuilder<CommandSourceStack, ?> register() {
		return Commands.literal("flySpeed")
			.requires(cs -> cs.hasPermission(2))
			.then(Commands.argument("speed", FloatArgumentType.floatArg(0))
				.then(Commands.argument("target", EntityArgument.player())
					.executes(ctx -> sendFlySpeedUpdate(ctx, EntityArgument.getPlayer(ctx, "target"),
						FloatArgumentType.getFloat(ctx, "speed"))))
				.executes(ctx -> sendFlySpeedUpdate(ctx, ctx.getSource()
					.getPlayerOrException(), FloatArgumentType.getFloat(ctx, "speed"))))
			.then(Commands.literal("reset")
				.then(Commands.argument("target", EntityArgument.player())
					.executes(ctx -> sendFlySpeedUpdate(ctx, EntityArgument.getPlayer(ctx, "target"), 0.05f)))
				.executes(ctx -> sendFlySpeedUpdate(ctx, ctx.getSource()
					.getPlayerOrException(), 0.05f))

			);
	}

	private static int sendFlySpeedUpdate(CommandContext<CommandSourceStack> ctx, ServerPlayer player, float speed) {
		Abilities abilities = player.getAbilities();
		abilities.setFlyingSpeed(speed);
		player.connection.send(new ClientboundPlayerAbilitiesPacket(abilities));
		ctx.getSource().sendSuccess(() ->
				Ponder.lang()
						.text("Temporarily set ")
						.add(player.getName().copy())
						.text("'s Flying Speed to " + speed)
						.component(),
				true
		);

		return Command.SINGLE_SUCCESS;
	}

}
