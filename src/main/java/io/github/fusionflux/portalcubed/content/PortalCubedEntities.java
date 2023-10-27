package io.github.fusionflux.portalcubed.content;

import io.github.fusionflux.portalcubed.content.portal.entity.Portal;
import io.github.fusionflux.portalcubed.content.portal.entity.PortalRenderer;
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

	public static void init() {
	}
}
