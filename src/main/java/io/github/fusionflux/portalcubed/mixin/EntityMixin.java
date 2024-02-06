package io.github.fusionflux.portalcubed.mixin;

import java.util.OptionalInt;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import io.github.fusionflux.portalcubed.content.prop.PropEntity;
import io.github.fusionflux.portalcubed.framework.extension.PlayerExt;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Entity.RemovalReason;
import net.minecraft.world.entity.player.Player;

@Mixin(Entity.class)
public class EntityMixin {
	@Inject(method = "setRemoved", at = @At("HEAD"))
	private void dropPropWhenRemoved(RemovalReason reason, CallbackInfo ci) {
		if (this instanceof PlayerExt ext) {
			ext.pc$heldProp().ifPresent(heldPropId -> {
				var heldProp = (PropEntity) ((Entity) ext).level().getEntity(heldPropId);
				heldProp.drop((Player) ext);
				ext.pc$heldProp(OptionalInt.empty());
			});
		} else if ((Object) this instanceof PropEntity prop) {
			prop.getHeldBy().ifPresent(playerHoldingId -> {
				var playerHolding = (PlayerExt) prop.level().getEntity(playerHoldingId);
				((PlayerExt) playerHolding).pc$heldProp(OptionalInt.empty());
			});
		}
	}
}
