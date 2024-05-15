package io.github.fusionflux.portalcubed.content;

import static io.github.fusionflux.portalcubed.PortalCubed.REGISTRAR;

import java.util.EnumMap;
import java.util.Map;

import com.terraformersmc.terraform.boat.api.item.TerraformBoatItemHelper;

import io.github.fusionflux.portalcubed.PortalCubed;
import io.github.fusionflux.portalcubed.content.misc.LemonadeDispenseBehavior;
import io.github.fusionflux.portalcubed.content.misc.LemonadeItem;
import io.github.fusionflux.portalcubed.content.portal.gun.PortalGunColorProvider;
import io.github.fusionflux.portalcubed.content.cannon.ConstructionCannonItem;
import io.github.fusionflux.portalcubed.content.portal.gun.PortalGunItem;
import net.fabricmc.fabric.api.loot.v2.LootTableEvents;
import net.minecraft.world.food.Foods;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.Item;
import io.github.fusionflux.portalcubed.content.prop.HammerItem;
import io.github.fusionflux.portalcubed.content.prop.PropDispenseBehavior;
import io.github.fusionflux.portalcubed.content.prop.PropItem;
import io.github.fusionflux.portalcubed.content.prop.PropType;

import net.minecraft.Util;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.SignItem;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraft.world.level.storage.loot.entries.LootItem;

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

	public static final ConstructionCannonItem CONSTRUCTION_CANNON = REGISTRAR.items.create("construction_cannon", ConstructionCannonItem::new)
			.settings(s -> s.stacksTo(1).fireResistant())
			.build();

	public static final Item LEMON = REGISTRAR.items.create("lemon", Item::new)
			.settings(s -> s.food(Foods.APPLE))
			.build();
	public static final LemonadeItem LEMONADE = REGISTRAR.items.create("lemonade", LemonadeItem::new)
			.settings(s -> s.stacksTo(1))
			.build();
	public static final SignItem LEMON_SIGN = REGISTRAR.items.create("lemon_sign", s -> new SignItem(s, PortalCubedBlocks.LEMON_SIGN, PortalCubedBlocks.LEMON_WALL_SIGN))
			.settings(s -> s.stacksTo(16))
			.build();
	public static final SignItem LEMON_HANGING_SIGN = REGISTRAR.items.create("lemon_hanging_sign", s -> new SignItem(s, PortalCubedBlocks.LEMON_HANGING_SIGN, PortalCubedBlocks.LEMON_WALL_HANGING_SIGN))
			.settings(s -> s.stacksTo(16))
			.build();
	public static final Item LEMON_BOAT = TerraformBoatItemHelper.registerBoatItem(PortalCubed.id("lemon_boat"), PortalCubedEntities.LEMON_BOAT, false);
	public static final Item LEMON_CHEST_BOAT = TerraformBoatItemHelper.registerBoatItem(PortalCubed.id("lemon_chest_boat"), PortalCubedEntities.LEMON_BOAT, true);

	public static final Item GOO_BUCKET = REGISTRAR.items.create("toxic_goo_bucket", s -> new BucketItem(PortalCubedFluids.GOO, s))
			.settings(s -> s.craftRemainder(Items.BUCKET).stacksTo(1))
			.build();

	public static final Map<PropType, PropItem> PROPS = Util.make(new EnumMap<>(PropType.class), map -> {
		for (PropType type : PropType.values()) {
			PropItem item = REGISTRAR.items.simple(type.toString(), s -> new PropItem(s, type));
			map.put(type, item);

			DispenserBlock.registerBehavior(item, new PropDispenseBehavior(item));
		}
	});

	public static void init() {
		DispenserBlock.registerBehavior(LEMONADE, new LemonadeDispenseBehavior());
		LootTableEvents.MODIFY.register((resourceManager, lootManager, id, tableBuilder, source) -> {
			if (BuiltInLootTables.SNIFFER_DIGGING.equals(id) && source.isBuiltin())
				tableBuilder.modifyPools(builder -> builder.add(LootItem.lootTableItem(PortalCubedBlocks.LEMON_SAPLING)));
		});
	}
}
