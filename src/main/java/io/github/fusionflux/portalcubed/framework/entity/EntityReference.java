package io.github.fusionflux.portalcubed.framework.entity;

import net.minecraft.Util;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;

import net.minecraft.world.level.Level;

import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public class EntityReference {
	public static final EntityReference EMPTY = new EntityReference(Util.NIL_UUID);

	public final UUID id;

	@Nullable
	private Entity entity;

	public EntityReference(UUID id) {
		this.id = id;
	}

	public EntityReference(Entity entity) {
		this(entity.getUUID());
		this.entity = entity;
	}

	@Nullable
	public Entity get(ServerLevel level) {
		if (this == EMPTY)
			return null;

		if (this.entity == null) {
			this.entity = level.getEntity(this.id);
		}

		if (this.entity != null && this.entity.isRemoved())
			this.entity = null;

		return this.entity;
	}

	@Nullable
	public Entity tryGet(Level level) {
		return level instanceof ServerLevel serverLevel ? this.get(serverLevel) : null;
	}

	public void save(CompoundTag nbt, String key) {
		if (this != EMPTY) {
			nbt.putUUID(key, this.id);
		}
	}

	public static EntityReference of(@Nullable Entity entity) {
		return entity == null ? EMPTY : new EntityReference(entity);
	}

	public static EntityReference parse(CompoundTag nbt, String key) {
		if (!nbt.hasUUID(key))
			return EMPTY;

		UUID id = nbt.getUUID(key);
		return id.equals(Util.NIL_UUID) ? EMPTY : new EntityReference(id);
	}
}
