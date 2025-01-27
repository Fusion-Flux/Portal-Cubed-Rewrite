package io.github.fusionflux.portalcubed.framework.registration.entity;

import java.util.function.Consumer;
import java.util.function.Supplier;

import io.github.fusionflux.portalcubed.framework.registration.Registrar;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.NoopRenderer;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;

public class EntityBuilderImpl<T extends Entity> implements EntityBuilder<T> {
	private final Registrar registrar;
	private final String name;
	private final EntityType.Builder<T> typeBuilder;

	private Supplier<Supplier<EntityRendererProvider<T>>> rendererSupplier;

	public EntityBuilderImpl(Registrar registrar, String name, EntityType.EntityFactory<T> factory) {
		this.registrar = registrar;
		this.name = name;
		this.typeBuilder = EntityType.Builder.of(factory, MobCategory.MISC);
	}

	@Override
	public EntityBuilder<T> configure(Consumer<EntityType.Builder<T>> consumer) {
		consumer.accept(this.typeBuilder);
		return this;
	}

	@Override
	public EntityBuilder<T> size(float width, float height) {
		this.typeBuilder.sized(width, height);
		return this;
	}

	@Override
	public EntityBuilder<T> renderer(Supplier<Supplier<EntityRendererProvider<T>>> supplier) {
		this.rendererSupplier = supplier;
		return this;
	}

	@Override
	public EntityType<T> build() {
		ResourceLocation id = this.registrar.id(this.name);
		ResourceKey<EntityType<?>> key = ResourceKey.create(Registries.ENTITY_TYPE, id);
		EntityType<T> type = this.typeBuilder.build(key);

		Registry.register(BuiltInRegistries.ENTITY_TYPE, id, type);

		if (FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT) {
			this.buildClient(type);
		}

		return type;
	}

	@Environment(EnvType.CLIENT)
	private void buildClient(EntityType<T> type) {
		EntityRendererProvider<T> provider = this.rendererSupplier == null
				? NoopRenderer::new
				: this.rendererSupplier.get().get();

		EntityRendererRegistry.register(type, provider);
	}
}
