package io.github.fusionflux.portalcubed.content.prop;

import java.util.Locale;
import java.util.Optional;

import io.github.fusionflux.portalcubed.content.PortalCubedEntities;

import io.github.fusionflux.portalcubed.content.prop.entity.ButtonActivatedProp;
import io.github.fusionflux.portalcubed.content.prop.entity.Chair;
import io.github.fusionflux.portalcubed.content.prop.entity.CompanionCube;
import io.github.fusionflux.portalcubed.content.prop.entity.P1CompanionCube;
import io.github.fusionflux.portalcubed.content.prop.entity.P1Prop;
import io.github.fusionflux.portalcubed.content.prop.entity.Prop;

import io.github.fusionflux.portalcubed.content.prop.entity.Radio;

import org.apache.commons.lang3.stream.IntStreams;

import io.github.fusionflux.portalcubed.PortalCubed;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntityType.EntityFactory;
import net.minecraft.world.level.Level;

public enum PropType {
	BEANS                  (EntityDimensions.fixed(.25f, .375f), SoundType.METAL),
	CHAIR                  (1, false, EntityDimensions.fixed(.4375f, .46875f), Chair::new, false, SoundType.GENERIC),
	CLIPBOARD              (7, true, EntityDimensions.fixed(.5625f, .0625f), SoundType.GENERIC),
	COMPANION_CUBE         (4, false, EntityDimensions.fixed(.625f, .625f), CompanionCube::new, true, SoundType.CUBE),
	COMPUTER               (EntityDimensions.fixed(.5f, .1875f), SoundType.METAL),
	COOKING_POT            (EntityDimensions.fixed(.43875f, .25125f), SoundType.METAL),
	HOOPY                  (EntityDimensions.fixed(1.625f, .0625f), SoundType.METAL),
	JUG                    (EntityDimensions.fixed(.375f, .5f), SoundType.GENERIC),
	LIL_PINEAPPLE          (10, false, EntityDimensions.fixed(.5625f, .5f), SoundType.GENERIC),
	MUG                    (8, true, EntityDimensions.fixed(.1875f, .25f), SoundType.GENERIC),
	OIL_DRUM               (4, true, EntityDimensions.fixed(.5625f, .9375f), SoundType.METAL),
	OLD_AP_CUBE            (EntityDimensions.fixed(.625f, .625f), SoundType.OLD_AP_CUBE),
	PORTAL_1_COMPANION_CUBE(2, false, EntityDimensions.fixed(.625f, .625f), P1CompanionCube::new, false, SoundType.PORTAL_1_CUBE),
	PORTAL_1_STORAGE_CUBE  (1, false, EntityDimensions.fixed(.625f, .625f), P1Prop::new, false, SoundType.PORTAL_1_CUBE),
	RADIO                  (5, false, EntityDimensions.fixed(.5625f, .3125f), Radio::new, true, SoundType.METAL),
	// REDIRECTION_CUBE(4, false, EntityDimensions.fixed(.625f, .625f), P2CubeProp::new
	// SCHRODINGER_CUBE(4, false, EntityDimensions.fixed(.625f, .625f), P2CubeProp::new
	STORAGE_CUBE           (4, false, EntityDimensions.fixed(.625f, .625f), ButtonActivatedProp::new, true, SoundType.CUBE),
	THE_TACO(new TacoDimensions(), SoundType.PORTAL_1_CUBE);

	public final int[] variants;
	public final boolean randomVariantOnSpawn;
	public final EntityDimensions dimensions;
	public final EntityFactory<Prop> factory;
	public final boolean hasDirtyVariant;
	public final SoundType soundType;

	PropType(EntityDimensions dimensions, SoundType soundType) {
		this(1, false, dimensions, soundType);
	}

	PropType(int variants, boolean randomVariantOnSpawn, EntityDimensions dimensions, SoundType soundType) {
		this(variants, randomVariantOnSpawn, dimensions, Prop::new, false, soundType);
	}

	PropType(int variants, boolean randomVariantOnSpawn, EntityDimensions dimensions, PropFactory factory, boolean hasDirtyVariant, SoundType soundType) {
		this.variants = IntStreams.range(variants).toArray();
		this.randomVariantOnSpawn = randomVariantOnSpawn;
		this.dimensions = dimensions;
		this.factory = (entityType, level) -> factory.create(this, entityType, level);
		this.hasDirtyVariant = hasDirtyVariant;
		this.soundType = soundType;
	}

	public boolean spawn(ServerLevel level, BlockPos pos, double yOffset, int variant, boolean randomizeVariant, Optional<Component> customName) {
		EntityType<Prop> entityType = PortalCubedEntities.PROPS.get(this);
		Prop entity = entityType.create(level);
		if (entity == null)
			return false;

		entity.setVariantFromItem(variant);
		if (randomizeVariant && randomVariantOnSpawn)
			variant = level.random.nextInt(variants.length - 1) + 1;
		entity.setVariant(variant);
		customName.ifPresent(entity::setCustomName);
		entity.setPos(pos.getX() + .5, pos.getY() + yOffset, pos.getZ() + .5);
		return level.addFreshEntity(entity);
	}

	@Override
	public String toString() {
		return name().toLowerCase(Locale.ROOT);
	}

	public enum SoundType {
		GENERIC,
		METAL,
		CUBE,
		OLD_AP_CUBE,
		PORTAL_1_CUBE;

		public final SoundEvent impactSound;

		SoundType() {
			var id = PortalCubed.id(this + "_impact");
			this.impactSound = Registry.register(BuiltInRegistries.SOUND_EVENT, id, SoundEvent.createVariableRangeEvent(id));
		}

		@Override
		public String toString() {
			return name().toLowerCase(Locale.ROOT);
		}
	}

	public interface PropFactory {
		Prop create(PropType type, EntityType<Prop> entityType, Level level);
	}
}
