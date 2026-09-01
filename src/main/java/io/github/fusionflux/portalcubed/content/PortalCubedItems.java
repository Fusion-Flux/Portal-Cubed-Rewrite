package io.github.fusionflux.portalcubed.content;

import static io.github.fusionflux.portalcubed.PortalCubed.REGISTRAR;

import java.util.EnumMap;
import java.util.Map;

import io.github.fusionflux.portalcubed.PortalCubed;
import io.github.fusionflux.portalcubed.content.cannon.ConstructionCannonItem;
import io.github.fusionflux.portalcubed.content.fizzler.tool.FizzleinatorItem;
import io.github.fusionflux.portalcubed.content.lemon.LemonadeDispenseBehavior;
import io.github.fusionflux.portalcubed.content.lemon.LemonadeItem;
import io.github.fusionflux.portalcubed.content.misc.CrowbarItem;
import io.github.fusionflux.portalcubed.content.portal.gun.PortalGunCauldronInteraction;
import io.github.fusionflux.portalcubed.content.portal.gun.PortalGunItem;
import io.github.fusionflux.portalcubed.content.portal.gun.PortalGunSettings;
import io.github.fusionflux.portalcubed.content.prop.HammerItem;
import io.github.fusionflux.portalcubed.content.prop.PropDispenseBehavior;
import io.github.fusionflux.portalcubed.content.prop.PropItem;
import io.github.fusionflux.portalcubed.content.prop.PropType;
import io.github.fusionflux.portalcubed.data.tags.PortalCubedBannerPatternTags;
import io.github.fusionflux.portalcubed.framework.item.BucketDispenseBehaviour;
import io.github.fusionflux.portalcubed.framework.item.FallSound;
import io.github.fusionflux.portalcubed.framework.registration.item.ItemBuilder;
import io.github.fusionflux.portalcubed.mixin.goo.CauldronInteraction$DispatcherAccessor;
import net.fabricmc.fabric.api.loot.v3.LootTableEvents;
import net.minecraft.core.Direction;
import net.minecraft.core.cauldron.CauldronInteractions;
import net.minecraft.core.component.DataComponents;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Util;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.food.Foods;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.HangingSignItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.StandingAndWallBlockItem;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.equipment.ArmorType;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.providers.number.NumberProviders;

public class PortalCubedItems {
	public static final PortalGunItem PORTAL_GUN = REGISTRAR.items.create("portal_gun", PortalGunItem::new)
			.properties(s -> s
					.stacksTo(1)
					.fireResistant()
					.component(PortalCubedDataComponents.PORTAL_GUN_SETTINGS, PortalGunSettings.DEFAULT)
					.component(DataComponents.TOOLTIP_STYLE, PortalCubed.id("aperture"))
					.rarity(Rarity.RARE)
			)
			.build();

	public static final Item MAGNESIUM_INGOT = REGISTRAR.items.create("magnesium_ingot", Item::new)
			.properties(s -> s.trimMaterial(PortalCubedMisc.MAGNESIUM_TRIM_MATERIAL))
			.build();

	public static final Item MAGNESIUM_NUGGET = REGISTRAR.items.create("magnesium_nugget", Item::new)
			.build();

	public static final Item RAW_MAGNESIUM = REGISTRAR.items.create("raw_magnesium", Item::new)
			.build();

	public static final Item APERTURE_BANNER_PATTERN = REGISTRAR.items.create("aperture_banner_pattern")
			.properties(s -> s
					.stacksTo(1)
					.rarity(Rarity.UNCOMMON)
					.delayedComponent(DataComponents.PROVIDES_BANNER_PATTERNS, context -> context.getOrThrow(PortalCubedBannerPatternTags.APERTURE))
			)
			.build();

	public static final CrowbarItem CROWBAR = REGISTRAR.items.create("crowbar", CrowbarItem::new)
			.properties(s -> s.stacksTo(1))
			.build();

	public static final HammerItem HAMMER = REGISTRAR.items.create("hammer", HammerItem::new)
			.properties(s -> s.stacksTo(1))
			.build();

	public static final Item SCREEN_ASSEMBLY = REGISTRAR.items.create("screen_assembly", Item::new)
			.properties(Item.Properties::fireResistant)
			.build();
	public static final Item KINETIC_ASSEMBLY = REGISTRAR.items.create("kinetic_assembly", Item::new)
			.properties(Item.Properties::fireResistant)
			.build();
	public static final Item EMITTER_ASSEMBLY = REGISTRAR.items.create("emitter_assembly", Item::new)
			.properties(Item.Properties::fireResistant)
			.build();

	public static final ConstructionCannonItem CONSTRUCTION_CANNON = REGISTRAR.items.create("construction_cannon", ConstructionCannonItem::new)
			.properties(s -> s
					.stacksTo(1)
					.fireResistant()
					.component(
						DataComponents.ATTRIBUTE_MODIFIERS,
						ItemAttributeModifiers.builder().add(
								Attributes.BLOCK_INTERACTION_RANGE,
								new AttributeModifier(ConstructionCannonItem.REACH_BOOST, 2.5, Operation.ADD_VALUE),
								EquipmentSlotGroup.MAINHAND
						).build()
					)
					.component(DataComponents.TOOLTIP_STYLE, PortalCubed.id("aperture"))
			)
			.build();

