package io.github.fusionflux.portalcubed.mixin.portals.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import io.github.fusionflux.portalcubed.content.portal.transform.SinglePortalTransform;
import io.github.fusionflux.portalcubed.framework.extension.PortalTeleportationExt;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.Direction;

@Mixin(LocalPlayer.class)
public final class LocalPlayerMixin implements PortalTeleportationExt {
	@Shadow
	public float xBob;

	@Shadow
	public float xBobO;

	@Shadow
	public float yBob;

	@Shadow
	public float yBobO;

	@Override
	public void applyAdditionalTransforms(SinglePortalTransform transform) {
		PortalTeleportationExt.super.applyAdditionalTransforms(transform);
		this.xBob = transform.apply(this.xBob, Direction.Axis.X);
		this.xBobO = transform.apply(this.xBobO, Direction.Axis.X);
		this.yBob = transform.apply(this.yBob, Direction.Axis.Y);
		this.yBobO = transform.apply(this.yBobO, Direction.Axis.Y);
	}
}
