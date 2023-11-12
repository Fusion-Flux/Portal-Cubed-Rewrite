package io.github.fusionflux.portalcubed.content.portal.projectile;

import io.github.fusionflux.portalcubed.content.PortalCubedEntities;
import io.github.fusionflux.portalcubed.content.portal.Portal;
import io.github.fusionflux.portalcubed.content.portal.PortalPair;
import io.github.fusionflux.portalcubed.content.portal.PortalSettings;
import io.github.fusionflux.portalcubed.content.portal.PortalType;
import io.github.fusionflux.portalcubed.content.portal.manager.ServerPortalManager;
import io.github.fusionflux.portalcubed.framework.UnsavedEntity;
import net.minecraft.core.Direction;
import net.minecraft.core.FrontAndTop;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.player.Player;
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

	// server only
	private PortalSettings portalSettings;
	private Direction shooterFacing;
	private UUID shooter;
	private PortalType type;
	// dual
	private int color;

	public PortalProjectile(EntityType<?> variant, Level world) {
		super(variant, world);
		this.noPhysics = true;
	}

	public static PortalProjectile create(Level level, Player shooter, PortalSettings settings, PortalType type, Direction shooterFacing) {
		PortalProjectile projectile = new PortalProjectile(PortalCubedEntities.PORTAL_PROJECTILE, level);
		projectile.entityData.set(COLOR, settings.color());
		projectile.portalSettings = settings;
		projectile.shooterFacing = shooterFacing;
		projectile.shooter = shooter.getUUID();
		projectile.type = type;
		return projectile;
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
		if (portalSettings == null || shooterFacing == null || shooter == null || type == null)
			return;

		Direction facing = hit.getDirection();
		Vec3 pos = hit.getLocation();
		Direction top = facing.getAxis().isHorizontal() ? Direction.UP : shooterFacing;
		FrontAndTop orientation = Objects.requireNonNull(FrontAndTop.fromFrontAndTop(facing, top));
		ServerPortalManager manager = ServerPortalManager.of(level);

		manager.createPortal(pos, orientation, portalSettings.color(), portalSettings.shape(), type, shooter);
	}

	public int getColor() {
		return this.color;
	}
}
