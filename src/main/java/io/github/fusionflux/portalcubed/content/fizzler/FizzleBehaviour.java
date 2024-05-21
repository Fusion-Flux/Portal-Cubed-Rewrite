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
			boolean wasSilent = entity.isSilent(); // disintegration makes entities silent
			if (!wasSilent && entity.pc$disintegrate()) {
				// Entity.playSound does a isSilent check
				DisintegrationSoundType.allFor(entity.getType()).forEach(soundType ->
						entity.level().playSound(null, entity.getX(), entity.getY(), entity.getZ(), soundType.sound, entity.getSoundSource(), 1f, 1f));
			}
		}
	},
	PAINT_CLEARING {
		@Override
		public void fizzle(Entity entity) {

		}
	};

	public abstract void fizzle(Entity entity);
}
