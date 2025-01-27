package io.github.fusionflux.portalcubed.content.fizzler;

import java.util.Locale;

import io.github.fusionflux.portalcubed.data.tags.PortalCubedEntityTags;
import net.minecraft.world.entity.Entity;

public enum FizzleBehaviour {
	PORTAL_CLEARING {
		@Override
		public boolean fizzle(Entity entity) {
			return false;
		}
	},
	DISINTEGRATION {
		@Override
		public boolean fizzle(Entity entity) {
			if (entity.getType().is(PortalCubedEntityTags.IMMUNE_TO_DISINTEGRATION))
				return false;
			return entity.pc$disintegrate();
		}
	},
	PAINT_CLEARING {
		@Override
		public boolean fizzle(Entity entity) {
			return false;
		}
	};

	public final String name = this.name().toLowerCase(Locale.ROOT);

	public abstract boolean fizzle(Entity entity);
}
