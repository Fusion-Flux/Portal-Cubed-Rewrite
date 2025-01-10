package io.github.fusionflux.portalcubed.framework.registration.entity;

import java.util.function.Consumer;
import java.util.function.Supplier;

import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;

public interface EntityBuilder<T extends Entity> {
	/**
	 * Arbitrarily configure the entity type builder.
	 */
	EntityBuilder<T> configure(Consumer<EntityType.Builder<T>> consumer);

	/**
	 * Set the dimensions of this entity type.
	 */
	EntityBuilder<T> size(float width, float height);

	/**
	 * Set the renderer for this entity type.
	 */
	EntityBuilder<T> renderer(Supplier<Supplier<EntityRendererProvider<T>>> supplier);

	/**
	 * Build this builder into an entity type.
	 */
	EntityType<T> build();
}
