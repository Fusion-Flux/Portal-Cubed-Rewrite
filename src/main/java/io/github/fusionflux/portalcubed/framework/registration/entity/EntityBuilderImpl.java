package io.github.fusionflux.portalcubed.framework.registration.entity;

import io.github.fusionflux.portalcubed.framework.registration.Registrar;
import net.fabricmc.api.EnvType;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.NoopRenderer;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;

import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;

import net.minecraft.world.entity.MobCategory;

import org.quiltmc.loader.api.minecraft.ClientOnly;
import org.quiltmc.loader.api.minecraft.MinecraftQuiltLoader;
import org.quiltmc.qsl.entity.extensions.api.QuiltEntityTypeBuilder;

import java.util.function.Consumer;
import java.util.function.Supplier;

public class EntityBuilderImpl<T extends Entity> implements EntityBuilder<T> {
	private final Registrar registrar;
	private final String name;
	private final QuiltEntityTypeBuilder<T> typeBuilder;

	private Supplier<Supplier<EntityRendererProvider<T>>> rendererSupplier;

	public EntityBuilderImpl(Registrar registrar, String name, EntityType.EntityFactory<T> factory) {
		this.registrar = registrar;
		this.name = name;
		this.typeBuilder = QuiltEntityTypeBuilder.create(MobCategory.MISC, factory);
	}

	@Override
	public EntityBuilder<T> configure(Consumer<QuiltEntityTypeBuilder<T>> consumer) {
		consumer.accept(this.typeBuilder);
		return this;
	}

	@Override
	public EntityBuilder<T> category(MobCategory category) {
		this.typeBuilder.spawnGroup(category);
		return this;
	}

	@Override
	public EntityBuilder<T> factory(EntityType.EntityFactory<T> factory) {
		this.typeBuilder.entityFactory(factory);
		return this;
	}

	@Override
	public EntityBuilder<T> size(EntityDimensions dimensions) {
		this.typeBuilder.setDimensions(dimensions);
		return this;
	}

	@Override
	public EntityBuilder<T> renderer(Supplier<Supplier<EntityRendererProvider<T>>> supplier) {
		this.rendererSupplier = supplier;
		return this;
	}

	@Override
	public EntityType<T> build() {
		EntityType<T> type = typeBuilder.build();
		ResourceLocation id = registrar.id(this.name);
		Registry.register(BuiltInRegistries.ENTITY_TYPE, id, type);

		if (MinecraftQuiltLoader.getEnvironmentType() == EnvType.CLIENT) {
			buildClient(type);
		}

		return type;
	}

	@ClientOnly
	private void buildClient(EntityType<T> type) {
		EntityRendererProvider<T> provider = this.rendererSupplier == null ? NoopRenderer::new : this.rendererSupplier.get().get();
		EntityRendererRegistry.register(type, provider);
	}
}
