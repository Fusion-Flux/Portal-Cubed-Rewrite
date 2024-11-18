package io.github.fusionflux.portalcubed.content.portal;

import java.util.UUID;

/**
 * Tracks progress through a teleport client-side.
 * Lifecycle:
 * - server entity teleports
 * - client is notified of portals passed through
 * - client creates a tracker and determines timeoutAge based on entity type
 * - tracker is stored on entity
 * - tracker is notified every time the client entity passes through a portal
 *   - if the end is reached, it is removed
 *   - if the wrong portal is passed through, it is removed
 * - tracker is queried every tick by its entity for if it should timeout. This is a failsafe for when the client and server disagree.
 *   - ex. client never makes it through a portal the server made it through
 *   - if it gives up, it is removed
 * while the tracker is present:
 * - all motion packets are reinterpreted to pass through the tracked portals
 * if the server passes through another portal before the client is done tracking:
 * - the next info is appended to the current tracker
 */
public class TeleportProgressTracker {
	/**
	 * Ticks until tracking gives up. Magic number, chosen because it's short but sufficient.
	 * Needs to be greater than the lerp steps for entities, which is:
	 * - 3 for normal entities
	 * - 10 for boats
	 * - 5 for minecarts
	 * - 9 for props
	 * also needs an unknown amount spare, because entities are always more behind than they should be.
	 */
	public static final int TIMEOUT_TICKS = 20;
	private int timeoutAge;
	private PortalTeleportInfo currentInfo;

	public TeleportProgressTracker(int timeoutAge, PortalTeleportInfo info) {
		this.timeoutAge = timeoutAge;
		this.currentInfo = info;
	}

	/**
	 * Notify the tracker that a pair of portals has been passed through.
	 */
	public void notify(UUID pair, PortalType entered) {
		if (this.currentInfo != null) {
			if (this.currentInfo.matches(pair, entered)) {
				System.out.println("tracker matched");
				this.currentInfo = this.currentInfo.next();
			} else {
				// mismatch, give up
				this.currentInfo = null;
			}
		}
	}

	/**
	 * Append a new teleport info to the end of the tracker and update the timeout age.
	 */
	public void append(PortalTeleportInfo newInfo, int newTimeoutAge) {
		this.timeoutAge = newTimeoutAge;
		this.currentInfo = this.currentInfo.append(newInfo);
	}

	/**
	 * Called every tick to see if tracking has gone wrong and should be discarded.
	 */
	public boolean hasTimedOut(int entityAge) {
		return entityAge >= this.timeoutAge;
	}

	public boolean isComplete() {
		return this.currentInfo == null;
	}

	public PortalTeleportInfo getCurrentInfo() {
		return this.currentInfo;
	}
}
