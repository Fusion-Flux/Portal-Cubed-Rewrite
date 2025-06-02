package io.github.fusionflux.portalcubed.mixin.portals.client;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import io.github.fusionflux.portalcubed.content.portal.gun.PortalGunItem;
import io.github.fusionflux.portalcubed.content.portal.gun.PortalGunSettings;
import io.github.fusionflux.portalcubed.content.portal.gun.crosshair.PortalGunCrosshairRenderer;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.LocalPlayer;

@Mixin(Gui.class)
public class GuiMixin {
	@Shadow
	@Final
	private Minecraft minecraft;

	@Inject(
			method = "renderCrosshair",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/client/gui/GuiGraphics;blitSprite(Ljava/util/function/Function;Lnet/minecraft/resources/ResourceLocation;IIII)V",
					ordinal = 0
			),
			cancellable = true
	)
	private void renderPortalGunCrossHair(GuiGraphics graphics, DeltaTracker deltaTracker, CallbackInfo ci) {
		LocalPlayer player = this.minecraft.player;
		if (player == null)
			return;

		PortalGunSettings settings = PortalGunItem.getGunSettings(player.getMainHandItem());
		if (settings == null)
			return;

		if (PortalGunCrosshairRenderer.render(graphics, player, settings, settings.crosshair()))
			ci.cancel();
	}
}
