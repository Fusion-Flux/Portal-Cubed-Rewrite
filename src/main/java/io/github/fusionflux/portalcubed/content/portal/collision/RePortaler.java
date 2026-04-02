package io.github.fusionflux.portalcubed.content.portal.collision;

import java.util.List;
import java.util.function.Predicate;

import com.google.common.collect.ImmutableList;

import io.github.fusionflux.portalcubed.content.portal.Polarity;
import io.github.fusionflux.portalcubed.content.portal.Portal;
import io.github.fusionflux.portalcubed.content.portal.PortalPair;
import io.github.fusionflux.portalcubed.content.portal.manager.listener.PortalChangeListener;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.DismountHelper;
import net.minecraft.world.level.CollisionGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;

public final class RePortaler implements PortalChangeListener {
	public static final double SPEED = 0.25;

	public static final Predicate<Entity> REPORTALABLE = EntitySelector.NO_SPECTATORS.and(entity -> {
		if (entity.noPhysics)
			return false;

		if (entity.level().isClientSide()) {
			return entity instanceof Player player && player.isLocalPlayer();
		}

		return true;
	});

	private final Level level;

	public RePortaler(Level level) {
		this.level = level;
	}

	@Override
	public void portalPairChanged(PortalPair oldPair, PortalPair newPair) {
		for (Polarity polarity : Polarity.values()) {
			if (!oldPair.has(polarity))
				continue;

			Portal portal = oldPair.getOrThrow(polarity);

			if (movedOrRemoved(polarity, portal, newPair)) {
				this.snap(portal);
			} else {
				this.push(portal);
			}
		}
	}

	private void snap(Portal portal) {
		entities: for (Entity entity : this.findEntities(portal)) {
			if (entity.isPassenger())
				continue;

			AABB area = getSnapArea(portal);
			// based on dismount code from several places (mostly Striders)
			for (BlockPos pos : BlockPos.betweenClosed(area)) {
				double height = this.level.getBlockFloorHeight(pos);
				if (!DismountHelper.isBlockFloorValid(height))
					continue;

				Vec3 teleportPos = Vec3.upFromBottomCenterOf(pos, height);
				for (Pose pose : getDismountPoses(entity)) {
					AABB bounds = getLocalBoundsForPose(entity, pose);
					if (canDismountTo(this.level, entity, bounds.move(teleportPos))) {
						entity.setPose(pose);
						entity.teleportTo(teleportPos.x, teleportPos.y, teleportPos.z);
						continue entities;
					}
				}
			}
		}
	}

	private void push(Portal portal) {
		List<Entity> entities = this.findEntities(portal);
		if (entities.isEmpty())
			return;

		Vec3 velocity = portal.normal.scale(SPEED);
		for (Entity entity : entities) {
			entity.addDeltaMovement(velocity);
			entity.hasImpulse = true;
		}
	}

	private List<Entity> findEntities(Portal portal) {
		AABB area = portal.quad.containingBox();
		List<Entity> entities = this.level.getEntities((Entity) null, area, REPORTALABLE);

		entities.removeIf(entity -> {
			AABB bounds = entity.getBoundingBox();
			return !portal.quad.intersects(bounds);
		});

		return entities;
	}

	private static boolean movedOrRemoved(Polarity polarity, Portal oldPortal, PortalPair newPair) {
		return !newPair.has(polarity) || !oldPortal.collisionEquals(newPair.getOrThrow(polarity));
	}

	/// @return the area within which to check each block to see if an entity can snap there
	private static AABB getSnapArea(Portal portal) {
		return AABB.ofSize(portal.origin(), 0, 0, 0).expandTowards(portal.normal);
	}

	// some LivingEntity-exclusive stuff that has no reason to be that way

	/// @see LivingEntity#getDismountPoses()]
	private static ImmutableList<Pose> getDismountPoses(Entity entity) {
		return entity instanceof LivingEntity living ? living.getDismountPoses() : ImmutableList.of(entity.getPose());
	}

	/// @see LivingEntity#getLocalBoundsForPose(Pose)]
	private static AABB getLocalBoundsForPose(Entity entity, Pose pose) {
		if (entity instanceof LivingEntity living) {
			return living.getLocalBoundsForPose(pose);
		}

		EntityDimensions dimensions = entity.getDimensions(pose);
		return new AABB(
				-dimensions.width() / 2, 0, -dimensions.width() / 2,
				dimensions.width() / 2, dimensions.height(), dimensions.width() / 2
		);
	}

	/// @see DismountHelper#canDismountTo(CollisionGetter, LivingEntity, AABB)]
	public static boolean canDismountTo(CollisionGetter level, Entity entity, AABB bounds) {
		if (entity instanceof LivingEntity living) {
			return DismountHelper.canDismountTo(level, living, bounds);
		}

		for (VoxelShape shape : level.getBlockCollisions(entity, bounds)) {
			if (!shape.isEmpty()) {
				return false;
			}
		}

		return level.getWorldBorder().isWithinBounds(bounds);
	}
}
