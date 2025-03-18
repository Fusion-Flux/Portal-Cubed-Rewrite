package io.github.fusionflux.portalcubed.mixin.misc;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;

import io.github.fusionflux.portalcubed.content.PortalCubedBlocks;
import io.github.fusionflux.portalcubed.content.misc.MagnesiumFireBlock;
import net.minecraft.world.level.block.BaseFireBlock;
import net.minecraft.world.level.block.state.BlockState;

@Mixin(BaseFireBlock.class)
public class BaseFireBlockMixin {
	@ModifyReturnValue(method = "getState", at = @At("RETURN"))
	private static BlockState fireTheMagnesium(BlockState original, @Local BlockState supportingBlock) {
		return MagnesiumFireBlock.canSurviveOnBlock(supportingBlock) ? PortalCubedBlocks.MAGNESIUM_FIRE.defaultBlockState() : original;
	}
}
