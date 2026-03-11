package io.github.fusionflux.portalcubed.mixin.portals.interaction;

import java.util.Optional;
import java.util.OptionalDouble;
import java.util.function.ToDoubleFunction;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;

import io.github.fusionflux.portalcubed.content.portal.interaction.PortalInteractionUtils;
import io.github.fusionflux.portalcubed.content.portal.interaction.packet.PortalAwareInteractPacket;
import io.github.fusionflux.portalcubed.content.portal.ref.PortalPath;
import io.github.fusionflux.portalcubed.content.portal.transform.PortalTransform;
import net.minecraft.core.Direction;
import net.minecraft.world.damagesource.DamageSource;
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
		ToDoubleFunction<Vec3> distanceFunction = p -> original.call(box, p);
		OptionalDouble distanceSqr = PortalInteractionUtils.findPathLengthSqr(this.level(), pos, distanceFunction, range);
		if (distanceSqr.isEmpty()) {
			return originalDistSqr;
		}

		return Math.min(originalDistSqr, distanceSqr.getAsDouble());
	}

	@ModifyExpressionValue(
			method = "attack",
			at = @At(
					value = "INVOKE",
					target = "Ljava/util/Optional;orElse(Ljava/lang/Object;)Ljava/lang/Object;"
			)
	)
	private Object teleportDamageSource(Object original) {
		if (!(original instanceof DamageSource source))
			return original;

		Vec3 sourcePos = source.getSourcePosition();
		if (sourcePos == null)
			return source;

		Optional<PortalPath> path = PortalAwareInteractPacket.currentPath();
		if (path.isEmpty())
			return source;

		PortalTransform transform = path.get().transform();
		Vec3 teleportedPos = transform.applyAbsolute(sourcePos);
		return new DamageSource(source.typeHolder(), source.getDirectEntity(), source.getEntity(), teleportedPos);
	}

	@ModifyExpressionValue(
			method = "attack",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/world/entity/player/Player;getYRot()F"
			)
	)
	private float teleportRotation(float original) {
		Optional<PortalPath> path = PortalAwareInteractPacket.currentPath();

		if (path.isPresent()) {
			PortalTransform transform = path.get().transform();
			return transform.apply(original, Direction.Axis.Y);
		}

		return original;
	}
}
