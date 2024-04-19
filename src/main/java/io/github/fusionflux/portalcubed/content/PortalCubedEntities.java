package io.github.fusionflux.portalcubed.content;

import io.github.fusionflux.portalcubed.content.portal.projectile.PortalProjectile;
import io.github.fusionflux.portalcubed.content.portal.projectile.PortalProjectileRenderer;
import io.github.fusionflux.portalcubed.content.prop.entity.Prop;
import io.github.fusionflux.portalcubed.content.prop.PropRenderer;
import io.github.fusionflux.portalcubed.content.prop.PropType;
import net.minecraft.Util;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;

import org.quiltmc.qsl.entity.extensions.api.QuiltEntityTypeBuilder;

import static io.github.fusionflux.portalcubed.PortalCubed.REGISTRAR;

import java.util.EnumMap;
import java.util.Map;

public class PortalCubedEntities {
	public static final EntityType<PortalProjectile> PORTAL_PROJECTILE = REGISTRAR.entities.create("portal_projectile", PortalProjectile::new)
			.configure(QuiltEntityTypeBuilder::disableSaving)
			.configure(QuiltEntityTypeBuilder::disableSummon)
			.size(EntityDimensions.fixed(0.5f, 0.5f))
			.renderer(() -> () -> PortalProjectileRenderer::new)
			.build();

	public static final Map<PropType, EntityType<Prop>> PROPS = Util.make(new EnumMap<>(PropType.class), map -> {
		for (PropType type : PropType.values()) {
			EntityType<Prop> entityType = REGISTRAR.entities.create(type.toString(), type.factory)
					.category(MobCategory.MISC)
					.size(type.dimensions)
					.renderer(() -> () -> PropRenderer::new)
					.build();
			map.put(type, entityType);
		}
	});

	public static void init() {
	}
}
