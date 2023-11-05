package io.github.fusionflux.portalcubed.mixin;

import io.github.fusionflux.portalcubed.content.portal.PortalPickResult;
import io.github.fusionflux.portalcubed.content.portal.manager.PortalManager;
import io.github.fusionflux.portalcubed.data.tags.PortalCubedEntityTags;
import net.minecraft.world.entity.Entity;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

@Mixin(Entity.class)
public abstract class EntityMixin {
	@Shadow
	public abstract Vec3 position();

	@Shadow
	public abstract Level level();

	@Shadow
	public abstract EntityType<?> getType();

	@ModifyArgs(
			method = "move",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/world/entity/Entity;setPos(DDD)V"
			)
	)
	private void moveThroughPortals(Args args) {
		if (this.getType().is(PortalCubedEntityTags.PORTAL_BLACKLIST))
			return;

		Vec3 oldPos = position();
		Vec3 newPos = new Vec3(args.get(0), args.get(1), args.get(2));
		PortalManager manager = PortalManager.of(level());
		PortalPickResult result = manager.pickPortal(oldPos, newPos);
		if (result != null) {
			Vec3 teleported = result.teleportedEnd();
			args.set(0, teleported.x);
			args.set(1, teleported.y);
			args.set(2, teleported.z);
			System.out.println("entity teleported");
		}
	}
}
