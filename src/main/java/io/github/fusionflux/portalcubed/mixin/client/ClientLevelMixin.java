package io.github.fusionflux.portalcubed.mixin.client;

import io.github.fusionflux.portalcubed.content.portal.manager.ClientPortalManager;
import io.github.fusionflux.portalcubed.framework.block.multiblock.AbstractMultiBlock;
import io.github.fusionflux.portalcubed.framework.extension.ClientLevelExt;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;

@Mixin(ClientLevel.class)
public class ClientLevelMixin implements ClientLevelExt {
	@Unique
	private ClientPortalManager portalManager;

	@Inject(method = "<init>", at = @At("TAIL"))
	private void init(CallbackInfo ci) {
		this.portalManager = new ClientPortalManager((ClientLevel) (Object) this);
	}

	@Override
	public ClientPortalManager pc$portalManager() {
		return this.portalManager;
	}

	// Overrides pc$playSoundInstance from LevelExt
	public void pc$playSoundInstance(Object soundInstance) {
		Minecraft.getInstance().getSoundManager().play((SoundInstance) soundInstance);
	}

	@WrapOperation(method = "destroyBlockProgress", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/LevelRenderer;destroyBlockProgress(ILnet/minecraft/core/BlockPos;I)V"))
	private void destroyMultiBlockProgress(LevelRenderer instance, int entityId, BlockPos pos, int progress, Operation<Void> original) {
		var state = ((BlockGetter) this).getBlockState(pos);
		if (state.getBlock() instanceof AbstractMultiBlock multiBlock) {
			for (var quadrantPos : multiBlock.quadrantIterator(multiBlock.getOriginPos(pos, state), state)) {
				original.call(instance, entityId, quadrantPos, progress);
			}
		} else {
			original.call(instance, entityId, pos, progress);
		}
	}
}
