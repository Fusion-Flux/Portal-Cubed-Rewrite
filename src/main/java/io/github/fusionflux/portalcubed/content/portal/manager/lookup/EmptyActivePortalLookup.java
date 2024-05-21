package io.github.fusionflux.portalcubed.content.portal.manager.lookup;

import io.github.fusionflux.portalcubed.content.portal.PortalHitResult;

import org.jetbrains.annotations.Nullable;

import net.minecraft.world.phys.Vec3;

public enum EmptyActivePortalLookup implements ActivePortalLookup {
	INSTANCE;

	@Override
	@Nullable
	public PortalHitResult clip(Vec3 from, Vec3 to) {
		return null;
	}
}
