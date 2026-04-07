package io.github.fusionflux.portalcubed.framework.config;

import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.argument;
import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;

import io.github.fusionflux.portalcubed.content.portal.graphics.render.PortalRenderer;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.network.chat.Component;

public final class ConfigCommand {
	public static final Component CONFIG_UPDATED = Component.translatable("commands.portalcubed.client.config.updated");

	public static LiteralArgumentBuilder<FabricClientCommandSource> build() {
		return literal("config").then(
				literal("portal_rendering_levels").then(
						argument("levels", IntegerArgumentType.integer(0, PortalRenderer.MAX_LEVELS)).executes(context -> {
							int levels = IntegerArgumentType.getInteger(context, "levels");
							PortalCubedClientConfig.set(new PortalCubedClientConfig(levels));
							context.getSource().sendFeedback(CONFIG_UPDATED);
							return 1;
						})
				)
		).then(
				literal("reset").executes(context -> {
					PortalCubedClientConfig.reset();
					context.getSource().sendFeedback(CONFIG_UPDATED);
					return 1;
				})
		);
	}
}
