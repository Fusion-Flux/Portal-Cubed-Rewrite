package io.github.fusionflux.portalcubed.content;

import static io.github.fusionflux.portalcubed.PortalCubed.REGISTRAR;

import java.util.EnumMap;
import java.util.Map;
import java.util.function.Supplier;

import io.github.fusionflux.portalcubed.content.lemon.Lemonade;
import io.github.fusionflux.portalcubed.content.prop.PropType;
import io.github.fusionflux.portalcubed.content.prop.entity.Prop;
import io.github.fusionflux.portalcubed.content.prop.renderer.PropRenderer;
import net.minecraft.client.renderer.entity.ThrownItemRenderer;
import net.minecraft.util.Util;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntityType.EntityFactory;
import net.minecraft.world.entity.vehicle.boat.AbstractBoat;
import net.minecraft.world.entity.vehicle.boat.Boat;
import net.minecraft.world.entity.vehicle.boat.ChestBoat;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;

public class PortalCubedEntities {
	public static final EntityType<Lemonade> LEMONADE = REGISTRAR.entities.create("lemonade", Lemonade::create)
			.configure(b -> b.clientTrackingRange(4))
			.size(0.375f, 0.375f)
			.renderer(() -> () -> ThrownItemRenderer::new)
			.build();

	public static final EntityType<Boat> LEMON_BOAT = boat("lemon_boat", Boat::new, () -> PortalCubedItems.LEMON_BOAT);
	public static final EntityType<ChestBoat> LEMON_CHEST_BOAT = boat("lemon_chest_boat", ChestBoat::new, () -> PortalCubedItems.LEMON_CHEST_BOAT);

	public static final Map<PropType, EntityType<Prop>> PROPS = Util.make(new EnumMap<>(PropType.class), map -> {
		for (PropType type : PropType.values()) {
			EntityType<Prop> entityType = REGISTRAR.entities.create(type.name, type.factory)
					.configure(b -> b.updateInterval(1))
					.size(type.width, type.height)
					.renderer(() -> () -> PropRenderer::new)
					.build();
			map.put(type, entityType);
		}
	});

	public static void init() {
	}

	private static <T extends AbstractBoat> EntityType<T> boat(String name, BoatFactory<T> factory, Supplier<Item> item) {
		EntityFactory<T> entityFactory = (type, level) -> factory.create(type, level, item);
		return REGISTRAR.entities.create(name, entityFactory).configure(
				builder -> builder.noLootTable()
						.sized(1.375F, 0.5625F)
						.eyeHeight(0.5625F)
						.clientTrackingRange(10)
		).build();
	}

	@FunctionalInterface
	private interface BoatFactory<T extends AbstractBoat> {
		T create(EntityType<T> type, Level level, Supplier<Item> item);
	}
}
