package io.github.fusionflux.portalcubed.mixin.portals.interaction;

import java.util.OptionalDouble;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.authlib.GameProfile;

import io.github.fusionflux.portalcubed.content.portal.PortalTeleportHandler;
import io.github.fusionflux.portalcubed.content.portal.interaction.PortalInteractionUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

@Mixin(ServerPlayer.class)
public abstract class ServerPlayerMixin extends Player {
	protected ServerPlayerMixin(Level level, BlockPos pos, float yRot, GameProfile gameProfile) {
		super(level, pos, yRot, gameProfile);
	}

	@ModifyReturnValue(method = "isReachableBedBlock", at = @At("RETURN"))
	private boolean sleepThroughPortals(boolean original, @Local(argsOnly = true) BlockPos blockPos) {
		if (original) {
			return true;
		}

		Vec3 bedPos = Vec3.atBottomCenterOf(blockPos);
		Vec3 center = PortalTeleportHandler.centerOf(this);
		OptionalDouble distanceSqr = PortalInteractionUtils.findPathThroughPortals(this.level(), bedPos, center::distanceToSqr, 3);
		if (distanceSqr.isEmpty())
			return false;

		return Math.sqrt(distanceSqr.getAsDouble()) < 3;
	}
}
