package io.github.fusionflux.portalcubed.content.portal.sync;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.core.Rotations;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

public record EntityState(Vec3 pos, Rotations rotations) {
	public EntityState lerp(EntityState to, double delta) {
		return new EntityState(
				this.pos.lerp(to.pos, delta),
				new Rotations(
						(float) Mth.rotLerp(delta, this.rotations.getX(), to.rotations.getX()),
						(float) Mth.rotLerp(delta, this.rotations.getY(), to.rotations.getY()),
						(float) Mth.rotLerp(delta, this.rotations.getZ(), to.rotations.getZ())
				)
		);
	}

	@Environment(EnvType.CLIENT)
	public void apply(EntityRenderState state) {
		state.x = this.pos.x;
		state.y = this.pos.y;
		state.z = this.pos.z;
	}

	public static EntityState capture(Entity entity) {
		return new EntityState(entity.position(), new Rotations(entity.getXRot(), entity.getYRot(), 0));
	}

	public static EntityState captureOld(Entity entity) {
		return new EntityState(entity.oldPosition(), new Rotations(entity.xRotO, entity.yRotO, 0));
	}
}
