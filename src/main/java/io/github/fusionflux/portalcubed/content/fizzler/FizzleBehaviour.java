package io.github.fusionflux.portalcubed.content.fizzler;

import io.github.fusionflux.portalcubed.data.tags.PortalCubedEntityTags;
import net.minecraft.world.entity.Entity;

import java.util.Locale;

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
			if (entity.getType().is(PortalCubedEntityTags.IMMUNE_TO_DISINTEGRATION)) return false;
			if (!entity.isSilent()) { // disintegration makes entities silent
				// Entity.playSound does a isSilent check
				DisintegrationSoundType.allFor(entity.getType()).forEach(soundType ->
						entity.level().playSound(null, entity.getX(), entity.getY(), entity.getZ(), soundType.sound, entity.getSoundSource(), 1f, 1f));
			}
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
