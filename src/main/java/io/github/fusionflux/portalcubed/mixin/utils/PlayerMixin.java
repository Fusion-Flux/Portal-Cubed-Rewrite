package io.github.fusionflux.portalcubed.mixin.utils;

import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import io.github.fusionflux.portalcubed.framework.entity.HoldableEntity;
import io.github.fusionflux.portalcubed.framework.extension.PlayerExt;
import net.minecraft.world.entity.player.Player;

@Mixin(Player.class)
public abstract class PlayerMixin implements PlayerExt {
	@Unique
	@Nullable
	private HoldableEntity heldEntity;

	@Unique
	private boolean hasSubmergedTheOperationalEndOfTheDevice;

	@Override
	public void setHeldEntity(@Nullable HoldableEntity heldEntity) {
		this.heldEntity = heldEntity;
	}

	@Override
	@Nullable
	public HoldableEntity getHeldEntity() {
		return this.heldEntity;
	}

	@Override
	public void pc$setHasSubmergedTheOperationalEndOfTheDevice(boolean hasSubmergedTheOperationalEndOfTheDevice) {
		this.hasSubmergedTheOperationalEndOfTheDevice = hasSubmergedTheOperationalEndOfTheDevice;
	}

	@Override
	public boolean pc$hasSubmergedTheOperationalEndOfTheDevice() {
		return this.hasSubmergedTheOperationalEndOfTheDevice;
	}
}
