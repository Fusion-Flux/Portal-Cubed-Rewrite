package io.github.fusionflux.portalcubed.content.portal.sync;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

public record EntityState(Vec3 pos, float pitch, float yaw) {
	public static final StreamCodec<ByteBuf, EntityState> CODEC = StreamCodec.composite(
			Vec3.STREAM_CODEC, EntityState::pos,
			ByteBufCodecs.FLOAT, EntityState::pitch,
			ByteBufCodecs.FLOAT, EntityState::yaw,
			EntityState::new
	);

	public void apply(Entity entity) {
		entity.moveTo(this.pos, this.yaw, this.pitch);
	}

	public static EntityState capture(Entity entity) {
		return new EntityState(entity.position(), entity.getXRot(), entity.getYRot());
	}
}
