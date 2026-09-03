package io.github.fusionflux.portalcubed.content.portal.gun.crosshair;

import java.util.EnumSet;
import java.util.Optional;
import java.util.Set;

import io.github.fusionflux.portalcubed.content.portal.Polarity;
import io.github.fusionflux.portalcubed.content.portal.PortalId;
import io.github.fusionflux.portalcubed.content.portal.PortalSettings;
import io.github.fusionflux.portalcubed.content.portal.gun.PortalGunSettings;
import io.github.fusionflux.portalcubed.content.portal.manager.PortalManager;
import io.github.fusionflux.portalcubed.framework.util.ClientTicks;
import io.github.fusionflux.portalcubed.framework.util.Or;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;

@Environment(EnvType.CLIENT)
public final class PortalGunCrosshairRenderer {
	private static final int SPRITE_SIZE = 31;

	private PortalGunCrosshairRenderer() {}

	private static void blit(GuiGraphicsExtractor graphics, Identifier sprite, int color) {
		graphics.blitSprite(RenderPipelines.GUI_TEXTURED, sprite, (graphics.guiWidth() - SPRITE_SIZE) / 2, (graphics.guiHeight() - SPRITE_SIZE) / 2, SPRITE_SIZE, SPRITE_SIZE, color);
	}

	private static void extractIndicator(GuiGraphicsExtractor graphics, PortalGunCrosshairType.Indicator indicator, boolean placed, boolean lastPlaced, int color) {
		if (placed) {
			blit(graphics, indicator.placed(), color);
			if (lastPlaced && indicator.lastPlaced().isPresent())
				blit(graphics, indicator.lastPlaced().get(), color);
		} else {
			blit(graphics, indicator.empty(), color);
		}
	}

	/// @return true to cancel vanilla crosshair rendering
	public static boolean extractRenderState(GuiGraphicsExtractor graphics, LocalPlayer player, PortalGunSettings settings, PortalGunCrosshair crosshair) {
		PortalGunCrosshairType type = PortalGunCrosshairTypeManager.INSTANCE.get(crosshair.typeId());
		if (type == null)
			return false;

		type.base().ifPresent(base -> blit(graphics, base, 0xFFFFFFFF));

		Polarity shotPolarity = settings.lastShot().orElse(null);
		boolean hasBoth = settings.portals() instanceof Or.Both;
		boolean enableLastPlaced = hasBoth && crosshair.enableLastPlaced();
		Set<Polarity> active = determineActiveIndicators(player, settings);

		float ticks = ClientTicks.get();
		for (Polarity polarity : Polarity.values()) {
			int color = settings.portalSettingsPreferring(polarity).color().getOpaque(ticks);
			boolean placed = active.contains(polarity);
			boolean lastPlaced = enableLastPlaced && (shotPolarity == polarity);
			extractIndicator(graphics, type.indicatorOf(polarity), placed, lastPlaced, color);
		}

		return type.removeVanillaCrosshair();
	}

	private static Set<Polarity> determineActiveIndicators(Player player, PortalGunSettings settings) {
		PortalManager manager = player.level().portalManager();

		// if the gun only has one portal, we want both indicators to match
		Optional<Polarity> singlePortalPolarity = settings.polarityOfSinglePortal();
		if (singlePortalPolarity.isPresent()) {
			Polarity polarity = singlePortalPolarity.get();
			PortalSettings portalSettings = settings.portalSettingsOf(polarity).orElseThrow();
			PortalId id = new PortalId(portalSettings.pairFor(player), polarity);
			if (manager.getPortal(id) != null) {
				return EnumSet.allOf(Polarity.class);
			} else {
				return Set.of();
			}
		}

		Set<Polarity> set = EnumSet.noneOf(Polarity.class);

		for (Polarity polarity : Polarity.values()) {
			// if we get here, the gun must have two portals, so this should never throw
			PortalSettings portalSettings = settings.portalSettingsOf(polarity).orElseThrow();
			PortalId id = new PortalId(portalSettings.pairFor(player), polarity);
			if (manager.getPortal(id) != null) {
				set.add(polarity);
			}
		}

		return set;
	}
}
