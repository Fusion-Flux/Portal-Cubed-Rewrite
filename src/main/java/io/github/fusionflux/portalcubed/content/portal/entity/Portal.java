package io.github.fusionflux.portalcubed.content.portal.entity;

import io.github.fusionflux.portalcubed.content.PortalCubedEntities;
import io.github.fusionflux.portalcubed.content.PortalCubedSerializers;
import io.github.fusionflux.portalcubed.content.portal.PortalShape;
import io.github.fusionflux.portalcubed.content.portal.PortalType;
import io.github.fusionflux.portalcubed.framework.UnsavedEntity;
import net.minecraft.core.Direction;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import org.jetbrains.annotations.Nullable;
import org.joml.Quaternionf;

public class Portal extends UnsavedEntity {

	public static final EntityDataAccessor<Integer> LINKED = SynchedEntityData.defineId(Portal.class, EntityDataSerializers.INT);
	public static final EntityDataAccessor<PortalType> TYPE = SynchedEntityData.defineId(Portal.class, PortalCubedSerializers.PORTAL_TYPE);
	public static final EntityDataAccessor<PortalShape> SHAPE = SynchedEntityData.defineId(Portal.class, PortalCubedSerializers.PORTAL_SHAPE);
	public static final EntityDataAccessor<Integer> COLOR = SynchedEntityData.defineId(Portal.class, EntityDataSerializers.INT);

	private Portal linkedPortal;
	private PortalType type;
	private PortalShape shape;
	private int color;

	public Portal(EntityType<?> variant, Level world) {
		super(variant, world);
		this.noPhysics = true;
	}

	public static Portal create(Level level, int color, Vec3 pos, Direction horizontalFacing, @Nullable Direction verticalFacing) {
		Portal portal = new Portal(PortalCubedEntities.PORTAL, level);
		portal.setPos(pos);
		if (verticalFacing != null) {
			portal.setXRot(verticalFacing == Direction.UP ? -90 : 90);
			portal.setYRot(verticalFacing == Direction.UP ? horizontalFacing.toYRot() : -horizontalFacing.toYRot());
		} else {
			portal.setYRot(horizontalFacing.toYRot());
		}
		portal.entityData.set(COLOR, color);
		return portal;
	}

	@Override
	protected void defineSynchedData() {
		entityData.define(LINKED, -1);
		entityData.define(TYPE, this.type = PortalType.PRIMARY);
		entityData.define(SHAPE, this.shape = PortalShape.SQUARE);
		entityData.define(COLOR, PortalType.PRIMARY.defaultColor);
	}

	@Override
	public void onSyncedDataUpdated(EntityDataAccessor<?> data) {
		if (data == LINKED) {
			int id = entityData.get(LINKED);
			if (id != -1 && this.level().getEntity(id) instanceof Portal otherPortal) {
				this.linkedPortal = otherPortal;
			}
		} else if (data == TYPE) {
			this.type = entityData.get(TYPE);
		} else if (data == SHAPE) {
			this.shape = entityData.get(SHAPE);
		} else if (data == COLOR) {
			this.color = entityData.get(COLOR);
		}
	}

	@Nullable
	public Portal getLinkedPortal() {
		return linkedPortal;
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
