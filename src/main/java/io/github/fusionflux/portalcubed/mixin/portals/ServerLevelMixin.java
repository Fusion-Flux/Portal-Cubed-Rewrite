package io.github.fusionflux.portalcubed.mixin.portals;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.llamalad7.mixinextras.sugar.Local;

import io.github.fusionflux.portalcubed.content.portal.manager.PortalSavedData;
import io.github.fusionflux.portalcubed.content.portal.manager.ServerPortalManager;
import io.github.fusionflux.portalcubed.framework.extension.ServerLevelExt;
import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedDataType;
import net.minecraft.world.level.storage.SavedDataStorage;
import net.minecraft.world.phys.AABB;

@Mixin(ServerLevel.class)
public abstract class ServerLevelMixin implements ServerLevelExt {
	@Shadow
	@Final
	private MinecraftServer server;

	@Shadow
	public abstract SavedDataStorage getDataStorage();

	@Unique
	private ServerPortalManager portalManager;

	@Inject(method = "<init>", at = @At("TAIL"))
	private void init(CallbackInfo ci) {
		SavedDataType<PortalSavedData> type = PortalSavedData.createType((ServerLevel) (Object) this);
		this.portalManager = this.getDataStorage().computeIfAbsent(type).manager;
	}

	@Inject(method = "updatePOIOnBlockStateChange", at = @At("HEAD"))
	private void updatePortals(CallbackInfo ci, @Local(argsOnly = true, name = "pos") BlockPos pos) {
		// this method is also called from worldgen
		if (!this.server.isSameThread())
			return;

		AABB area = new AABB(pos).inflate(0.5);
		this.portalManager.validatePortals(area);
	}

	@Override
	public ServerPortalManager portalManager() {
		return this.portalManager;
	}
}
