package io.github.fusionflux.portalcubed.mixin;

import java.util.OptionalInt;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import io.github.fusionflux.portalcubed.content.prop.Prop;
import io.github.fusionflux.portalcubed.framework.extension.AmbientSoundEmitter;
import io.github.fusionflux.portalcubed.framework.extension.CollisionListener;
import io.github.fusionflux.portalcubed.framework.extension.PlayerExt;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Entity.RemovalReason;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

@Mixin(Entity.class)
public class EntityMixin {
	@Shadow @Final private static EntityDataAccessor<Boolean> DATA_SILENT;
	@Shadow private boolean horizontalCollision;
	@Shadow private boolean verticalCollision;
	@Shadow private boolean verticalCollisionBelow;

	@Unique
	private boolean isColliding = false;

	@Inject(method = "onSyncedDataUpdated(Lnet/minecraft/network/syncher/EntityDataAccessor;)V", at = @At("RETURN"))
	private void startSoundWhenUnSilenced(EntityDataAccessor<?> data, CallbackInfo ci) {
		var self = (Entity) (Object) this;
		if (self.level().isClientSide && self instanceof AmbientSoundEmitter ambientSoundEmitter) {
			if (DATA_SILENT.equals(data) && !self.getEntityData().get(DATA_SILENT))
				ambientSoundEmitter.playAmbientSound();
		}
	}

	@Inject(
		method = "move",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/world/entity/Entity;checkFallDamage(DZLnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/core/BlockPos;)V",
			shift = At.Shift.BEFORE
		)
	)
	private void listenForCollisions(MoverType movementType, Vec3 movement, CallbackInfo ci) {
		if (this instanceof CollisionListener collisionListener) {
			if (horizontalCollision || verticalCollision || verticalCollisionBelow) {
				if (!isColliding) collisionListener.onCollision();
				isColliding = true;
			} else {
				isColliding = false;
			}
		}
	}

	@Inject(method = "canCollideWith", at = @At("RETURN"), cancellable = true)
	private void dontCollideWithHeldProp(Entity other, CallbackInfoReturnable<Boolean> cir) {
		if (this instanceof PlayerExt ext && other.getId() == ext.pc$heldProp().orElse(-1))
			cir.setReturnValue(false);
	}

	@Inject(method = "setRemoved", at = @At("HEAD"))
	private void dropPropWhenRemoved(RemovalReason reason, CallbackInfo ci) {
		if (this instanceof PlayerExt ext) {
			ext.pc$heldProp().ifPresent(heldPropId -> {
				var heldProp = (Prop) ((Entity) ext).level().getEntity(heldPropId);
				heldProp.drop((Player) ext);
				ext.pc$heldProp(OptionalInt.empty());
			});
		} else if ((Object) this instanceof Prop prop) {
			prop.getHeldBy().ifPresent(playerHoldingId -> {
				var playerHolding = (PlayerExt) prop.level().getEntity(playerHoldingId);
				((PlayerExt) playerHolding).pc$heldProp(OptionalInt.empty());
			});
		}
	}
}
