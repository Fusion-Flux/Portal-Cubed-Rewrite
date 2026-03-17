package io.github.fusionflux.portalcubed.framework.raycast;

import java.util.Optional;
import java.util.function.Predicate;

import org.jetbrains.annotations.Nullable;

import io.github.fusionflux.portalcubed.content.portal.interaction.IgnoringClipContextMode;
import io.github.fusionflux.portalcubed.mixin.utils.accessors.ClipContextAccessor;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.EntityCollisionContext;

/**
 * Centralized raycasting logic. Allows for performing any kind of raycast with just one method call.
 */
public record RaycastOptions(ClipContext.Block blockMode, ClipContext.Fluid fluidMode, Optional<Predicate<Entity>> entityPredicate,
							 PortalMode portalMode, CollisionContext collisionContext, double blockRange, double entityRange,
							 boolean hitWorldBorder, boolean ignoreInteractionOverride, float entityExpansion) {

	public static final RaycastOptions DEFAULT = new RaycastOptions(
			ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, Optional.of(EntitySelector.CAN_BE_PICKED),
			PortalMode.PASS_THROUGH, CollisionContext.empty(), Double.MAX_VALUE, Double.MAX_VALUE,
			false, false, 0
	);

	public RaycastOptions {
		if (blockRange < 0) {
			throw new IllegalArgumentException("Block range override cannot be <0");
		} else if (entityRange < 0) {
			throw new IllegalArgumentException("Entity range override cannot be <0");
		}
	}

	public RaycastResult raycast(Level level, Vec3 from, Vec3 to) {
		return new RayCaster(level, from, to, this).raycast();
	}

	public RaycastResult raycast(Level level, Vec3 from, Vec3 direction, double distance) {
		if (direction.lengthSqr() == 0) {
			throw new IllegalArgumentException("Direction has no length");
		} else if (distance <= 0) {
			throw new IllegalArgumentException("Distance must be >0");
		}

		Vec3 to = from.add(direction.normalize().scale(distance));
		return this.raycast(level, from, to);
	}

	public boolean shouldClipLevel() {
		return this.blockMode != NoneClipContextMode.get() || this.fluidMode != ClipContext.Fluid.NONE;
	}

	public ClipContext createClipContext(Vec3 start, Vec3 end, @Nullable BlockPos ignoredBlock) {
		ClipContext.Block blockMode = IgnoringClipContextMode.maybeWrap(this.blockMode, ignoredBlock);
		ClipContext context = new ClipContext(start, end, blockMode, this.fluidMode, this.collisionContext);
		context.pc$setIgnoreInteractionOverride(this.ignoreInteractionOverride);
		return context;
	}

	public BlockHitResult clipLevel(Level level, ClipContext context) {
		return this.hitWorldBorder ? level.clipIncludingBorder(context) : level.clip(context);
	}

	@Nullable
	public Entity contextEntity() {
		return this.collisionContext instanceof EntityCollisionContext entityCtx ? entityCtx.getEntity() : null;
	}

	public Builder edit() {
		return new Builder(this);
	}

	public static Builder of(ClipContext context) {
		ClipContextAccessor accessor = (ClipContextAccessor) context;

		return DEFAULT.edit()
				.blocks(accessor.getBlock())
				.fluids(accessor.getFluid())
				.entities(Optional.empty())
				.collisionContext(accessor.getCollisionContext());
	}

	public enum PortalMode {
		HIT, PASS_THROUGH, IGNORE
	}

	public static final class Builder {
		private ClipContext.Block blockMode;
		private ClipContext.Fluid fluidMode;
		private Optional<Predicate<Entity>> entityPredicate;
		private PortalMode portalMode;
		private CollisionContext collisionContext;
		private double blockRange;
		private double entityRange;
		private boolean hitWorldBorder;
		private boolean ignoreInteractionOverride;
		private float entityExpansion;

		private Builder(RaycastOptions options) {
			this.blockMode = options.blockMode;
			this.fluidMode = options.fluidMode;
			this.entityPredicate = options.entityPredicate;
			this.portalMode = options.portalMode;
			this.collisionContext = options.collisionContext;
			this.blockRange = options.blockRange;
			this.entityRange = options.entityRange;
			this.hitWorldBorder = options.hitWorldBorder;
			this.ignoreInteractionOverride = options.ignoreInteractionOverride;
		}

		public Builder blocks(ClipContext.Block mode) {
			this.blockMode = mode;
			return this;
		}

		public Builder fluids(ClipContext.Fluid mode) {
			this.fluidMode = mode;
			return this;
		}

		public Builder entities(Optional<Predicate<Entity>> predicate) {
			this.entityPredicate = predicate;
			return this;
		}

		public Builder entities(Predicate<Entity> predicate) {
			return this.entities(Optional.of(predicate));
		}

		public Builder portals(PortalMode mode) {
			this.portalMode = mode;
			return this;
		}

		public Builder collisionContext(CollisionContext context) {
			this.collisionContext = context;
			return this;
		}

		public Builder collisionContext(@Nullable Entity entity) {
			return this.collisionContext(entity == null ? CollisionContext.empty() : CollisionContext.of(entity));
		}

		public Builder blockRange(double range) {
			if (range < 0) {
				throw new IllegalArgumentException("Range cannot be <0");
			}

			this.blockRange = range;
			return this;
		}

		public Builder entityRange(double range) {
			if (range < 0) {
				throw new IllegalArgumentException("Range cannot be <0");
			}

			this.entityRange = range;
			return this;
		}

		public Builder hitWorldBorder(boolean hitWorldBorder) {
			this.hitWorldBorder = hitWorldBorder;
			return this;
		}

		public Builder ignoreInteractionOverride(boolean ignore) {
			this.ignoreInteractionOverride = ignore;
			return this;
		}

		public Builder entityExpansion(float expansion) {
			this.entityExpansion = expansion;
			return this;
		}

		public Builder forPlayer(Player player) {
			this.collisionContext(player);
			this.blockRange(player.blockInteractionRange());
			this.entityRange(player.entityInteractionRange());
			return this;
		}

		public RaycastOptions build() {
			return new RaycastOptions(
					this.blockMode, this.fluidMode, this.entityPredicate,
					this.portalMode, this.collisionContext, this.blockRange, this.entityRange,
					this.hitWorldBorder, this.ignoreInteractionOverride, this.entityExpansion
			);
		}
	}
}
