package io.github.fusionflux.portalcubed.content.prop;

import java.util.Locale;
import java.util.function.Consumer;

import io.github.fusionflux.portalcubed.framework.registration.item.ItemBuilder;

import org.apache.commons.lang3.stream.IntStreams;
import org.jetbrains.annotations.Nullable;

import io.github.fusionflux.portalcubed.content.PortalCubedEntities;
import io.github.fusionflux.portalcubed.content.PortalCubedItems;
import io.github.fusionflux.portalcubed.content.prop.entity.ButtonActivatedProp;
import io.github.fusionflux.portalcubed.content.prop.entity.Chair;
import io.github.fusionflux.portalcubed.content.prop.entity.CompanionCube;
import io.github.fusionflux.portalcubed.content.prop.entity.Prop;
import io.github.fusionflux.portalcubed.content.prop.entity.Radio;
import io.github.fusionflux.portalcubed.content.prop.entity.Taco;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntityType.EntityFactory;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;

public enum PropType {
	BEANS                  (3, true, EntityDimensions.fixed(.25f, .375f)),
	CHAIR                  (1, false, EntityDimensions.fixed(.4375f, .59375f), Chair::new),
	CLIPBOARD              (7, true, EntityDimensions.fixed(.5625f, .0625f)),
	COMPANION_CUBE         (4, false, EntityDimensions.fixed(.625f, .6875f), CompanionCube::new),
	COMPUTER               (EntityDimensions.fixed(.5f, .1875f)), //add open variant at some point™️
	COOKING_POT            (EntityDimensions.fixed(.43875f, .25125f)),
	HOOPY                  (EntityDimensions.fixed(1.625f, .0625f)),
	JUG                    (EntityDimensions.fixed(.375f, .5f)),
	LIL_PINEAPPLE          (11, false, EntityDimensions.fixed(.5625f, .5f)),
	MUG                    (8, true, EntityDimensions.fixed(.1875f, .25f)),
	OIL_DRUM               (4, true, EntityDimensions.fixed(.5625f, .9375f)),
	OLD_AP_CUBE            (EntityDimensions.fixed(.625f, .6875f)),
	PORTAL_1_COMPANION_CUBE(2, false, EntityDimensions.fixed(.625f, .6875f)),
	PORTAL_1_STORAGE_CUBE  (1, false, EntityDimensions.fixed(.625f, .6875f)),
	RADIO                  (5, false, EntityDimensions.fixed(.5625f, .3125f), Radio::new),
	// REDIRECTION_CUBE(4, false, EntityDimensions.fixed(.625f, .6875f), P2CubeProp::new
	// SCHRODINGER_CUBE(4, false, EntityDimensions.fixed(.625f, .6875f), P2CubeProp::new
	STORAGE_CUBE           (4, false, EntityDimensions.fixed(.625f, .6875f), ButtonActivatedProp::new),
	THE_TACO			   (2, false, new TacoDimensions(), Taco::new, false, builder -> builder.compostChance(1)),
	ERROR                  (EntityDimensions.fixed(1f, 1f));

	public final int[] variants;
	public final boolean randomVariantOnSpawn;
	public final EntityDimensions dimensions;
	public final EntityFactory<Prop> factory;
	public final boolean facesPlayer;

	private final Consumer<ItemBuilder<PropItem>> itemModifier;

	PropType(EntityDimensions dimensions) {
		this(1, false, dimensions);
	}

	PropType(int variants, boolean randomVariantOnSpawn, EntityDimensions dimensions) {
		this(variants, randomVariantOnSpawn, dimensions, Prop::new);
	}

	PropType(int variants, boolean randomVariantOnSpawn, EntityDimensions dimensions, PropFactory factory) {
		this(variants, randomVariantOnSpawn, dimensions, factory, true);
	}

	PropType(int variants, boolean randomVariantOnSpawn, EntityDimensions dimensions, PropFactory factory, boolean facesPlayer) {
		this(variants, randomVariantOnSpawn, dimensions, factory, facesPlayer, builder -> {});
	}

	PropType(int variants, boolean randomVariantOnSpawn, EntityDimensions dimensions, PropFactory factory,
			 boolean facesPlayer, Consumer<ItemBuilder<PropItem>> itemModifier) {
		this.variants = IntStreams.range(variants).toArray();
		this.randomVariantOnSpawn = randomVariantOnSpawn;
		this.dimensions = dimensions;
		this.factory = (entityType, level) -> factory.create(this, entityType, level);
		this.facesPlayer = facesPlayer;
		this.itemModifier = itemModifier;
	}

	public Item item() {
		return PortalCubedItems.PROPS.get(this);
	}

	public EntityType<Prop> entityType() {
		return PortalCubedEntities.PROPS.get(this);
	}

	public boolean spawn(ServerLevel world, BlockPos pos, MobSpawnType reason, boolean alignPosition, boolean invertY, int variant, boolean randomizeVariant, @Nullable Component customName) {
		return this.entityType().spawn(world, null, prop -> {
			prop.setVariantFromItem(variant);
			prop.setVariant(!(randomizeVariant && randomVariantOnSpawn) ? variant : world.random.nextInt(variants.length - 1) + 1);
			prop.setCustomName(customName);
		}, pos, reason, alignPosition, invertY) != null;
	}

	public void modify(ItemBuilder<PropItem> builder) {
		this.itemModifier.accept(builder);
	}

	@Override
	public String toString() {
		return name().toLowerCase(Locale.ROOT);
	}

	public interface PropFactory {
		Prop create(PropType type, EntityType<Prop> entityType, Level level);
	}
}
