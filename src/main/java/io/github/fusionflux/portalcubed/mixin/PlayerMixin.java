package io.github.fusionflux.portalcubed.mixin;

import java.util.OptionalInt;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import io.github.fusionflux.portalcubed.framework.extension.PlayerExt;
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
}
