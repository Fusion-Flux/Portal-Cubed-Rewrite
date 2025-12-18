package io.github.fusionflux.portalcubed.content.portal.command.argument.placement;

import org.joml.Quaternionf;

import io.github.fusionflux.portalcubed.content.portal.placement.validator.PortalValidator;
import net.minecraft.world.phys.Vec3;

public record Placement(Vec3 pos, Quaternionf rotation, PortalValidator validator) {
	@Override
	public String toString() {
		return "x=" + this.pos.x + ", y=" + this.pos.y + ", z=" + this.pos.z + ", rot=" + this.rotation;
	}
}
