package io.github.fusionflux.portalcubed.content.prop;

import java.util.Locale;
import java.util.Optional;

import org.apache.commons.lang3.function.TriFunction;
import org.apache.commons.lang3.stream.IntStreams;
import org.quiltmc.loader.api.minecraft.ClientOnly;
import org.quiltmc.loader.api.minecraft.MinecraftQuiltLoader;
import org.quiltmc.qsl.entity.extensions.api.QuiltEntityTypeBuilder;

import io.github.fusionflux.portalcubed.PortalCubed;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.fabricmc.api.EnvType;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;

public enum PropType {
	BEANS                  (EntityDimensions.fixed(.25f, .375f)),
	CHAIR                  (EntityDimensions.fixed(.4375f, .46875f)),
	CLIPBOARD              (7, true, EntityDimensions.fixed(.625f, .625f)),
	COMPANION_CUBE         (4, false, EntityDimensions.fixed(.625f, .625f), P2CubeProp::new),
	COMPUTER               (EntityDimensions.fixed(.5f, .1875f)),
	COOKING_POT            (EntityDimensions.fixed(.43875f, .25125f)),
	HOOPY                  (EntityDimensions.fixed(1.625f, .0625f)),
	JUG                    (EntityDimensions.fixed(.375f, .5f)),
	LIL_PINEAPPLE          (10, false, EntityDimensions.fixed(.5625f, .5f)),
	MUG                    (8, true, EntityDimensions.fixed(.1875f, .25f)),
	OIL_DRUM               (4, true, EntityDimensions.fixed(.5625f, .9375f)),
	OLD_AP_CUBE            (EntityDimensions.fixed(.625f, .625f)),
	PORTAL_1_COMPANION_CUBE(2, false, EntityDimensions.fixed(.625f, .625f)),
	PORTAL_1_STORAGE_CUBE  (EntityDimensions.fixed(.625f, .625f)),
	RADIO                  (4, false, EntityDimensions.fixed(.625f, .3125f)),
	// REDIRECTION_CUBE(4, false, EntityDimensions.fixed(.625f, .625f), P2CubeProp::new
	// SCHRODINGER_CUBE(4, false, EntityDimensions.fixed(.625f, .625f), P2CubeProp::new
	STORAGE_CUBE           (4, false, EntityDimensions.fixed(.625f, .625f), P2CubeProp::new);

	public static final Object2ObjectOpenHashMap<PropType, Item> ITEMS = new Object2ObjectOpenHashMap<>();

	public final int[] variants;
	public final boolean randomVariantOnPlace;
	public final EntityDimensions dimensions;
	public final EntityType<Prop> entityType;

	PropType(EntityDimensions dimensions) {
		this(1, false, dimensions);
	}

	PropType(int variants, boolean randomVariantOnPlace, EntityDimensions dimensions) {
		this(variants, randomVariantOnPlace, dimensions, Prop::new);
	}

	PropType(int variants, boolean randomVariantOnPlace, EntityDimensions dimensions, TriFunction<PropType, EntityType<Prop>, Level, Prop> factory) {
		this.variants = IntStreams.range(variants).toArray();
		this.randomVariantOnPlace = randomVariantOnPlace;
		this.dimensions = dimensions;
		this.entityType = QuiltEntityTypeBuilder.<Prop>create(MobCategory.MISC, (entityType, level) -> factory.apply(this, entityType, level)).setDimensions(dimensions).build();
	}

	public static void init() {
		for (var type : values())
			type.register();
	}

	public void register() {
		var id = PortalCubed.id(toString());
		Registry.register(BuiltInRegistries.ENTITY_TYPE, id, this.entityType);
		ITEMS.put(this, Registry.register(BuiltInRegistries.ITEM, id, new PropItem(new Item.Properties(), this)));

		if (MinecraftQuiltLoader.getEnvironmentType() == EnvType.CLIENT)
			registerClient();
	}

	@ClientOnly
	private void registerClient() {
		EntityRendererRegistry.register(entityType, PropRenderer::new);
	}

	public boolean spawn(ServerLevel level, BlockPos pos, double yOffset, int variant, Optional<Component> customName) {
		var entity = entityType.create(level);
		entity.setVariant(variant);
		customName.ifPresent(name -> entity.setCustomName(name));
		entity.setPos(pos.getX() + .5, pos.getY() + yOffset, pos.getZ() + .5);
		return level.addFreshEntity(entity);
	}

	@Override
	public String toString() {
		return name().toLowerCase(Locale.ROOT);
	}
}
