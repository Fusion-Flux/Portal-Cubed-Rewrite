package io.github.fusionflux.portalcubed.content.portal;

import java.util.Optional;

import org.jetbrains.annotations.ApiStatus;

import io.github.fusionflux.portalcubed.content.portal.manager.PortalManager;
import io.github.fusionflux.portalcubed.content.portal.transform.SinglePortalTransform;

/**
 * A live reference to a {@link Portal}.
 * <p>
 * If the portal is modified, this reference will be updated. If a portal is removed, then this
 * reference is no longer valid. Attempting to continue using it will throw an exception.
 * <p>
 * Once a reference has been removed, it will never be un-removed.
 */
public final class PortalReference {
	public final PortalId id;

	private final PortalManager manager;

	private Portal portal;

	public PortalReference(PortalId id, PortalManager manager, Portal initialPortal) {
		this.id = id;
		this.manager = manager;
		this.portal = initialPortal;
	}

	/**
	 * @return the referenced portal
	 * @throws IllegalStateException if the referenced portal has been removed
	 */
	public Portal get() throws IllegalStateException {
		if (this.portal == null) {
			throw new IllegalStateException("Portal " + this.id + " has been removed");
		}

		return this.portal;
	}

	/**
	 * @return a reference to the opposite linked portal, if it exists
	 */
	public Optional<PortalReference> opposite() {
		return Optional.ofNullable(this.manager.getPortal(this.id.opposite()));
	}

	/**
	 * @return a {@link SinglePortalTransform} from this portal to its opposite, if it exists
	 */
	public Optional<SinglePortalTransform> transform() {
		return this.opposite().map(opposite -> new SinglePortalTransform(this.get(), opposite.get()));
	}

	public boolean isLinked() {
		return this.opposite().isPresent();
	}

	/**
	 * @return true if the referenced portal has been removed
	 */
	public boolean isRemoved() {
		return this.portal == null;
	}

	@ApiStatus.Internal
	public void update(Portal portal) {
		if (this.portal == null && portal != null) {
			throw new IllegalStateException("A PortalReference cannot be un-removed");
		}

		this.portal = portal;
	}
}
