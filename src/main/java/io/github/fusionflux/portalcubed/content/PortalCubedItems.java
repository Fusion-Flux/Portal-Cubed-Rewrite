package io.github.fusionflux.portalcubed.content;

import static io.github.fusionflux.portalcubed.PortalCubed.REGISTRAR;

import java.util.EnumMap;
import java.util.Map;

import io.github.fusionflux.portalcubed.content.portal.gun.PortalGunColorProvider;
import io.github.fusionflux.portalcubed.content.portal.gun.PortalGunItem;
import net.minecraft.world.item.Item;
import io.github.fusionflux.portalcubed.content.prop.HammerItem;
import io.github.fusionflux.portalcubed.content.prop.PropDispenseBehavior;
import io.github.fusionflux.portalcubed.content.prop.PropItem;
import io.github.fusionflux.portalcubed.content.prop.PropType;

import net.minecraft.Util;
import net.minecraft.world.level.block.DispenserBlock;

public class PortalCubedItems {
	public static final PortalGunItem PORTAL_GUN = REGISTRAR.items.create("portal_gun", PortalGunItem::new)
			.settings(s -> s.stacksTo(1).fireResistant())
			.colored(() -> () -> PortalGunColorProvider.INSTANCE)
			.build();

	public static final Item MAGNESIUM_INGOT = REGISTRAR.items.create("magnesium_ingot", Item::new)
			.build();

	public static final Item MAGNESIUM_NUGGET = REGISTRAR.items.create("magnesium_nugget", Item::new)
			.build();

	public static final Item RAW_MAGNESIUM = REGISTRAR.items.create("raw_magnesium", Item::new)
			.build();

	public static final HammerItem HAMMER = REGISTRAR.items.create("hammer", HammerItem::new)
			.settings(s -> s.stacksTo(1))
			.build();

	public static final Map<PropType, PropItem> PROPS = Util.make(new EnumMap<>(PropType.class), map -> {
		for (PropType type : PropType.values()) {
			PropItem item = REGISTRAR.items.simple(type.toString(), s -> new PropItem(s, type));
			map.put(type, item);

			DispenserBlock.registerBehavior(item, new PropDispenseBehavior(item));
		}
	});

	public static void init() {
	}
}
