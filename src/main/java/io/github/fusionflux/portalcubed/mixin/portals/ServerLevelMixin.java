package io.github.fusionflux.portalcubed.mixin.portals;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.llamalad7.mixinextras.sugar.Local;

import io.github.fusionflux.portalcubed.content.portal.Portal;
import io.github.fusionflux.portalcubed.content.portal.manager.PortalStorage;
import io.github.fusionflux.portalcubed.content.portal.manager.ServerPortalManager;
import io.github.fusionflux.portalcubed.framework.extension.ServerLevelExt;
import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.storage.DimensionDataStorage;
import net.minecraft.world.phys.AABB;

@Mixin(ServerLevel.class)
public abstract class ServerLevelMixin implements ServerLevelExt {
	@Shadow
	public abstract DimensionDataStorage getDataStorage();

	@Shadow
	@Final
	private MinecraftServer server;
	@Unique
	private ServerPortalManager portalManager;

	@Inject(method = "<init>", at = @At("TAIL"))
	private void init(CallbackInfo ci) {
		this.portalManager = new ServerPortalManager(
				this.getDataStorage().computeIfAbsent(PortalStorage.Persistent.factory(), PortalStorage.Persistent.ID),
				(ServerLevel) (Object) this
		);
	}

	@Inject(method = "onBlockStateChange", at = @At("HEAD"))
	private void updatePortals(CallbackInfo ci, @Local(argsOnly = true) BlockPos pos) {
		// this method is also called from worldgen
		if (!this.server.isSameThread())
			return;

		AABB area = new AABB(pos).inflate(0.5);
		this.portalManager.lookup().getPortals(area).forEach(holder -> {
			Portal portal = holder.portal();
			if (!portal.data.validator().isValid((ServerLevel) (Object) this, holder)) {
				// krill
				this.portalManager.removePortal(holder.pair().key(), holder.polarity());
			}
		});
	}

	@Override
	public ServerPortalManager portalManager() {
		return this.portalManager;
	}
}
