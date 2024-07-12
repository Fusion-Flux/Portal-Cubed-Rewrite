package io.github.fusionflux.portalcubed.mixin.client;

import io.github.fusionflux.portalcubed.content.crowbar.CrowbarItem;
import io.github.fusionflux.portalcubed.framework.extension.ScreenExt;
import io.github.fusionflux.portalcubed.framework.gui.widget.TickableWidget;
import io.github.fusionflux.portalcubed.framework.item.DirectClickItem;
import io.github.fusionflux.portalcubed.packet.PortalCubedPackets;
import io.github.fusionflux.portalcubed.packet.serverbound.DirectClickItemPacket;
import net.minecraft.client.Minecraft;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;

import net.minecraft.world.phys.BlockHitResult;

import net.minecraft.world.phys.HitResult;

import org.jetbrains.annotations.Nullable;
import org.quiltmc.qsl.base.api.util.TriState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(Minecraft.class)
public class MinecraftMixin {
	@Shadow
	@Nullable
	public ClientLevel level;

	@Shadow
	@Nullable
	public LocalPlayer player;

	@Shadow
	@Nullable
	public Screen screen;

	@Shadow
	@Nullable
	public HitResult hitResult;

	@Inject(method = "method_1572", at = @At("TAIL"))
	private void handleScreenTickables(CallbackInfo ci) {
		// this is done because injecting into screen#tick is unreliable, most don't call super.
		if (this.screen instanceof ScreenExt ext) {
			var tickables = ext.pc$tickables();
			if (tickables != null)
				tickables.forEach(TickableWidget::tick);
		}
	}

	@Inject(
			method = "startAttack",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/world/phys/HitResult;getType()Lnet/minecraft/world/phys/HitResult$Type;"
			),
			locals = LocalCapture.CAPTURE_FAILHARD,
			cancellable = true
	)
	private void onAttack(CallbackInfoReturnable<Boolean> cir, ItemStack stack) {
		if (stack.getItem() instanceof DirectClickItem direct) {
			TriState result = direct.onAttack(level, player, stack);
			if (result != TriState.DEFAULT) {
				if (result == TriState.TRUE) {
					PortalCubedPackets.sendToServer(new DirectClickItemPacket(true, InteractionHand.MAIN_HAND));
				}
				// Crowbar check or else there will be no delay between startAttack and continueAttack causing a double swing
				cir.setReturnValue(direct instanceof CrowbarItem || result.toBoolean());
			}
		}
	}

	@Inject(
			method = "startUseItem",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/world/phys/HitResult;getType()Lnet/minecraft/world/phys/HitResult$Type;"
			),
			locals = LocalCapture.CAPTURE_FAILHARD,
			cancellable = true
	)
	private void onUse(CallbackInfo ci, InteractionHand[] hands, int var2, int var3, InteractionHand hand, ItemStack stack) {
		if (stack.getItem() instanceof DirectClickItem direct) {
			TriState result = direct.onUse(level, player, stack, hand);
			if (result != TriState.DEFAULT) {
				if (result == TriState.TRUE) {
					PortalCubedPackets.sendToServer(new DirectClickItemPacket(false, hand));
				}
				ci.cancel();
			}
		}
	}

	@Inject(
			method = "continueAttack",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/client/player/LocalPlayer;swing(Lnet/minecraft/world/InteractionHand;)V",
					shift = At.Shift.AFTER
			)
	)
	private void onContinueAttack(CallbackInfo ci) {
		ItemStack stack = this.player.getItemInHand(InteractionHand.MAIN_HAND);
		boolean didSwing = this.player.swingTime == -1; // best way to check because swing doesn't return a boolean
		if (stack.getItem() instanceof CrowbarItem crowbar && this.hitResult instanceof BlockHitResult hit && didSwing)
			crowbar.onSwing(this.player, hit, true);
	}
}
