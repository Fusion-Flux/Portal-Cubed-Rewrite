package io.github.fusionflux.portalcubed.content.portal.projectile;

import io.github.fusionflux.portalcubed.content.PortalCubedEntities;
import io.github.fusionflux.portalcubed.content.portal.PortalData;
import io.github.fusionflux.portalcubed.content.portal.PortalSettings;
import io.github.fusionflux.portalcubed.content.portal.PortalType;
import io.github.fusionflux.portalcubed.framework.entity.UnsavedEntity;
import net.minecraft.core.Direction;
import net.minecraft.core.FrontAndTop;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import java.util.Objects;
import java.util.UUID;

public class PortalProjectile extends UnsavedEntity {
	public static final double SPEED = (6 * 16) / 20f; // 6 chunks per second

	public static final EntityDataAccessor<Integer> COLOR = SynchedEntityData.defineId(PortalProjectile.class, EntityDataSerializers.INT);

	// cached color for rendering
	private int color;

	// only tracked on the server
	private PortalSettings portalSettings;
	private Direction horizontalFacing;
	private UUID pair;
	private PortalType type;

	public PortalProjectile(EntityType<?> variant, Level world) {
		super(variant, world);
		this.noPhysics = true;
	}

	public PortalProjectile(Level level, PortalSettings settings, Direction horizontalFacing, UUID pair, PortalType type) {
		this(PortalCubedEntities.PORTAL_PROJECTILE, level);
		this.portalSettings = settings;
		this.entityData.set(COLOR, settings.color());
		this.horizontalFacing = horizontalFacing;
		this.pair = pair;
		this.type = type;
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
		if (this.portalSettings == null || this.horizontalFacing == null || this.pair == null || this.type == null)
			return;

		Direction facing = hit.getDirection();
		Vec3 pos = hit.getLocation();
		Direction top = facing.getAxis().isHorizontal() ? Direction.UP : horizontalFacing;
		FrontAndTop orientation = Objects.requireNonNull(FrontAndTop.fromFrontAndTop(facing, top));
		PortalData data = new PortalData(pos, orientation, this.portalSettings);
		level.portalManager().createPortal(this.pair, this.type, data);
	}

	public int getColor() {
		return this.color;
	}
}
