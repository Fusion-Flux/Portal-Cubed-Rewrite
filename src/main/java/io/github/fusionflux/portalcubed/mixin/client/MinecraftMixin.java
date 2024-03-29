package io.github.fusionflux.portalcubed.mixin.client;

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

	@Inject(method = "method_1572", at = @At("TAIL"))
	private void handleScreenTickables(CallbackInfo ci) {
		// this is done because injecting into screen#tick is unreliable, most don't call super.
		if (this.screen instanceof ScreenExt ext) {
			ext.pc$tickables().forEach(TickableWidget::tick);
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
				cir.setReturnValue(result.toBoolean());
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
}
