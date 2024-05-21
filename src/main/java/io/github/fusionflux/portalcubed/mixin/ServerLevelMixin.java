package io.github.fusionflux.portalcubed.mixin;

import io.github.fusionflux.portalcubed.content.portal.manager.ServerPortalManager;
import io.github.fusionflux.portalcubed.framework.extension.ServerLevelExt;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.storage.DimensionDataStorage;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerLevel.class)
public abstract class ServerLevelMixin implements ServerLevelExt {
	@Shadow
	public abstract DimensionDataStorage getDataStorage();

	@Unique
	private ServerPortalManager.PersistentState portalManagerSavedData;

	@Inject(method = "<init>", at = @At("TAIL"))
	private void init(CallbackInfo ci) {
		this.portalManagerSavedData = this.getDataStorage().computeIfAbsent(
				ServerPortalManager.PersistentState.factory((ServerLevel) (Object) this),
				ServerPortalManager.PersistentState.ID
		);
	}

	@Override
	public ServerPortalManager portalManager() {
		return this.portalManagerSavedData.manager;
	}
}
