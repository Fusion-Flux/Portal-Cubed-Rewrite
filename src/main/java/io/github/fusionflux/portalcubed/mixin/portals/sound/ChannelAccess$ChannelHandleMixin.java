package io.github.fusionflux.portalcubed.mixin.portals.sound;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import io.github.fusionflux.portalcubed.framework.extension.ChannelHandleExt;
import net.minecraft.client.sounds.ChannelAccess;

@Mixin(ChannelAccess.ChannelHandle.class)
public final class ChannelAccess$ChannelHandleMixin implements ChannelHandleExt {
	@Unique
	private boolean teleportedLastTick;

	@Override
	public boolean pc$teleportedLastTick() {
		return this.teleportedLastTick;
	}

	@Override
	public void pc$setTeleportedLastTick(boolean value) {
		this.teleportedLastTick = value;
	}
}
