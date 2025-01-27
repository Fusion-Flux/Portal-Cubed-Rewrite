package io.github.fusionflux.portalcubed.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;

import net.minecraft.gametest.framework.GameTestInfo;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.phys.AABB;

@Mixin(GameTestInfo.class)
public abstract class GameTestInfoMixin {
	@Shadow
	public abstract ServerLevel getLevel();

	@ModifyExpressionValue(
			method = "succeed",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/gametest/framework/GameTestInfo;getStructureBounds()Lnet/minecraft/world/phys/AABB;"
			)
	)
	private AABB clearPortals(AABB bounds) {
		this.getLevel().portalManager().removePortalsInBox(bounds);
		return bounds;
	}
}
