package io.github.fusionflux.portalcubed.mixin;

import java.util.OptionalInt;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import io.github.fusionflux.portalcubed.content.prop.Prop;
import io.github.fusionflux.portalcubed.framework.extension.PlayerExt;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;

@Mixin(Player.class)
public class PlayerMixin implements PlayerExt {
	@Unique
	private OptionalInt heldProp = OptionalInt.empty();

	@Override
	public void pc$heldProp(OptionalInt prop) {
		heldProp = prop;
	}

	@Override
	public OptionalInt pc$heldProp() {
		return heldProp;
	}

	@Inject(method = "attack", at = @At("HEAD"), cancellable = true)
	private void destroyProps(Entity target, CallbackInfo ci) {
		if (target instanceof Prop prop) {
			var self = (Player) (Object) this;
			prop.hurt(self.damageSources().playerAttack(self), 0);
			ci.cancel();
		}
	}
}
