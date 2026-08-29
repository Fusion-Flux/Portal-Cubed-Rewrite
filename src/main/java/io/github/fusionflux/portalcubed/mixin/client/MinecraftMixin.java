package io.github.fusionflux.portalcubed.mixin.client;

import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.llamalad7.mixinextras.sugar.Local;

import io.github.fusionflux.portalcubed.content.PortalCubedReloadListeners;
import io.github.fusionflux.portalcubed.content.misc.CrowbarItem;
import io.github.fusionflux.portalcubed.framework.item.AttackListeningItem;
import io.github.fusionflux.portalcubed.mixin.utils.accessors.LivingEntityAccessor;
import io.github.fusionflux.portalcubed.packet.PortalCubedPackets;
import io.github.fusionflux.portalcubed.packet.serverbound.CustomAttackPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.util.TriState;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;

@Mixin(Minecraft.class)
public class MinecraftMixin {
	@Shadow
	@Nullable
	public ClientLevel level;

	@Shadow
	public LocalPlayer player;

	@Shadow
	@Nullable
	public HitResult hitResult;

	@Inject(
			method = "<init>",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/server/packs/resources/ReloadableResourceManager;createReload(Ljava/util/concurrent/Executor;Ljava/util/concurrent/Executor;Ljava/util/concurrent/CompletableFuture;Ljava/util/List;)Lnet/minecraft/server/packs/resources/ReloadInstance;"
			)
	)
	private void registerAssetReloadListeners(CallbackInfo ci) {
		PortalCubedReloadListeners.registerAssets();
	}

	@Inject(
			method = "startAttack",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/world/phys/HitResult;getType()Lnet/minecraft/world/phys/HitResult$Type;"
			),
			cancellable = true
	)
	private void directAttack(CallbackInfoReturnable<Boolean> cir, @Local ItemStack stack) {
		if (stack.getItem() instanceof AttackListeningItem direct) {
			TriState result = direct.onAttack(this.level, this.player, stack, this.hitResult);
			if (result != TriState.DEFAULT) {
				if (result == TriState.TRUE) {
					PortalCubedPackets.sendToServer(new CustomAttackPacket(InteractionHand.MAIN_HAND, this.hitResult));
				}

				cir.setReturnValue(result.toBoolean(false));
			}
		}
	}

	@Inject(method = "continueAttack", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;swing(Lnet/minecraft/world/InteractionHand;Lnet/minecraft/world/item/component/SwingAnimation;Z)Z"))
	private void onContinueAttack(CallbackInfo ci) {
		ItemStack stack = this.player.getItemInHand(InteractionHand.MAIN_HAND);
		int swingDuration = ((LivingEntityAccessor) this.player).callGetCurrentSwingDuration();
		if (stack.getItem() instanceof CrowbarItem crowbar && this.hitResult instanceof BlockHitResult hit && this.player.swingTime >= swingDuration / 2)
			crowbar.onSwing(this.player, hit, true);
	}
}
