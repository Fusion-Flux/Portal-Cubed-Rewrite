package io.github.fusionflux.portalcubed.content.portal.projectile;

import io.github.fusionflux.portalcubed.content.PortalCubedEntities;
import io.github.fusionflux.portalcubed.content.portal.PortalData;
import io.github.fusionflux.portalcubed.content.portal.PortalSettings;
import io.github.fusionflux.portalcubed.content.portal.Polarity;
import io.github.fusionflux.portalcubed.framework.entity.UnsavedEntity;

import org.joml.Quaternionf;

import net.minecraft.core.Direction;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import java.util.UUID;

public class PortalProjectile extends UnsavedEntity {
	public static final double SPEED = (6 * 16) / 20f; // 6 chunks per second

	public static final EntityDataAccessor<Integer> COLOR = SynchedEntityData.defineId(PortalProjectile.class, EntityDataSerializers.INT);

	// cached color for rendering
	private int color;

	// only tracked on the server
	private PortalSettings portalSettings;
	private float yRot;
	private UUID pair;
	private Polarity polarity;

	public PortalProjectile(EntityType<?> variant, Level world) {
		super(variant, world);
		this.noPhysics = true;
	}

	public PortalProjectile(Level level, PortalSettings settings, float yRot, UUID pair, Polarity polarity) {
		this(PortalCubedEntities.PORTAL_PROJECTILE, level);
		this.portalSettings = settings;
		this.entityData.set(COLOR, settings.color());
		this.yRot = yRot;
		this.pair = pair;
		this.polarity = polarity;
	}

	public static PortalProjectile create(EntityType<?> type, Level level) {
		// trying to use the constructor directly makes the compiler have a stroke with the generics
		return new PortalProjectile(type, level);
	}

	@Override
	protected void defineSynchedData() {
		entityData.define(COLOR, 0);
	}

	@Override
	public void onSyncedDataUpdated(EntityDataAccessor<?> data) {
		if (data == COLOR) {
			this.color = entityData.get(COLOR);
		}
		super.onSyncedDataUpdated(data);
	}

	@Override
	public void tick() {
		if (this.getDeltaMovement().lengthSqr() == 0) {
			super.tick();
			return;
		}

		Vec3 start = this.position();
		super.tick();
		this.move(MoverType.SELF, getDeltaMovement());
		Vec3 end = this.position();

		Level level = this.level();
		BlockHitResult hit = level.clip(new ClipContext(
				start, end, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, this
		));
		if (hit.getType() == HitResult.Type.BLOCK) {
			this.setDeltaMovement(0, 0, 0);
			if (level instanceof ServerLevel serverLevel) {
				this.discard();
				this.spawnPortal(serverLevel, hit);
			}
		}
	}

	private void spawnPortal(ServerLevel level, BlockHitResult hit) {
		if (this.portalSettings == null || this.pair == null || this.polarity == null)
			return;

		Quaternionf rotation = getPortalRotation(hit.getDirection(), this.yRot);
		PortalData data = new PortalData(hit.getLocation(), rotation, this.portalSettings);
		level.portalManager().createPortal(this.pair, this.polarity, data);
	}

	public int getColor() {
		return this.color;
	}

	public static Quaternionf getPortalRotation(Direction normal, float yRot) {
		Quaternionf rotation = normal.getRotation();
		rotation.rotateX(Mth.DEG_TO_RAD * 270);
		rotation.rotateY(Mth.DEG_TO_RAD * 180);
		rotation.rotateZ(Mth.DEG_TO_RAD * switch (normal) {
			case UP -> yRot;
			case DOWN -> -yRot;
			default -> 0;
		});
		return rotation;
	}
}
