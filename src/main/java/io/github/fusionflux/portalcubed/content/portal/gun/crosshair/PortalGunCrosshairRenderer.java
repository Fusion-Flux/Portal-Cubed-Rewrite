package io.github.fusionflux.portalcubed.content.portal.gun.crosshair;

import com.mojang.blaze3d.systems.RenderSystem;

import io.github.fusionflux.portalcubed.content.portal.Polarity;
import io.github.fusionflux.portalcubed.content.portal.PortalPair;
import io.github.fusionflux.portalcubed.content.portal.gun.PortalGunSettings;
import io.github.fusionflux.portalcubed.framework.util.ClientTicks;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.ARGB;

@Environment(EnvType.CLIENT)
public final class PortalGunCrosshairRenderer {
	private static final int SPRITE_SIZE = 31;

	private static void blit(GuiGraphics graphics, ResourceLocation sprite) {
		graphics.blitSprite(RenderType::guiTextured, sprite, (graphics.guiWidth() - SPRITE_SIZE) / 2, (graphics.guiHeight() - SPRITE_SIZE) / 2, SPRITE_SIZE, SPRITE_SIZE);
	}

	private static void renderIndicator(GuiGraphics graphics, PortalGunCrosshairType.Indicator indicator, boolean placed, boolean lastPlaced, int color) {
		if (placed) {
			blit(graphics, indicator.placed());
			if (lastPlaced && indicator.lastPlaced().isPresent())
				blit(graphics, indicator.lastPlaced().get());
		} else {
			blit(graphics, indicator.empty());
		}

		RenderSystem.setShaderColor(ARGB.redFloat(color), ARGB.greenFloat(color), ARGB.blueFloat(color), 1f);
		graphics.flush();
		RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
	}

	/**
	 * @return false to not draw the vanilla crosshair, true to fall back to vanilla
	 */
	public static boolean render(GuiGraphics graphics, LocalPlayer player, PortalGunSettings settings, PortalGunCrosshair crosshair) {
		PortalGunCrosshairType type = PortalGunCrosshairTypeManager.INSTANCE.get(crosshair.typeId());
		if (type == null)
			return false;

		type.base().ifPresent(base -> {
			blit(graphics, base);
			graphics.flush();
		});

		String pairKey = settings.pair().orElse(player.getGameProfile().getName());
		PortalPair pair = player.level().portalManager().getPairOrEmpty(pairKey);
		Polarity shotPolarity = settings.shot().orElse(null);

		boolean hasSecondary = settings.secondary().isPresent();
		boolean enableLastPlaced = crosshair.enableLastPlaced() && hasSecondary;

		float ticks = ClientTicks.get();
		for (Polarity polarity : Polarity.values()) {
			int color = settings.portalSettingsOf(polarity).color().getOpaque(ticks);
			renderIndicator(graphics, type.indicatorOf(polarity), pair.get(hasSecondary ? polarity : Polarity.PRIMARY).isPresent(), enableLastPlaced && (shotPolarity == polarity), color);
		}

		return type.removeVanillaCrosshair();
	}
}
