package io.github.fusionflux.portalcubed.mixin;

import java.util.OptionalInt;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import io.github.fusionflux.portalcubed.content.prop.Prop;
import io.github.fusionflux.portalcubed.framework.extension.PlayerExt;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Entity.RemovalReason;
import net.minecraft.world.entity.player.Player;

@Mixin(Entity.class)
public class EntityMixin {
	@Inject(method = "canCollideWith", at = @At("RETURN"), cancellable = true)
	private void dontCollideWithHeldProp(Entity other, CallbackInfoReturnable<Boolean> cir) {
		if (this instanceof PlayerExt ext)
			cir.setReturnValue((other.getId() != ext.pc$heldProp().orElse(-1)) && cir.getReturnValue());
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
