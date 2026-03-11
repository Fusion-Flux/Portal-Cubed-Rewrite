package io.github.fusionflux.portalcubed.content.portal.ref;

import java.util.Optional;

import net.minecraft.world.phys.HitResult;

/// A holder of a [PortalPath] that represents one of three states:
/// 1. A path is present
/// 2. A path is not present, because it's known no portals were passed through
/// 3. A path is not present, because the path is unknown
///
/// This is used to provide portal context to [HitResult]s. It may be important to have
/// a distinction between paths that are missing because no portals were passed through,
/// and paths that are missing because no value was set.
public sealed interface PortalPathHolder {
	static PortalPathHolder.Known of(Optional<PortalPath> path) {
		return path.isEmpty() ? Empty.INSTANCE : new Present(path.get());
	}

	/// A subset of path holders that are "known," meaning they were explicitly set.
	sealed interface Known extends PortalPathHolder {}
	/// A subset of path holders that hold no value.
	sealed interface Missing extends PortalPathHolder {}

	/// A present path.
	record Present(PortalPath path) implements Known {}
	/// A path that is missing because no portals were passed through.
	enum Empty implements Missing, Known { INSTANCE }
	/// A path that is missing because one was never set.
	enum Unknown implements Missing { INSTANCE }
}
