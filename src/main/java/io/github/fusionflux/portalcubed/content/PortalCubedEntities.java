package io.github.fusionflux.portalcubed.content;

import io.github.fusionflux.portalcubed.content.portal.entity.Portal;
import io.github.fusionflux.portalcubed.content.portal.entity.PortalRenderer;
import io.github.fusionflux.portalcubed.content.portal.projectile.PortalProjectile;
import io.github.fusionflux.portalcubed.content.portal.projectile.PortalProjectileRenderer;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;

import org.quiltmc.qsl.entity.api.QuiltEntityTypeBuilder;

import static io.github.fusionflux.portalcubed.PortalCubed.REGISTRAR;

public class PortalCubedEntities {
	public static final EntityType<Portal> PORTAL = REGISTRAR.entities.create("portal", Portal::new)
			.configure(QuiltEntityTypeBuilder::disableSaving)
			.size(EntityDimensions.scalable(1, 2))
			.renderer(() -> () -> PortalRenderer::new)
			.build();

	public static final EntityType<PortalProjectile> PORTAL_PROJECTILE = REGISTRAR.entities.create("portal_projectile", PortalProjectile::new)
			.configure(QuiltEntityTypeBuilder::disableSaving)
			.size(EntityDimensions.fixed(0.5f, 0.5f))
			.renderer(() -> () -> PortalProjectileRenderer::new)
			.build();

	public static void init() {
	}
}