	public static final FizzleinatorItem FIZZLEINATOR = REGISTRAR.items.create("fizzleinator", FizzleinatorItem::new)
			.properties(s -> s
					.stacksTo(1)
					.rarity(Rarity.EPIC)
					.component(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true)
			)
			.build();

	public static final Item LEMON = REGISTRAR.items.create("lemon", Item::new)
			.properties(s -> s.food(Foods.APPLE).compostable(NumberProviders.COMPOSTABLE_MEDIUM))
			.build();
	public static final LemonadeItem LEMONADE = REGISTRAR.items.create("lemonade", LemonadeItem::new)
			.properties(s -> s.stacksTo(1))
			.build();
	public static final Item LEMON_SIGN = REGISTRAR.items.create("lemon_sign", s -> new StandingAndWallBlockItem(PortalCubedBlocks.LEMON_SIGN, PortalCubedBlocks.LEMON_WALL_SIGN, Direction.DOWN, s))
			.properties(s -> s
					.useBlockDescriptionPrefix()
					.cookingFuel(NumberProviders.COOKING_TIME_WOOD_ITEMS_LARGE)
					.stacksTo(16)
					.signText()
			)
			.build();
	public static final Item LEMON_HANGING_SIGN = REGISTRAR.items.create("lemon_hanging_sign", s -> new HangingSignItem(PortalCubedBlocks.LEMON_HANGING_SIGN, PortalCubedBlocks.LEMON_WALL_HANGING_SIGN, s))
			.properties(s -> s
					.useBlockDescriptionPrefix()
					.cookingFuel(NumberProviders.COOKING_TIME_HANGING_SIGNS)
					.stacksTo(16)
					.signText()
			)
			.build();
	// TODO: Lemon Boats - Max
//	public static final Item LEMON_BOAT = TerraformBoatItemHelper.registerBoatItem(PortalCubed.id("lemon"), false, false);
//	public static final Item LEMON_CHEST_BOAT = TerraformBoatItemHelper.registerBoatItem(PortalCubed.id("lemon"), true, false);

	public static final Item GOO_BUCKET = REGISTRAR.items.create("toxic_goo_bucket", s -> new BucketItem(PortalCubedFluids.GOO, s))
			.properties(s -> s.craftRemainder(Items.BUCKET).stacksTo(1))
			.build();

	public static final Item LONG_FALL_BOOTS = REGISTRAR.items.create("long_fall_boots")
			.properties(s -> s
					.fireResistant()
					.humanoidArmor(PortalCubedArmorMaterials.LONG_FALL_BOOTS, ArmorType.BOOTS)
					.component(PortalCubedDataComponents.FALL_SOUND, new FallSound(PortalCubedSounds.LONG_FALL_BOOTS_LAND, 6))
					.component(DataComponents.TOOLTIP_STYLE, PortalCubed.id("aperture"))
			)
			.build();
	public static final Item ADVANCED_KNEE_REPLACEMENTS = REGISTRAR.items.create("advanced_knee_replacements")
			.properties(s -> s
					.fireResistant()
					.humanoidArmor(PortalCubedArmorMaterials.ADVANCED_KNEE_REPLACEMENTS, ArmorType.BOOTS)
					.component(PortalCubedDataComponents.FALL_SOUND, new FallSound(PortalCubedSounds.ADVANCED_KNEE_REPLACEMENTS_LAND, 5))
					.component(DataComponents.TOOLTIP_STYLE, PortalCubed.id("aperture"))
			)
			.build();

	public static final Map<PropType, PropItem> PROPS = Util.make(new EnumMap<>(PropType.class), map -> {
		for (PropType type : PropType.values()) {
			ItemBuilder<PropItem> builder = REGISTRAR.items.create(type.name, s -> new PropItem(s, type));
			type.modify(builder);
			PropItem item = builder.build();
			map.put(type, item);

			DispenserBlock.registerBehavior(item, new PropDispenseBehavior(item));
		}
	});

	public static void init() {
		DispenserBlock.registerBehavior(GOO_BUCKET, new BucketDispenseBehaviour());
		DispenserBlock.registerBehavior(LEMONADE, LemonadeDispenseBehavior.INSTANCE);

		CauldronInteraction$DispatcherAccessor emptyDispatcher = (CauldronInteraction$DispatcherAccessor) CauldronInteractions.EMPTY;
		emptyDispatcher.callPut(GOO_BUCKET, (_, level, pos, player, hand, stack) -> CauldronInteractions.emptyBucket(
				level, pos, player, hand, stack, PortalCubedBlocks.GOO_CAULDRON.defaultBlockState(), SoundEvents.BUCKET_EMPTY
		));

		CauldronInteraction$DispatcherAccessor waterDispatcher = (CauldronInteraction$DispatcherAccessor) CauldronInteractions.WATER;
		waterDispatcher.callPut(PORTAL_GUN, PortalGunCauldronInteraction.INSTANCE);

		LootTableEvents.MODIFY.register((key, builder, source, _) -> {
			if (key == BuiltInLootTables.SNIFFER_DIGGING && source.isBuiltin()) {
				builder.modifyPools(pool -> pool.add(LootItem.lootTableItem(PortalCubedBlocks.LEMON_SAPLING)));
			}
		});
	}
}
