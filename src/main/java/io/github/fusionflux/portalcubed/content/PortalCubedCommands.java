package io.github.fusionflux.portalcubed.content;

import io.github.fusionflux.portalcubed.PortalCubed;

import io.github.fusionflux.portalcubed.content.command.CreateConstructCommand;

import org.quiltmc.qsl.command.api.CommandRegistrationCallback;

import static net.minecraft.commands.Commands.literal;

public class PortalCubedCommands {
	public static void init() {
		CommandRegistrationCallback.EVENT.register((dispatcher, ctx, env) -> dispatcher.register(
				literal(PortalCubed.ID)
						.then(CreateConstructCommand.build())
		));
	}
}
