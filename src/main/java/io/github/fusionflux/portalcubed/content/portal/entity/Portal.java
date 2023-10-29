package io.github.fusionflux.portalcubed.content.portal.entity;

import io.github.fusionflux.portalcubed.content.PortalCubedSerializers;
import io.github.fusionflux.portalcubed.content.portal.PortalShape;
import io.github.fusionflux.portalcubed.content.portal.PortalType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;

import org.jetbrains.annotations.Nullable;
import org.joml.Quaternionf;
import org.joml.Quaternionfc;

import java.util.Optional;
import java.util.UUID;

public class Portal extends Entity {
	public static final int PRIMARY_DEFAULT_COLOR = 0xB00B69;
	public static final int SECONDARY_DEFAULT_COLOR = 0x69B00B;

	public static final EntityDataAccessor<Optional<UUID>> LINKED = SynchedEntityData.defineId(Portal.class, EntityDataSerializers.OPTIONAL_UUID);
	public static final EntityDataAccessor<Quaternionf> ROTATION = SynchedEntityData.defineId(Portal.class, EntityDataSerializers.QUATERNION);
	public static final EntityDataAccessor<PortalType> TYPE = SynchedEntityData.defineId(Portal.class, PortalCubedSerializers.PORTAL_TYPE);
	public static final EntityDataAccessor<PortalShape> SHAPE = SynchedEntityData.defineId(Portal.class, PortalCubedSerializers.PORTAL_SHAPE);
	public static final EntityDataAccessor<Integer> COLOR = SynchedEntityData.defineId(Portal.class, EntityDataSerializers.INT);

	private Portal linkedPortal;
	private Quaternionf rotation;
	private PortalType type;
	private PortalShape shape;
	private int color;

	public Portal(EntityType<?> variant, Level world) {
		super(variant, world);
	}

	@Override
	protected void defineSynchedData() {
		entityData.define(LINKED, Optional.empty());
		entityData.define(ROTATION, this.rotation = new Quaternionf(0, 1, 0, 1).normalize());
		entityData.define(TYPE, this.type = PortalType.PRIMARY);
		entityData.define(SHAPE, this.shape = PortalShape.SQUARE);
		entityData.define(COLOR, this.color = PRIMARY_DEFAULT_COLOR);
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

	@Nullable
	public Portal getLinkedPortal() {
		return linkedPortal;
	}

	public Quaternionf getRotation() {
		return rotation;
	}

	public PortalType getPortalType() {
		return type;
	}

	public PortalShape getPortalShape() {
		return shape;
	}

	public int getColor() {
		return color;
	}
}
