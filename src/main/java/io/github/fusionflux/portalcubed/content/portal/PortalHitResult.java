package io.github.fusionflux.portalcubed.content.portal;

import net.minecraft.core.Rotations;
import net.minecraft.world.phys.Vec3;

import java.util.Iterator;
import java.util.Objects;

import org.jetbrains.annotations.Nullable;

/**
 * A result of a raycast that passes through a pair of portals.
 * Each result may have either a next result or an end position. If there is another result, then the raycast passed
 * through multiple portals.
 *
 * @param start  Start of the raycast.
 * @param end    End of the raycast, teleported through the portals. This is non-null when next is null.
 * @param in     The portal that was entered.
 * @param out    The portal that was exited.
 * @param pair   The pair of portals passed through.
 * @param inHit  Position where the entered portal was hit.
 * @param outHit Position where the exited portal was hit.
 * @param next   The next hit result in the chain. Null when end is non-null.
 */
public record PortalHitResult(Vec3 start, @Nullable Vec3 end,
							  PortalInstance in, PortalInstance out, PortalPair pair,
							  Vec3 inHit, Vec3 outHit,
							  @Nullable PortalHitResult next) {

	public static final PortalHitResult OVERFLOW_MARKER = new PortalHitResult(null, null, null, null, null, null, null, null);

	public PortalHitResult(Vec3 start, @Nullable Vec3 end,
						   PortalInstance in, PortalInstance out, PortalPair pair,
						   Vec3 inHit, Vec3 outHit,
						   @Nullable PortalHitResult next) {
		this.start = start;
		this.end = end;
		this.inHit = inHit;
		this.outHit = outHit;
		this.in = in;
		this.out = out;
		this.pair = pair;
		this.next = next;

		// both null or both non-null
//		if ((end == null) == (next == null)) {
//			throw new IllegalArgumentException();
//		}
	}

	public boolean hasNext() {
		return this.next != null;
	}

	@Override
	public PortalHitResult next() {
		return Objects.requireNonNull(this.next);
	}

	public boolean isEnd() {
		return this.end != null;
	}

	@Override
	public Vec3 end() {
		return Objects.requireNonNull(this.end);
	}

	public Vec3 findEnd() {
		return this.isEnd() ? this.end() : this.next().findEnd();
	}

	public PortalHitResult getLast() {
		return this.hasNext() ? this.next.getLast() : this;
	}

	public int depth() {
		return 1 + (this.hasNext() ? this.next.depth() : 0);
	}

	public Vec3 teleportAbsoluteVec(Vec3 pos) {
		return PortalTeleportHandler.teleportAbsoluteVecBetween(pos, in, out);
	}

	public Vec3 teleportRelativeVec(Vec3 vec) {
		return PortalTeleportHandler.teleportRelativeVecBetween(vec, in, out);
	}

	public Rotations teleportRotations(float xRot, float yRot) {
		return PortalTeleportHandler.teleportRotations(xRot, yRot, in, out);
	}
}
