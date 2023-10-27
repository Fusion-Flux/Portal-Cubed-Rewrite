package io.github.fusionflux.portalcubed.framework.registration.entity;

import io.github.fusionflux.portalcubed.framework.registration.Registrar;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;

public class EntityHelper {
	private final Registrar registrar;

	public EntityHelper(Registrar registrar) {
		this.registrar = registrar;
	}

	public <T extends Entity> EntityBuilder<T> create(String name, EntityType.EntityFactory<T> factory) {
		return new EntityBuilderImpl<>(registrar, name, factory);
	}
}
