package io.github.fusionflux.portalcubed.mixin.portals;

import org.joml.Quaternionf;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;

import io.github.fusionflux.portalcubed.content.portal.clip.PortalHitResult;
import io.github.fusionflux.portalcubed.content.portal.manager.PortalManager;
import io.github.fusionflux.portalcubed.content.portal.ref.PortalPath;
import io.github.fusionflux.portalcubed.content.portal.ref.PortalPathHolder;
import io.github.fusionflux.portalcubed.content.portal.transform.PortalTransform;
import io.github.fusionflux.portalcubed.framework.extension.LevelExt;
import io.github.fusionflux.portalcubed.framework.raycast.RaycastOptions;
import io.github.fusionflux.portalcubed.framework.raycast.RaycastResult;
import net.minecraft.client.Camera;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

@Mixin(Camera.class)
public abstract class CameraMixin {
	@Shadow
	private BlockGetter level;

	@Shadow
	public abstract Vec3 getPosition();

	@Shadow
	@Final
	private Quaternionf rotation;

	@WrapOperation(
			method = "getMaxZoom",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/world/level/BlockGetter;clip(Lnet/minecraft/world/level/ClipContext;)Lnet/minecraft/world/phys/BlockHitResult;"
			)
	)
	private BlockHitResult raycastThroughPortals(BlockGetter blockGetter, ClipContext context, Operation<BlockHitResult> original) {
		BlockHitResult originalResult = original.call(blockGetter, context);

		if (!(blockGetter instanceof Level level))
			return originalResult;

		RaycastResult result = RaycastOptions.of(context).build().raycast(level, context.getFrom(), context.getTo());
		if (!(result instanceof RaycastResult.BlockLike blockLike)) {
			throw new IllegalStateException("Unexpected result: " + result);
		}

		return blockLike.passedThroughPortals() ? blockLike.toVanilla() : originalResult;
	}

	@WrapOperation(
			method = "getMaxZoom",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/world/phys/HitResult;getLocation()Lnet/minecraft/world/phys/Vec3;"
			)
	)
	private Vec3 unTeleportLocation(HitResult result, Operation<Vec3> original) {
		Vec3 pos = original.call(result);

		if (!(result.portalPath() instanceof PortalPathHolder.Present(PortalPath path)))
			return pos;

		return path.transform().inverse().applyAbsolute(pos);
	}

	@WrapOperation(
			method = "move",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/client/Camera;setPosition(Lnet/minecraft/world/phys/Vec3;)V"
			)
	)
	private void moveThroughPortals(Camera self, Vec3 target, Operation<Void> original) {
		if (this.level instanceof LevelExt level) {
			PortalManager manager = level.portalManager();
			PortalHitResult result = manager.lookup().clip(this.getPosition(), target);
			if (result.path().isPresent()) {
				PortalPath path = result.path().get();
				PortalTransform transform = path.transform();

				Vec3 transformedTarget = transform.applyAbsolute(target);
				original.call(self, transformedTarget);
				transform.apply(this.rotation);

				return;
			}
		}

		original.call(self, target);
	}
}
