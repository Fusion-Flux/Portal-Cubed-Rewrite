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
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.Hud;
import net.minecraft.client.player.LocalPlayer;

@Mixin(Hud.class)
public class HudMixin {
	@Shadow
	@Final
	private Minecraft minecraft;

	@Inject(
			method = "extractCrosshair",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/client/gui/GuiGraphicsExtractor;blitSprite(Lcom/mojang/renderpearl/api/pipeline/RenderPipeline;Lnet/minecraft/resources/Identifier;IIII)V",
					ordinal = 0
			),
			cancellable = true
	)
	private void extractPortalGunCrossHair(GuiGraphicsExtractor graphics, DeltaTracker deltaTracker, CallbackInfo ci) {
		LocalPlayer player = this.minecraft.player;
		if (player == null || player.isSpectator())
			return;

		PortalGunSettings settings = PortalGunItem.getGunSettings(player.getMainHandItem());
		if (settings == null)
			return;

		if (PortalGunCrosshairRenderer.extractRenderState(graphics, player, settings, settings.crosshair())) {
			ci.cancel();
		}
	}
}
