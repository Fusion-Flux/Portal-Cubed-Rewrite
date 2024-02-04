package io.github.fusionflux.portalcubed.content.prop;

import java.util.Locale;

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
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;

public enum PropType {
	BEANS                  (EntityDimensions.fixed(.5f, .5f)),
	CHAIR                  (EntityDimensions.fixed(.5f, .5f)),
	CLIPBOARD              (6, EntityDimensions.fixed(.5f, .5f)),
	COMPANION_CUBE         (2, EntityDimensions.fixed(.5f, .5f)),
	COMPUTER               (EntityDimensions.fixed(.5f, .5f)),
	COOKING_POT            (EntityDimensions.fixed(.5f, .5f)),
	HOOPY                  (EntityDimensions.fixed(.5f, .5f)),
	JUG                    (EntityDimensions.fixed(.5f, .5f)),
	LIL_PINEAPPLE          (10, EntityDimensions.fixed(.5f, .5f)),
	MUG                    (4,  EntityDimensions.fixed(.5f, .5f)),
	OIL_DRUM               (3, EntityDimensions.fixed(.5f, .5f)),
	OLD_AP_CUBE            (EntityDimensions.fixed(.5f, .5f)),
	PORTAL_1_COMPANION_CUBE(EntityDimensions.fixed(.5f, .5f)),
	PORTAL_1_STORAGE_CUBE  (EntityDimensions.fixed(.5f, .5f)),
	RADIO                  (EntityDimensions.fixed(.5f, .5f)),
	// REDIRECTION_CUBE(2,
	// SCHRODINGER_CUBE(2,
	STORAGE_CUBE           (2, EntityDimensions.fixed(.5f, .5f));

	public static final Object2ObjectOpenHashMap<PropType, Item> ITEMS = new Object2ObjectOpenHashMap<>();

	public final int[] variants;
	public final EntityDimensions dimensions;
	public final EntityType<PropEntity> entityType;

	PropType(EntityDimensions dimensions) {
		this(1, dimensions);
	}

	PropType(int variants, EntityDimensions dimensions) {
		this(variants, dimensions, PropEntity::new);
	}

	PropType(int variants, EntityDimensions dimensions, TriFunction<PropType, EntityType<PropEntity>, Level, PropEntity> factory) {
		this.variants = IntStreams.range(variants).toArray();
		this.dimensions = dimensions;
		this.entityType = QuiltEntityTypeBuilder.<PropEntity>create(MobCategory.MISC, (entityType, level) -> factory.apply(this, entityType, level)).setDimensions(dimensions).build();
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

	public boolean spawn(ServerLevel level, BlockPos pos, double yOffset, int variant) {
		var entity = entityType.create(level);
		entity.setVariant(variant);
		entity.setPos(pos.getX() + .5, pos.getY() + yOffset, pos.getZ() + .5);
		return level.addFreshEntity(entity);
	}

	@Override
	public String toString() {
		return name().toLowerCase(Locale.ROOT);
	}
}
