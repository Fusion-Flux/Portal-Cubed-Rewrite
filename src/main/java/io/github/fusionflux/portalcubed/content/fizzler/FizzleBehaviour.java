package io.github.fusionflux.portalcubed.content.fizzler;

import io.github.fusionflux.portalcubed.data.tags.PortalCubedEntityTags;
import net.minecraft.world.entity.Entity;

public enum FizzleBehaviour {
	PORTAL_CLEARING {
		@Override
		public void fizzle(Entity entity) {

		}
	},
	DISINTEGRATION {
		@Override
		public void fizzle(Entity entity) {
			if (entity.getType().is(PortalCubedEntityTags.IMMUNE_TO_DISINTEGRATION)) return;
			if (entity.pc$disintegrate() && !entity.isSilent())
				DisintegrationSoundType.allFor(entity.getType()).forEach(soundType -> entity.playSound(soundType.sound));
		}
	},
	PAINT_CLEARING {
		@Override
		public void fizzle(Entity entity) {

		}
	};

	public abstract void fizzle(Entity entity);
}
