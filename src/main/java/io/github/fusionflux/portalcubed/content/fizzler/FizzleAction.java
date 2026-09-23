package io.github.fusionflux.portalcubed.content.fizzler;

import java.util.Locale;

import io.github.fusionflux.portalcubed.data.tags.PortalCubedEntityTags;
import net.minecraft.world.entity.Entity;

/// An action that can be performed by fizzlers.
public enum FizzleAction {
	CLEAR_PORTALS {
		@Override
		public boolean apply(Entity entity) {
			return false;
		}
	},
	DISINTEGRATE {
		@Override
		public boolean apply(Entity entity) {
			if (entity.is(PortalCubedEntityTags.IMMUNE_TO_DISINTEGRATION))
				return false;

			return Disintegration.disintegrate(entity);
		}
	},
	CLEAR_PAINT {
		@Override
		public boolean apply(Entity entity) {
			return false;
		}
	};

	public final String name = this.name().toLowerCase(Locale.ROOT);

	/// Apply this action to the given entity. Only called on the server-side.
	public abstract boolean apply(Entity entity);
}
