package io.github.fusionflux.portalcubed.mixin.client;

import java.util.Collection;
import java.util.List;

import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import io.github.fusionflux.portalcubed.content.portal.PortalHitResult;
import io.github.fusionflux.portalcubed.content.portal.manager.ClientPortalManager;
import io.github.fusionflux.portalcubed.framework.extension.ClientSuggestionProviderExt;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.multiplayer.ClientSuggestionProvider;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.phys.Vec3;

@Mixin(ClientSuggestionProvider.class)
public class ClientSuggestionProviderMixin implements ClientSuggestionProviderExt {
	@Shadow
	@Final
	private Minecraft minecraft;

	@Override
	@Nullable
	public String pc$getTargetedPortal() {
		ClientLevel level = this.minecraft.level;
		LocalPlayer player = this.minecraft.player;
		if (level == null || player == null)
			return null;

		Vec3 from = player.getEyePosition();
		Vec3 normal = player.getLookAngle().normalize();
		Vec3 to = from.add(normal.scale(16));

		ClientPortalManager manager = level.portalManager();
		// TODO: check for inactive portals too
		PortalHitResult hit = manager.activePortals().clip(from, to);
		return hit == null ? null : hit.pairKey();
	}

	@Override
	public Collection<String> pc$getAllPortalKeys() {
		ClientLevel level = this.minecraft.level;
		if (level == null)
			return List.of();

		return level.portalManager().getAllKeys();
	}
}
