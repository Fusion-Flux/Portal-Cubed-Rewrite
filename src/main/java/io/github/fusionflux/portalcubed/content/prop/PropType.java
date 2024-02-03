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
import net.minecraft.world.phys.Vec3;

public enum PropType {
	BEANS                  (EntityDimensions.fixed(.5f, .5f)),
	CHAIR                  (EntityDimensions.fixed(.5f, .5f)),
	COMPANION_CUBE         (EntityDimensions.fixed(.5f, .5f)),
	COMPUTER               (EntityDimensions.fixed(.5f, .5f)),
	HOOPY                  (EntityDimensions.fixed(.5f, .5f)),
	JUG                    (EntityDimensions.fixed(.5f, .5f)),
	LIL_PINEAPPLE          (EntityDimensions.fixed(.5f, .5f)),
	MUG                    (EntityDimensions.fixed(.5f, .5f)),
	OLD_AP_CUBE            (EntityDimensions.fixed(.5f, .5f)),
	PORTAL_1_COMPANION_CUBE(EntityDimensions.fixed(.5f, .5f)),
	PORTAL_1_STORAGE_CUBE  (EntityDimensions.fixed(.5f, .5f)),
	RADIO                  (EntityDimensions.fixed(.5f, .5f)),
	// REDIRECTION_CUBE,
	// SCHRODINGER_CUBE,
	STORAGE_CUBE           (EntityDimensions.fixed(.5f, .5f));

	public static final Object2ObjectOpenHashMap<PropType, Item> ITEMS = new Object2ObjectOpenHashMap<>();

	public final int[] variants;
	public final EntityType<PropEntity> entityType;

	PropType(EntityDimensions dimensions) {
		this(1, dimensions);
	}

	PropType(int variants, EntityDimensions dimensions) {
		this(variants, dimensions, PropEntity::new);
	}

	PropType(int variants, EntityDimensions dimensions, TriFunction<PropType, EntityType<PropEntity>, Level, PropEntity> factory) {
		this.variants = IntStreams.range(variants).toArray();
		this.entityType = QuiltEntityTypeBuilder.<PropEntity>create(MobCategory.MISC, (entityType, level) -> factory.apply(this, entityType, level)).setDimensions(dimensions).build();
	}

	public static void init() {
		for (var type : values())
			type.register();
	}

	public void register() {
		var id = PortalCubed.id(toString());
		Registry.register(BuiltInRegistries.ENTITY_TYPE, id, this.entityType);
		ITEMS.put(this, Registry.register(BuiltInRegistries.ITEM, id, new Item(new Item.Properties())));

		if (MinecraftQuiltLoader.getEnvironmentType() == EnvType.CLIENT)
			registerClient();
	}

	@ClientOnly
	private void registerClient() {
		EntityRendererRegistry.register(entityType, PropRenderer::new);
	}

	public boolean spawn(ServerLevel level, BlockPos pos) {
		var entity = entityType.create(level);
		entity.setPos(Vec3.atCenterOf(pos));
		return level.addFreshEntity(entity);
	}

	@Override
	public String toString() {
		return name().toLowerCase(Locale.ROOT);
	}
}
