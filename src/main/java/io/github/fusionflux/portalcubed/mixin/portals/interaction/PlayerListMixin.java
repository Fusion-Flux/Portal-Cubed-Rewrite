package io.github.fusionflux.portalcubed.mixin.portals.interaction;

import java.util.OptionalDouble;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.expression.Definition;
import com.llamalad7.mixinextras.expression.Expression;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;

import io.github.fusionflux.portalcubed.content.portal.interaction.PortalInteractionUtils;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

@Mixin(PlayerList.class)
public class PlayerListMixin {
	@Definition(id = "radius", local = @Local(type = double.class, ordinal = 3, argsOnly = true))
	@Expression("? < radius * radius")
	@ModifyExpressionValue(method = "broadcast", at = @At("MIXINEXTRAS:EXPRESSION"))
	private boolean checkRangeThroughPortals(boolean original, @Local ServerPlayer player,
											 @Local(argsOnly = true, ordinal = 0) double x,
											 @Local(argsOnly = true, ordinal = 1) double y,
											 @Local(argsOnly = true, ordinal = 2) double z,
											 @Local(argsOnly = true, ordinal = 3) double radius) {
		if (original) {
			return true;
		}

		Vec3 playerPos = player.position();
		Vec3 eventPos = new Vec3(x, y, z);

		OptionalDouble distance = PortalInteractionUtils.findPathThroughPortals(player.level(), eventPos, playerPos::distanceToSqr, radius);
		if (distance.isEmpty())
			return false;

		return distance.getAsDouble() < Mth.square(radius);
	}
}
