package io.github.fusionflux.portalcubed.content;

import static io.github.fusionflux.portalcubed.PortalCubed.REGISTRAR;

import java.util.EnumMap;
import java.util.Map;

import net.minecraft.world.entity.EntityType.Builder;

import io.github.fusionflux.portalcubed.PortalCubed;
import io.github.fusionflux.portalcubed.content.lemon.Lemonade;
import io.github.fusionflux.portalcubed.content.portal.projectile.PortalProjectile;
import io.github.fusionflux.portalcubed.content.portal.projectile.PortalProjectileRenderer;
import io.github.fusionflux.portalcubed.content.prop.PropRenderer;
import io.github.fusionflux.portalcubed.content.prop.PropType;
import io.github.fusionflux.portalcubed.content.prop.entity.Prop;
import net.minecraft.Util;
import net.minecraft.client.renderer.entity.ThrownItemRenderer;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EntityType;

public class PortalCubedEntities {
	public static final EntityType<PortalProjectile> PORTAL_PROJECTILE = REGISTRAR.entities.create("portal_projectile", PortalProjectile::create)
			.configure(Builder::noSave)
			.configure(Builder::noSummon)
			.size(0.5f, 0.5f)
			.renderer(() -> () -> PortalProjectileRenderer::new)
			.build();

	public static final EntityType<Lemonade> LEMONADE = REGISTRAR.entities.create("lemonade", Lemonade::create)
			.configure(b -> b.clientTrackingRange(4).updateInterval(10))
			.size(0.375f, 0.375f)
			.renderer(() -> () -> ThrownItemRenderer::new)
			.build();
	public static final ResourceKey<TerraformBoatType> LEMON_BOAT = TerraformBoatTypeRegistry.createKey(PortalCubed.id("lemon"));

	public static final Map<PropType, EntityType<Prop>> PROPS = Util.make(new EnumMap<>(PropType.class), map -> {
		for (PropType type : PropType.values()) {
			EntityType<Prop> entityType = REGISTRAR.entities.create(type.toString(), type.factory)
					.size(type.dimensions)
					.renderer(() -> () -> PropRenderer::new)
					.build();
			map.put(type, entityType);
		}
	});

	public static void init() {
		Registry.register(TerraformBoatTypeRegistry.INSTANCE, LEMON_BOAT,
				new TerraformBoatType.Builder()
						.item(PortalCubedItems.LEMON_BOAT)
						.chestItem(PortalCubedItems.LEMON_CHEST_BOAT)
						.planks(PortalCubedBlocks.LEMON_PLANKS.asItem())
						.build()
		);
	}
}
