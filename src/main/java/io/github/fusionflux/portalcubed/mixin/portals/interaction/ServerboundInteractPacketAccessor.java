package io.github.fusionflux.portalcubed.mixin.portals.interaction;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.network.protocol.game.ServerboundInteractPacket;

@Mixin(ServerboundInteractPacket.class)
public interface ServerboundInteractPacketAccessor {
	@Accessor
	ServerboundInteractPacket.Action getAction();
}
