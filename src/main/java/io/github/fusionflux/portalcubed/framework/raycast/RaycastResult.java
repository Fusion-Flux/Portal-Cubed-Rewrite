package io.github.fusionflux.portalcubed.framework.raycast;

import java.util.Optional;

import io.github.fusionflux.portalcubed.content.portal.ref.PortalPath;
import io.github.fusionflux.portalcubed.content.portal.ref.PortalPathHolder;
import io.github.fusionflux.portalcubed.content.portal.ref.PortalReference;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

/**
 * The result of a raycast performed with {@link RaycastOptions}.
 */
public sealed abstract class RaycastResult {
	/**
	 * The {@link PortalPath} the raycast passed through, if it passed through any portals.
	 */
	public final Optional<PortalPath> path;

	public final Vec3 pos;

	protected RaycastResult(Optional<PortalPath> path, Vec3 pos) {
		this.path = path;
		this.pos = pos;
	}

	public final boolean isCloserToPosThan(Vec3 pos, RaycastResult that) {
		double thisDistance = this.pos.distanceToSqr(pos);
		double thatDistance = that.pos.distanceToSqr(pos);

		// bias slightly in favor of Portal results, so we prioritize portals
		// that are exactly on their surface over the surface itself.
		double thisBias = biasOf(this);
		double thatBias = biasOf(that);

		return thisDistance - thisBias < thatDistance - thatBias;
	}

	public final boolean passedThroughPortals() {
		return this.path.isPresent();
	}

	public abstract RaycastResult withPath(PortalPath path);

	public abstract VanillaConvertible assertNotPortal();

	protected <T extends HitResult> T addPortalContext(T result) {
		result.setPortalPath(PortalPathHolder.of(this.path));
		return result;
	}

	public static BlockLike of(BlockHitResult blockHit) {
		Vec3 pos = blockHit.getLocation();
		Direction face = blockHit.getDirection();

		if (blockHit.getType() == HitResult.Type.MISS) {
			return new Missed(pos, face);
		}

		if (blockHit.isWorldBorderHit()) {
			return new WorldBorder(pos, face);
		} else {
			return new Block(pos, blockHit.getBlockPos(), face, blockHit.isInside());
		}
	}

	private static double biasOf(RaycastResult result) {
		return result instanceof Portal ? 0.01 : 0;
	}

	public static abstract sealed class VanillaConvertible extends RaycastResult {
		protected VanillaConvertible(Optional<PortalPath> path, Vec3 pos) {
			super(path, pos);
		}

		public abstract HitResult toVanilla();

		@Override
		public final VanillaConvertible assertNotPortal() {
			return this;
		}
	}

	public static abstract sealed class BlockLike extends VanillaConvertible permits Missed, Block, WorldBorder {
		public final BlockPos blockPos;
		public final Direction face;

		protected BlockLike(Optional<PortalPath> path, Vec3 pos, BlockPos blockPos, Direction face) {
			super(path, pos);
			this.blockPos = blockPos;
			this.face = face;
		}

		@Override
		public abstract BlockHitResult toVanilla();
	}

	public static final class Missed extends BlockLike {
		public Missed(Optional<PortalPath> path, Vec3 pos, Direction face) {
			super(path, pos, BlockPos.containing(pos), face);
		}

		public Missed(Vec3 pos, Direction face) {
			this(Optional.empty(), pos, face);
		}

		@Override
		public Missed withPath(PortalPath path) {
			return new Missed(Optional.of(path), this.pos, this.face);
		}

		@Override
		public BlockHitResult toVanilla() {
			return this.addPortalContext(BlockHitResult.miss(this.pos, this.face, this.blockPos));
		}
	}

	public static final class Block extends BlockLike {
		public final boolean isInside;

		public Block(Optional<PortalPath> path, Vec3 pos, BlockPos blockPos, Direction face, boolean isInside) {
			super(path, pos, blockPos, face);
			this.isInside = isInside;
		}

		public Block(Vec3 pos, BlockPos blockPos, Direction face, boolean isInside) {
			this(Optional.empty(), pos, blockPos, face, isInside);
		}

		@Override
		public Block withPath(PortalPath path) {
			return new Block(Optional.of(path), this.pos, this.blockPos, this.face, this.isInside);
		}

		@Override
		public BlockHitResult toVanilla() {
			return this.addPortalContext(new BlockHitResult(this.pos, this.face, this.blockPos, this.isInside));
		}
	}

	public static final class WorldBorder extends BlockLike {
		public WorldBorder(Optional<PortalPath> path, Vec3 pos, Direction face) {
			super(path, pos, BlockPos.containing(pos), face);
		}

		public WorldBorder(Vec3 pos, Direction face) {
			this(Optional.empty(), pos, face);
		}

		@Override
		public WorldBorder withPath(PortalPath path) {
			return new WorldBorder(Optional.of(path), this.pos, this.face);
		}

		@Override
		public BlockHitResult toVanilla() {
			return this.addPortalContext(new BlockHitResult(this.pos, this.face, this.blockPos, false, true));
		}
	}

	public static final class Entity extends VanillaConvertible {
		public final net.minecraft.world.entity.Entity entity;

		public Entity(Optional<PortalPath> path, Vec3 pos, net.minecraft.world.entity.Entity entity) {
			super(path, pos);
			this.entity = entity;
		}

		public Entity(Vec3 pos, net.minecraft.world.entity.Entity entity) {
			this(Optional.empty(), pos, entity);
		}

		@Override
		public Entity withPath(PortalPath path) {
			return new Entity(Optional.of(path), this.pos, this.entity);
		}

		@Override
		public EntityHitResult toVanilla() {
			return this.addPortalContext(new EntityHitResult(this.entity, this.pos));
		}
	}

	public static final class Portal extends RaycastResult {
		public final PortalReference portal;

		public Portal(Optional<PortalPath> path, Vec3 pos, PortalReference portal) {
			super(path, pos);
			this.portal = portal;
		}

		public Portal(Vec3 pos, PortalReference portal) {
			this(Optional.empty(), pos, portal);
		}

		@Override
		public Portal withPath(PortalPath path) {
			return new Portal(Optional.of(path), this.pos, this.portal);
		}

		@Override
		public VanillaConvertible assertNotPortal() {
			throw new IllegalStateException("Raycast shouldn't've hit a portal, but it did: " + this.portal);
		}
	}
}
