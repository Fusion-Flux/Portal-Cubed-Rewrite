package io.github.fusionflux.portalcubed.mixin;

import io.github.fusionflux.portalcubed.content.portal.manager.ServerPortalManager;
import io.github.fusionflux.portalcubed.framework.extension.ServerLevelExt;
import net.minecraft.server.level.ServerLevel;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerLevel.class)
public class ServerLevelMixin implements ServerLevelExt {
	@Unique
	private ServerPortalManager portalManager;

	@Inject(method = "<init>", at = @At("TAIL"))
	private void init(CallbackInfo ci) {
		this.portalManager = new ServerPortalManager((ServerLevel) (Object) this);
	}

	@Override
	public ServerPortalManager pc$portalManager() {
		return this.portalManager;
	}
}
