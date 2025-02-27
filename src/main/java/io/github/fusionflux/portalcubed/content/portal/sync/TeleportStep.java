package io.github.fusionflux.portalcubed.content.portal.sync;

public record TeleportStep(float weight, EntityState start, EntityState end) {
	public EntityState getState(float partialTick) {
		return this.start.lerp(this.end, partialTick);
	}
}
