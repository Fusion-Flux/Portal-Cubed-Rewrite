package io.github.fusionflux.portalcubed.framework.entity;

import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;

import io.github.fusionflux.portalcubed.packet.PortalCubedPackets;
import io.github.fusionflux.portalcubed.packet.serverbound.RequestEntitySyncPacket;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;

public final class ResyncEntitiesCommand {
	public static final Component SINGLE = Component.translatable("commands.portalcubed.client.debug.entity_resync.single");

	public static LiteralArgumentBuilder<FabricClientCommandSource> build() {
		return literal("resync_entities").executes(context -> {
			ClientLevel level = context.getSource().getWorld();

			int count = 0;
			for (Entity entity : level.entitiesForRendering()) {
				count++;
				PortalCubedPackets.sendToServer(new RequestEntitySyncPacket(entity));
			}

			Component message = count == 1 ? SINGLE : Component.translatable("commands.portalcubed.client.debug.entity_resync.multi", count);
			context.getSource().sendFeedback(message);
			return count;
		});
	}
}
