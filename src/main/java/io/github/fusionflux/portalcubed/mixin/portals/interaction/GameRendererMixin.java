package io.github.fusionflux.portalcubed.mixin.portals.interaction;

import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;

import io.github.fusionflux.portalcubed.content.portal.clip.PortalHitResult;
import io.github.fusionflux.portalcubed.content.portal.interaction.OutlineWithIgnoreClipContextMode;
import io.github.fusionflux.portalcubed.content.portal.manager.lookup.PortalLookup;
import io.github.fusionflux.portalcubed.data.tags.PortalCubedBlockTags;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

@Mixin(GameRenderer.class)
public class GameRendererMixin {
	@Shadow
	@Final
	private Minecraft minecraft;

	@ModifyReturnValue(
			method = "pick(Lnet/minecraft/world/entity/Entity;DDF)Lnet/minecraft/world/phys/HitResult;",
			at = @At("RETURN")
	)
	private HitResult raycastThroughPortals(HitResult original,
											@Local(argsOnly = true) Entity entity,
											@Local(argsOnly = true, ordinal = 0) double blockReach,
											@Local(argsOnly = true, ordinal = 1) double entityReach,
											@Local(ordinal = 0) Vec3 eyePos) {

		if (original == null || this.minecraft.level == null)
			return original;

		double maxRange = Math.max(blockReach, entityReach);
		Vec3 direction = eyePos.vectorTo(original.getLocation()).normalize();
		Vec3 idealEnd = eyePos.add(direction.scale(maxRange));

		ClientLevel level = this.minecraft.level;
		PortalLookup lookup = level.portalManager().lookup();
		PortalHitResult result = lookup.clip(eyePos, idealEnd);
		if (result == null || isOriginalBetter(original, result, eyePos, level)) {
			return original;
		}

		double distanceCovered = eyePos.distanceTo(result.hit());

		while (true) {
			Vec3 start = result.exitHit();
			Vec3 end = switch (result) {
				case PortalHitResult.Mid mid -> mid.next().hit();
				case PortalHitResult.Tail tail -> tail.end();
			};

			double stepBlockReach = blockReach - distanceCovered;
			double stepEntityReach = entityReach - distanceCovered;
			distanceCovered += start.distanceTo(end);

			HitResult hit = pick(entity, start, end, stepBlockReach, stepEntityReach);
			if (hit != null)
				return hit;

			if (!(result instanceof PortalHitResult.Mid mid)) {
				// end reached, whole raycast missed
				Direction nearestFacing = Direction.getApproximateNearest(idealEnd);
				BlockPos blockPos = BlockPos.containing(idealEnd);
				return BlockHitResult.miss(idealEnd, nearestFacing, blockPos);
			}

			result = mid.next();
		}
	}

	@Unique
	private static boolean isOriginalBetter(HitResult original, PortalHitResult portalHit, Vec3 start, Level level) {
		boolean farther = portalHit.isFartherThan(original, start);
		boolean ignore = ignoreHitBlock(original, portalHit.hit(), level);
		return farther && !ignore;
	}

	@Unique
	private static boolean ignoreHitBlock(HitResult hit, Vec3 portalHitPos, Level level) {
		if (!(hit instanceof BlockHitResult blockHit) || hit.getType() == HitResult.Type.MISS)
			return false;

		BlockState state = level.getBlockState(blockHit.getBlockPos());
		if (!state.is(PortalCubedBlockTags.PORTAL_INTERACTION_PASSTHROUGH))
			return false;

		// make sure this is the block that was actually hit
		return portalHitPos.distanceToSqr(hit.getLocation()) < 0.1;
	}

	@Unique
	@Nullable
	private static HitResult pick(Entity entity, Vec3 from, Vec3 to, double blockReach, double entityReach) {
		BlockHitResult blockHit = pickBlock(entity, from, to, blockReach);
		if (blockHit != null) {
			to = blockHit.getLocation();
		}

		EntityHitResult entityHit = pickEntity(entity, from, to, entityReach);

		if (blockHit == null && entityHit == null) {
			return null;
		} else if (blockHit == null) { // entityHit != null
			return entityHit;
		} else if (entityHit == null) { // blockHit != null
			return blockHit;
		} else {
			double blockDist = blockHit.getLocation().distanceToSqr(from);
			double entityDist = entityHit.getLocation().distanceToSqr(from);
			return blockDist < entityDist ? blockHit : entityHit;
		}
	}

	@Unique
	@Nullable
	private static BlockHitResult pickBlock(Entity entity, Vec3 from, Vec3 to, double reach) {
		if (reach <= 0) {
			return null;
		}

		Level level = entity.level();
		ClipContext context = new ClipContext(from, to, ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, entity);
		BlockHitResult result = level.clip(context);
		if (result.getType() == HitResult.Type.MISS)
			return null;

		if (!ignoreHitBlock(result, from, level))
			return result;

		ClipContext.Block blockMode = OutlineWithIgnoreClipContextMode.create(result.getBlockPos());
		ClipContext newContext = new ClipContext(from, to, blockMode, ClipContext.Fluid.NONE, entity);
		BlockHitResult newResult = level.clip(newContext);
		return newResult.getType() == HitResult.Type.MISS ? null : newResult;
	}

	@Unique
	@Nullable
	private static EntityHitResult pickEntity(Entity entity, Vec3 from, Vec3 to, double reach) {
		if (reach <= 0) {
			return null;
		}

		// step back very slightly, since we need to make sure the raycast starts outside of entity hitboxes.
		// entities right up against the portal could be missed otherwise.
		Vec3 actualStart = from.lerp(to, -1e-5);
		AABB searchArea = new AABB(actualStart, to);
		return ProjectileUtil.getEntityHitResult(entity.level(), null, actualStart, to, searchArea, EntitySelector.CAN_BE_PICKED, 0);
	}
}
