package io.github.fusionflux.portalcubed.mixin.portals.interaction;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;

import io.github.fusionflux.portalcubed.content.portal.ref.PortalPath;
import io.github.fusionflux.portalcubed.content.portal.ref.PortalPathHolder;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.phys.BlockHitResult;

@Mixin(UseOnContext.class)
public abstract class UseOnContextMixin {
	@Shadow
	protected abstract BlockHitResult getHitResult();

	@ModifyReturnValue(method = "getRotation", at = @At("RETURN"))
	private float teleportRotation(float original) {
		PortalPathHolder holder = this.getHitResult().portalPath();
		if (!(holder instanceof PortalPathHolder.Present(PortalPath path)))
			return original;

		return path.transform().apply(original, Direction.Axis.Y);
	}

	@ModifyExpressionValue(
			method = "getHorizontalDirection",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/world/entity/player/Player;getDirection()Lnet/minecraft/core/Direction;"
			)
	)
	protected Direction teleportDirection(Direction original) {
		PortalPathHolder holder = this.getHitResult().portalPath();
		if (!(holder instanceof PortalPathHolder.Present(PortalPath path)))
			return original;

		return path.transform().apply(original);
	}
}
