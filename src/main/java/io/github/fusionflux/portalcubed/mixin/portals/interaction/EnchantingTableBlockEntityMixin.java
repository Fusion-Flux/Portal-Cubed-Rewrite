package io.github.fusionflux.portalcubed.mixin.portals.interaction;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;

import io.github.fusionflux.portalcubed.content.portal.PortalTeleportHandler;
import io.github.fusionflux.portalcubed.content.portal.interaction.PortalInteractionUtils;
import io.github.fusionflux.portalcubed.content.portal.ref.PortalPath;
import io.github.fusionflux.portalcubed.content.portal.transform.PortalTransform;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.EnchantingTableBlockEntity;
import net.minecraft.world.phys.Vec3;

@Mixin(EnchantingTableBlockEntity.class)
public class EnchantingTableBlockEntityMixin {
	@WrapOperation(
			method = "bookAnimationTick",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/world/level/Level;getNearestPlayer(DDDDZ)Lnet/minecraft/world/entity/player/Player;"
			)
	)
	private static Player findPlayersThroughPortals(Level level, double x, double y, double z, double radius, boolean creative, Operation<Player> original,
													@Share("pos") LocalRef<Vec3> transformedPos) {
		Player nearest = original.call(level, x, y, z, radius, creative);
		Vec3 pos = new Vec3(x, y, z);
		PortalPath.With<Player> throughPortals = PortalInteractionUtils.getNearestPlayer(level, pos, radius, creative);
		if (throughPortals == null)
			return nearest;

		Player player = throughPortals.value();
		if (nearest == null || throughPortals.path().length(pos, player.position()) < nearest.position().distanceTo(pos)) {
			PortalTransform transform = throughPortals.path().createTransform();
			// use the center since it has better results with non-upright-wall portals
			Vec3 center = PortalTeleportHandler.centerOf(player);
			transformedPos.set(transform.inverse().applyAbsolute(center));
			return player;
		}

		return nearest;
	}

	@ModifyExpressionValue(
			method = "bookAnimationTick",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/world/entity/player/Player;getX()D"
			)
	)
	private static double useTransformedX(double x, @Share("pos") LocalRef<Vec3> transformedPos) {
		Vec3 pos = transformedPos.get();
		return pos == null ? x : pos.x;
	}

	@ModifyExpressionValue(
			method = "bookAnimationTick",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/world/entity/player/Player;getZ()D"
			)
	)
	private static double useTransformedZ(double z, @Share("pos") LocalRef<Vec3> transformedPos) {
		Vec3 pos = transformedPos.get();
		return pos == null ? z : pos.z;
	}
}
