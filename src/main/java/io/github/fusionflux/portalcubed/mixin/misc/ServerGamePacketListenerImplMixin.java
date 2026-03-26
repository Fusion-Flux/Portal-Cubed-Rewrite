package io.github.fusionflux.portalcubed.mixin.misc;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;

import io.github.fusionflux.portalcubed.framework.extension.BigShapeBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.level.block.state.BlockState;

@Mixin(ServerGamePacketListenerImpl.class)
public class ServerGamePacketListenerImplMixin {
	@ModifyExpressionValue(
			method = "handleUseItemOn",
			at = @At(
					value = "CONSTANT",
					args = "doubleValue=1.0000001"
			)
	)
	private double increaseAllowedUseRange(double original, @Local ServerLevel level, @Local BlockPos pos) {
		// vanilla normally prevents using items on blocks when the hit result landed over a block away from the center.
		// we need to expand this safe area for big blocks to not cause errors when items are used on the far parts of shapes.
		BlockState state = level.getBlockState(pos);
		if (state.getBlock() instanceof BigShapeBlock) {
			return 1.51;
		}

		return original;
	}
}
