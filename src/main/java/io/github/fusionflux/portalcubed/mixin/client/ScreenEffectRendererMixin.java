package io.github.fusionflux.portalcubed.mixin.client;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.ScreenEffectRenderer;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;

import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockState;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ScreenEffectRenderer.class)
public class ScreenEffectRendererMixin {
	@WrapOperation(
			method = "getViewBlockingState",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/world/level/block/state/BlockState;isViewBlocking(Lnet/minecraft/world/level/BlockGetter;Lnet/minecraft/core/BlockPos;)Z"
			)
	)
	private static boolean dontRenderInPortals(BlockState state, BlockGetter level, BlockPos pos, Operation<Boolean> original, Player player) {
		boolean blocksView = original.call(state, level, pos);
		if (!blocksView)
			return false; // already fine

		if (!(level instanceof ClientLevel clientLevel))
			return true; // ???

		// TODO: collision
		return true;
	}
}
