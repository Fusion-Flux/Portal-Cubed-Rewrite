package io.github.fusionflux.portalcubed.mixin.portals;

import java.util.Optional;
import java.util.function.ToDoubleBiFunction;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;

import io.github.fusionflux.portalcubed.content.PortalCubedDamageSources;
import io.github.fusionflux.portalcubed.content.portal.PortalReference;
import io.github.fusionflux.portalcubed.content.portal.manager.lookup.PortalLookup;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageSources;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

@Mixin(Player.class)
public abstract class PlayerMixin extends LivingEntity {
	protected PlayerMixin(EntityType<? extends LivingEntity> entityType, Level level) {
		super(entityType, level);
	}

	@WrapOperation(
			method = "attack",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/world/damagesource/DamageSources;playerAttack(Lnet/minecraft/world/entity/player/Player;)Lnet/minecraft/world/damagesource/DamageSource;"
			)
	)
	private DamageSource changeSourceWhenAttackingSelf(DamageSources sources, Player player, Operation<DamageSource> original, @Local(argsOnly = true) Entity target) {
		if (player != target) {
			return original.call(sources, player);
		}

		return PortalCubedDamageSources.attackSelf(player);
	}

	@WrapOperation(
			method = {
					"canInteractWithBlock",
					"canInteractWithEntity(Lnet/minecraft/world/phys/AABB;D)Z"
			},
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/world/phys/AABB;distanceToSqr(Lnet/minecraft/world/phys/Vec3;)D"
			)
	)
	private double interactThroughPortals(AABB box, Vec3 pos, Operation<Double> original,
										  @Local(ordinal = 1) double range) {
		double originalDistSqr = original.call(box, pos);
		double shortestDistSqr = this.findShortestPath(box, pos, original::call, range);
		if (shortestDistSqr == Double.MAX_VALUE) {
			return originalDistSqr;
		}

		return Math.min(originalDistSqr, shortestDistSqr);
	}

	@Unique
	private double findShortestPath(AABB box, Vec3 pos, ToDoubleBiFunction<AABB, Vec3> distanceFunction, double range) {
		double shortestDistSqr = Double.MAX_VALUE;

		PortalLookup lookup = this.level().portalManager().lookup();
		for (PortalReference portal : lookup.getPortalsAround(pos, range)) {
			Optional<PortalReference> maybeOpposite = portal.opposite();
			if (maybeOpposite.isEmpty())
				continue;

			double distanceToPortal = pos.distanceTo(portal.get().data.origin());
			if (distanceToPortal == 0)
				continue;

			double remainingRange = range - distanceToPortal;
			if (remainingRange <= 0)
				continue;

			PortalReference linked = maybeOpposite.get();
			Vec3 newPos = linked.get().data.origin();
			double directDistSqr = distanceFunction.applyAsDouble(box, newPos);
			double distSqrThroughPortals = this.findShortestPath(box, newPos, distanceFunction, remainingRange);
			shortestDistSqr = Math.min(shortestDistSqr, Math.min(directDistSqr, distSqrThroughPortals));
		}

		return shortestDistSqr;
	}
}
