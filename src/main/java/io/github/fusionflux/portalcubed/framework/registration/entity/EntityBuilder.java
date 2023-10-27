package io.github.fusionflux.portalcubed.framework.registration.entity;

import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.world.entity.Entity;

import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;

import net.minecraft.world.entity.MobCategory;

import org.quiltmc.qsl.entity.api.QuiltEntityTypeBuilder;

import java.util.function.Consumer;
import java.util.function.Supplier;

public interface EntityBuilder<T extends Entity> {
	/**
	 * Arbitrarily configure the entity type builder.
	 */
	EntityBuilder<T> configure(Consumer<QuiltEntityTypeBuilder<T>> consumer);

	/**
	 * Set the category of this entity type.
	 */
	EntityBuilder<T> category(MobCategory category);

	/**
	 * Set the factory of this entity type.
	 */
	EntityBuilder<T> factory(EntityType.EntityFactory<T> factory);

	/**
	 * Set the dimensions of this entity type.
	 */
	EntityBuilder<T> size(EntityDimensions dimensions);

	/**
	 * Set the renderer for this entity type.
	 */
	EntityBuilder<T> renderer(Supplier<Supplier<EntityRendererProvider<T>>> supplier);

	/**
	 * Build this builder into an entity type.
	 */
	EntityType<T> build();
}
