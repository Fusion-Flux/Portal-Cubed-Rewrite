package io.github.fusionflux.portalcubed.content.portal;

import net.minecraft.core.Rotations;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.phys.Vec3;

public record TeleportStep(Vec3 from, Vec3 to, Vec3 vel, Rotations rotations) {
	public Vec3 pos(float progress) {
		return this.from.lerp(this.to, progress);
	}

	public void toNetwork(FriendlyByteBuf buf) {
		buf.writeVec3(this.from);
		buf.writeVec3(this.to);
		buf.writeVec3(this.vel);
		buf.writeFloat(this.rotations.getX());
		buf.writeFloat(this.rotations.getY());
		buf.writeFloat(this.rotations.getZ());
	}

	public static TeleportStep fromNetwork(FriendlyByteBuf buf) {
		return new TeleportStep(
				buf.readVec3(), buf.readVec3(), buf.readVec3(),
				new Rotations(
						buf.readFloat(), buf.readFloat(), buf.readFloat()
				)
		);
	}
}
