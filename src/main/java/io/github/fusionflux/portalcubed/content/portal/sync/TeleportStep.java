package io.github.fusionflux.portalcubed.content.portal.sync;

public record TeleportStep(float untilPartialTicks, EntityState start, EntityState end) {
	public EntityState lerp(float partialTicks) {
		return this.start.lerp(this.end, partialTicks);
	}
}
