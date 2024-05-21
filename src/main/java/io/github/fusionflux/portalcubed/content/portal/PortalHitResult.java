package io.github.fusionflux.portalcubed.content.portal;

import net.minecraft.world.phys.Vec3;

import java.util.Objects;

import org.jetbrains.annotations.Nullable;

/**
 * A result of a raycast that passes through a pair of portals.
 * Each result may have either a next result or an end position. If there is another result, then the raycast passed
 * through multiple portals.
 */
public class PortalHitResult {
	/** Start of the raycast. */
	public final Vec3 start;
	/** End of the raycast, teleported through the portals. This is non-null when next is null. */
	@Nullable
	private final Vec3 end;

	/** The portal that was entered. */
	public final PortalInstance in;
	/** The portal that was exited. */
	public final PortalInstance out;
	/** The pair of portals passed through. */
	public final PortalPair pair;

	/** Position where the entered portal was hit. */
	public final Vec3 inHit;
	/** Position where the exited portal was hit. */
	public final Vec3 outHit;

	/** The next hit result in the chain. Null when end is non-null. */
	@Nullable
	private final PortalHitResult next;

	PortalHitResult(Vec3 start, @Nullable Vec3 end,
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
		if ((end == null) == (next == null)) {
			throw new IllegalArgumentException();
		}
	}

	public boolean hasNext() {
		return this.next != null;
	}

	public PortalHitResult next() {
		return Objects.requireNonNull(this.next);
	}

	public boolean isEnd() {
		return this.end != null;
	}

	public Vec3 end() {
		return Objects.requireNonNull(this.end);
	}

	public Vec3 teleportAbsoluteVec(Vec3 pos) {
		return PortalTeleportHandler.teleportAbsoluteVecBetween(pos, in, out);
	}

	public Vec3 teleportRelativeVec(Vec3 vec) {
		return PortalTeleportHandler.teleportRelativeVecBetween(vec, in, out);
	}
}
