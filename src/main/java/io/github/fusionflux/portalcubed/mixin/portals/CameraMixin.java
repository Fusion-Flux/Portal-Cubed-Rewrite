package io.github.fusionflux.portalcubed.mixin.portals;

import java.util.Optional;

import org.joml.Quaternionf;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;

import io.github.fusionflux.portalcubed.content.portal.clip.PortalHitResult;
import io.github.fusionflux.portalcubed.content.portal.manager.PortalManager;
import io.github.fusionflux.portalcubed.content.portal.ref.PortalPath;
import io.github.fusionflux.portalcubed.content.portal.ref.PortalPathHolder;
import io.github.fusionflux.portalcubed.content.portal.transform.PortalTransform;
import io.github.fusionflux.portalcubed.framework.extension.LevelExt;
import io.github.fusionflux.portalcubed.framework.raycast.RaycastOptions;
import io.github.fusionflux.portalcubed.framework.raycast.RaycastResult;
import net.minecraft.client.Camera;
import net.minecraft.util.Mth;
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

	@Shadow
	private float eyeHeight;

	@Shadow
	private float eyeHeightOld;

	@WrapOperation(
			method = "setup",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/world/phys/Vec3;add(Lnet/minecraft/world/phys/Vec3;)Lnet/minecraft/world/phys/Vec3;"
			)
	)
	private Vec3 teleportWhenPositioningInNewMinecart(Vec3 pos, Vec3 offset, Operation<Vec3> original) {
		Vec3 target = original.call(pos, offset);
		return this.teleportAndRotate(pos, target);
	}

	@WrapOperation(
			method = "setup",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/client/Camera;setPosition(DDD)V"
			)
	)
	private void teleportWhenPositioningNormally(Camera self, double x, double y, double z, Operation<Void> original, @Local(argsOnly = true) float partialTick) {
		Vec3 target = new Vec3(x, y, z);
		// I don't like recalculating this, but I think it's the best way
		Vec3 base = target.subtract(0, Mth.lerp(partialTick, this.eyeHeightOld, this.eyeHeight), 0);
		Vec3 newTarget = this.teleportAndRotate(base, target);
		original.call(self, newTarget.x, newTarget.y, newTarget.z);
	}

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

		// Minecraft determines the maximum zoom level by effectively moving a small box surrounding the camera as far back
		// as possible without hitting any blocks. this is done by performing 8 raycasts, one for each corner of the box.
		//
		// if this box is partially inside a portal, with the corners split across the sides,
		// some corners of the box might be inside the wall the portal is sitting on. if so,
		// we need to teleport the start and end positions to the other side of the portal.

		Vec3 originalStart = context.getFrom();
		Vec3 originalEnd = context.getTo();

		Optional<PortalTransform> transform = level.portalManager().lookup()
				.clip(this.getPosition(), originalStart).path()
				.map(PortalPath::transform);

		Vec3 start = transform.isEmpty() ? originalStart : transform.get().applyAbsolute(originalStart);
		Vec3 end = transform.isEmpty() ? originalEnd : transform.get().applyAbsolute(originalEnd);

		RaycastResult result = RaycastOptions.of(context).build().raycast(level, start, end);
		if (!(result instanceof RaycastResult.BlockLike blockLike)) {
			throw new IllegalStateException("Unexpected result: " + result);
		}

		return blockLike.passedThroughPortals() || transform.isPresent() ? blockLike.toVanilla() : originalResult;
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
		original.call(self, this.teleportAndRotate(this.getPosition(), target));
	}

	@Unique
	private Vec3 teleportAndRotate(Vec3 base, Vec3 target) {
		if (!(this.level instanceof LevelExt level))
			return target;

		PortalManager manager = level.portalManager();
		PortalHitResult result = manager.lookup().clip(base, target);
		if (result.path().isEmpty())
			return target;

		PortalPath path = result.path().get();
		PortalTransform transform = path.transform();

		transform.apply(this.rotation);
		return transform.applyAbsolute(target);
	}
}
