package io.github.fusionflux.portalcubed.content.prop;

import java.util.Locale;
import java.util.function.Consumer;

import org.apache.commons.lang3.stream.IntStreams;
import org.jetbrains.annotations.Nullable;

import io.github.fusionflux.portalcubed.content.PortalCubedDataComponents;
import io.github.fusionflux.portalcubed.content.PortalCubedEntities;
import io.github.fusionflux.portalcubed.content.PortalCubedItems;
import io.github.fusionflux.portalcubed.content.prop.entity.ButtonActivatedProp;
import io.github.fusionflux.portalcubed.content.prop.entity.Chair;
import io.github.fusionflux.portalcubed.content.prop.entity.CompanionCube;
import io.github.fusionflux.portalcubed.content.prop.entity.Prop;
import io.github.fusionflux.portalcubed.content.prop.entity.Radio;
import io.github.fusionflux.portalcubed.content.prop.entity.Taco;
import io.github.fusionflux.portalcubed.framework.registration.item.ItemBuilder;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntityType.EntityFactory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.level.Level;

public enum PropType {
	BEANS                  (3, true, .25f, .375f),
	CHAIR                  (1, false, .4375f, .59375f, Chair::new),
	CLIPBOARD              (7, true, .5625f, .0625f),
	COMPANION_CUBE         (4, false, .625f, .6875f, CompanionCube::new),
	COMPUTER               (.5f, .1875f), //add open variant at some point™️
	COOKING_POT            (.43875f, .25125f),
	HOOPY                  (1.625f, .0625f),
	JUG                    (.375f, .5f),
	LIL_PINEAPPLE          (11, false, .5625f, .5f),
	MUG                    (8, true, .1875f, .25f),
	OIL_DRUM               (4, true, .5625f, .9375f),
	OLD_AP_CUBE            (.625f, .6875f),
	PORTAL_1_COMPANION_CUBE(2, false, .625f, .6875f),
	PORTAL_1_STORAGE_CUBE  (1, false, .625f, .6875f),
	RADIO                  (5, false, .5625f, .3125f, Radio::new),
	// REDIRECTION_CUBE(4, false, .625f, .6875f), P2CubeProp::new
	// SCHRODINGER_CUBE(4, false, .625f, .6875f), P2CubeProp::new
	STORAGE_CUBE           (4, false, .625f, .6875f, ButtonActivatedProp::new),
	THE_TACO               (2, false, .69375f, .38125f, Taco::new, false, builder -> builder
			.compostChance(1)
			.properties(p -> p.rarity(Rarity.EPIC))
	),
	ERROR                  (1, false, 1f, 1f, Prop::new, false, builder -> builder.properties(p -> p.rarity(Rarity.EPIC)));

	public final String name;
	public final int[] variants;
	public final boolean randomVariantOnSpawn;
	public final float width;
	public final float height;
	public final EntityFactory<Prop> factory;
	public final boolean facesPlayer;

	private final Consumer<ItemBuilder<PropItem>> itemModifier;

	PropType(float width, float height) {
		this(1, false, width, height);
	}

	PropType(int variants, boolean randomVariantOnSpawn, float width, float height) {
		this(variants, randomVariantOnSpawn, width, height, Prop::new);
	}

	PropType(int variants, boolean randomVariantOnSpawn, float width, float height, PropFactory factory) {
		this(variants, randomVariantOnSpawn, width, height, factory, true);
	}

	PropType(int variants, boolean randomVariantOnSpawn, float width, float height, PropFactory factory, boolean facesPlayer) {
		this(variants, randomVariantOnSpawn, width, height, factory, facesPlayer, builder -> {});
	}

	PropType(int variants, boolean randomVariantOnSpawn, float width, float height, PropFactory factory,
			 boolean facesPlayer, Consumer<ItemBuilder<PropItem>> itemModifier) {
		this.name = this.name().toLowerCase(Locale.ROOT);
		this.variants = IntStreams.range(variants).toArray();
		this.randomVariantOnSpawn = randomVariantOnSpawn;
		this.width = width;
		this.height = height;
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

	@Nullable
	public Prop spawn(ServerLevel level, BlockPos pos, @Nullable ItemStack stack, @Nullable Player player,
					  EntitySpawnReason reason, @Nullable Integer variant, boolean offsetY, boolean offsetYMore) {
		Consumer<Prop> consumer = prop -> {
			prop.setVariantFromItem(variant != null ? variant : 0);
			boolean randomize = variant == null;
			if (randomize && this.randomVariantOnSpawn) {
				int randomVariant = Util.getRandom(this.variants, prop.getRandom());
				prop.setVariant(randomVariant);
			} else {
				prop.setVariant(variant != null ? variant : 0);
			}

			if (stack != null) {
				Holder<SoundEvent> track = stack.get(PortalCubedDataComponents.RADIO_TRACK);
				if (prop instanceof Radio radio && track != null) {
					radio.playTrack(track);
				}
			}
		};

		return this.entityType().spawn(
				level,
				stack != null ? EntityType.appendDefaultStackConfig(consumer, level, stack, player) : consumer,
				pos,
				reason,
				offsetY,
				offsetYMore
		);
	}

	public void modify(ItemBuilder<PropItem> builder) {
		this.itemModifier.accept(builder);
	}

	@FunctionalInterface
	public interface PropFactory {
		Prop create(PropType type, EntityType<Prop> entityType, Level level);
	}
}
