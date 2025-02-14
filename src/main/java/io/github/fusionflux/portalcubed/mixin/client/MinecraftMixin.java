package io.github.fusionflux.portalcubed.mixin.client;

import java.util.List;

import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;

import io.github.fusionflux.portalcubed.content.PortalCubedReloadListeners;
import io.github.fusionflux.portalcubed.content.misc.CrowbarItem;
import io.github.fusionflux.portalcubed.framework.extension.ScreenExt;
import io.github.fusionflux.portalcubed.framework.gui.widget.TickableWidget;
import io.github.fusionflux.portalcubed.framework.item.DirectClickItem;
import io.github.fusionflux.portalcubed.mixin.utils.accessors.LivingEntityAccessor;
import io.github.fusionflux.portalcubed.packet.PortalCubedPackets;
import io.github.fusionflux.portalcubed.packet.serverbound.DirectClickItemPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
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
	public Screen screen;

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
			method = "tick",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/client/gui/screens/Screen;tick()V",
					shift = At.Shift.AFTER
			)
	)
	private void handleScreenTickables(CallbackInfo ci) {
		// this is done because injecting into screen#tick is unreliable, most don't call super.
		if (this.screen instanceof ScreenExt ext) {
			List<TickableWidget> tickables = ext.pc$tickables();
			if (tickables != null)
				tickables.forEach(TickableWidget::tick);
		}
	}

	@WrapMethod(method = "startAttack")
	private boolean directAttack(Operation<Boolean> original) {
		ItemStack stack = this.player.getMainHandItem();
		if (stack.getItem() instanceof DirectClickItem direct) {
			TriState result = direct.onAttack(this.level, this.player, stack, this.hitResult);
			if (result != TriState.DEFAULT) {
				if (result == TriState.TRUE)
					PortalCubedPackets.sendToServer(new DirectClickItemPacket(true, InteractionHand.MAIN_HAND, this.hitResult));
				return result.toBoolean(false);
			}
		}

		return original.call();
	}

	@WrapMethod(method = "startUseItem")
	private void directUse(Operation<Void> original) {
		boolean cancel = false;

		for (InteractionHand hand : InteractionHand.values()) {
			ItemStack stack = this.player.getItemInHand(hand);
			if (!stack.isEmpty()) {
				if (stack.getItem() instanceof DirectClickItem direct) {
					TriState result = direct.onUse(this.level, this.player, stack, this.hitResult, hand);
					if (result != TriState.DEFAULT) {
						if (result == TriState.TRUE)
							PortalCubedPackets.sendToServer(new DirectClickItemPacket(false, hand, this.hitResult));
						cancel = true;
					}
				}

				break;
			}
		}

		if (!cancel)
			original.call();
	}

	@Inject(method = "continueAttack", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;swing(Lnet/minecraft/world/InteractionHand;)V"))
	private void onContinueAttack(CallbackInfo ci) {
		ItemStack stack = this.player.getItemInHand(InteractionHand.MAIN_HAND);
		int swingDuration = ((LivingEntityAccessor) this.player).callGetCurrentSwingDuration();
		if (stack.getItem() instanceof CrowbarItem crowbar && this.hitResult instanceof BlockHitResult hit && this.player.swingTime >= swingDuration / 2)
			crowbar.onSwing(this.player, hit, true);
	}
}
