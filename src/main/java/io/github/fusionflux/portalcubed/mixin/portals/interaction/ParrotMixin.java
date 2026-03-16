package io.github.fusionflux.portalcubed.mixin.portals.interaction;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;

import io.github.fusionflux.portalcubed.content.portal.interaction.PortalInteractionUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Position;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.Parrot;
import net.minecraft.world.entity.animal.ShoulderRidingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

@Mixin(Parrot.class)
public abstract class ParrotMixin extends ShoulderRidingEntity {
	protected ParrotMixin(EntityType<? extends ShoulderRidingEntity> entityType, Level level) {
		super(entityType, level);
	}

	@WrapOperation(
			method = "aiStep",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/core/BlockPos;closerToCenterThan(Lnet/minecraft/core/Position;D)Z"
			)
	)
	private boolean checkJukeboxDistanceThroughPortals(BlockPos jukebox, Position pos, double maxDistance, Operation<Boolean> original) {
		if (original.call(jukebox, pos, maxDistance)) {
			// close enough without portals
			return true;
		}

		return PortalInteractionUtils.findPath(this.level(), Vec3.atCenterOf(jukebox), (Vec3) pos, maxDistance) != null;
	}
}
