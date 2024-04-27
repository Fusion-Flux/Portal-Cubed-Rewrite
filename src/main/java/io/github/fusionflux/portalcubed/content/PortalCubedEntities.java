package io.github.fusionflux.portalcubed.content;

import com.terraformersmc.terraform.boat.api.TerraformBoatType;
import com.terraformersmc.terraform.boat.api.TerraformBoatTypeRegistry;

import com.terraformersmc.terraform.boat.api.client.TerraformBoatClientHelper;

import io.github.fusionflux.portalcubed.PortalCubed;
import io.github.fusionflux.portalcubed.content.misc.Lemonade;
import io.github.fusionflux.portalcubed.content.portal.projectile.PortalProjectile;
import io.github.fusionflux.portalcubed.content.portal.projectile.PortalProjectileRenderer;
import io.github.fusionflux.portalcubed.content.prop.entity.Prop;
import io.github.fusionflux.portalcubed.content.prop.PropRenderer;
import io.github.fusionflux.portalcubed.content.prop.PropType;
import net.minecraft.Util;
import net.minecraft.client.renderer.entity.ThrownItemRenderer;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;

import org.quiltmc.loader.api.minecraft.ClientOnly;
import org.quiltmc.qsl.entity.extensions.api.QuiltEntityTypeBuilder;

import static io.github.fusionflux.portalcubed.PortalCubed.REGISTRAR;

import java.util.EnumMap;
import java.util.Map;

public class PortalCubedEntities {
	public static final EntityType<PortalProjectile> PORTAL_PROJECTILE = REGISTRAR.entities.create("portal_projectile", PortalProjectile::new)
			.configure(QuiltEntityTypeBuilder::disableSaving)
			.size(EntityDimensions.fixed(0.5f, 0.5f))
			.renderer(() -> () -> PortalProjectileRenderer::new)
			.build();

	public static final EntityType<Lemonade> LEMONADE = REGISTRAR.entities.create("lemonade", Lemonade::create)
			.configure(b -> b.maxChunkTrackingRange(4).trackingTickInterval(10))
			.size(EntityDimensions.fixed(0.375f, 0.375f))
			.renderer(() -> () -> ThrownItemRenderer::new)
			.build();
	public static final ResourceKey<TerraformBoatType> LEMON_BOAT = TerraformBoatTypeRegistry.createKey(PortalCubed.id("lemon"));

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
		Registry.register(TerraformBoatTypeRegistry.INSTANCE, LEMON_BOAT,
				new TerraformBoatType.Builder()
						.item(PortalCubedItems.LEMON_BOAT)
						.chestItem(PortalCubedItems.LEMON_CHEST_BOAT)
						.planks(PortalCubedBlocks.LEMON_PLANKS.asItem())
						.build()
		);
	}

	@ClientOnly
	public static void initClient() {
		TerraformBoatClientHelper.registerModelLayers(LEMON_BOAT.location(), false);
	}
}
