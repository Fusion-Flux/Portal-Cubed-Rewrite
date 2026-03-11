package io.github.fusionflux.portalcubed.mixin.portals.interaction;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import io.github.fusionflux.portalcubed.content.portal.ref.PortalPathHolder;
import io.github.fusionflux.portalcubed.framework.extension.HitResultExt;
import net.minecraft.world.phys.HitResult;

@Mixin(HitResult.class)
public class HitResultMixin implements HitResultExt {
	@Unique
	private PortalPathHolder portalPath = PortalPathHolder.Unknown.INSTANCE;

	@Override
	public PortalPathHolder portalPath() {
		return this.portalPath;
	}

	@Override
	public void setPortalPath(PortalPathHolder holder) {
		this.portalPath = holder;
	}
}
