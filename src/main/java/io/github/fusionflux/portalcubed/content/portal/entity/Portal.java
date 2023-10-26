package io.github.fusionflux.portalcubed.content.portal.entity;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;

import org.joml.Quaternionf;

import java.util.Optional;
import java.util.UUID;

public class Portal extends Entity {
	public static final EntityDataAccessor<Optional<UUID>> LINKED = SynchedEntityData.defineId(Portal.class, EntityDataSerializers.OPTIONAL_UUID);
	public static final EntityDataAccessor<Quaternionf> ROTATION = SynchedEntityData.defineId(Portal.class, EntityDataSerializers.QUATERNION);

	private Portal linkedPortal;
	private Quaternionf rotation;

	public Portal(EntityType<?> variant, Level world) {
		super(variant, world);
	}

	@Override
	protected void defineSynchedData() {
		entityData.define(LINKED, Optional.empty());
		entityData.define(ROTATION, this.rotation = new Quaternionf());
	}

	@Override
	protected void addAdditionalSaveData(CompoundTag nbt) {
		ExtraCodecs.QUATERNIONF.encodeStart(NbtOps.INSTANCE, this.rotation).result()
				.ifPresent(tag -> nbt.put("rotation", tag));
		if (linkedPortal != null) {
			nbt.putUUID("linked_portal", linkedPortal.getUUID());
		}
	}

	@Override
	protected void readAdditionalSaveData(CompoundTag nbt) {
		ExtraCodecs.QUATERNIONF.decode(NbtOps.INSTANCE, nbt.get("rotation")).result()
				.ifPresent(pair -> entityData.set(ROTATION, pair.getFirst()));
		if (nbt.contains("linked_portal")) {
			UUID uuid = nbt.getUUID("linked_portal");
			entityData.define(LINKED, Optional.of(uuid));
		}
	}
}
