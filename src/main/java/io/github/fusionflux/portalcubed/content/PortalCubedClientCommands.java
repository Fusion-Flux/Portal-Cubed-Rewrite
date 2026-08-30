package io.github.fusionflux.portalcubed.content;

import static net.fabricmc.fabric.api.client.command.v2.ClientCommands.literal;

import io.github.fusionflux.portalcubed.PortalCubed;
import io.github.fusionflux.portalcubed.framework.config.ConfigCommand;
import io.github.fusionflux.portalcubed.framework.entity.ResyncEntitiesCommand;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;

public final class PortalCubedClientCommands {
	public static void init() {
		ClientCommandRegistrationCallback.EVENT.register((dispatcher, _) -> dispatcher.register(
				literal(PortalCubed.ID + "_client")
						.then(ConfigCommand.build())
						.then(literal("debug").then(ResyncEntitiesCommand.build()))
		));
	}
}
