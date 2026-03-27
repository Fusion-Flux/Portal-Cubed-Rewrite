package io.github.fusionflux.portalcubed.mixin.portals;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;

import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.Slice;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;

import io.github.fusionflux.portalcubed.content.portal.Portal;
import io.github.fusionflux.portalcubed.content.portal.manager.PortalManager;
import io.github.fusionflux.portalcubed.content.portal.ref.PortalReference;
import io.github.fusionflux.portalcubed.content.portal.transform.SinglePortalTransform;
import io.github.fusionflux.portalcubed.framework.extension.LevelExt;
import io.github.fusionflux.portalcubed.framework.shape.OBB;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.EntityGetter;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

@Mixin(EntityGetter.class)
public interface EntityGetterMixin {
	@Shadow
	List<Entity> getEntities(@Nullable Entity entity, AABB area, Predicate<? super Entity> predicate);

	@ModifyArg(
			method = "getEntityCollisions",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/world/level/EntityGetter;getEntities(Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/phys/AABB;Ljava/util/function/Predicate;)Ljava/util/List;"
			)
	)
	private Predicate<Entity> carveEntities(Predicate<Entity> predicate, @Local(argsOnly = true) @Nullable Entity entity) {
		if (entity == null) {
			return predicate;
		}

		Set<PortalReference> portals = entity.relevantPortals().get();
		if (portals.isEmpty())
			return predicate;

		return predicate.and(otherEntity -> {
			AABB box = otherEntity.getBoundingBox();

			for (PortalReference portal : portals) {
				if (portal.get().hides(box)) {
					return false;
				}
			}

			return true;
		});
	}

	@ModifyReturnValue(
			method = "getEntityCollisions",
			at = @At("RETURN"),
			// skip the first early return after an epsilon check
			slice = @Slice(from = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/world/level/EntityGetter;getEntities(Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/phys/AABB;Ljava/util/function/Predicate;)Ljava/util/List;"
			))
	)
	private List<VoxelShape> addProxyHitboxes(List<VoxelShape> original, @Nullable Entity except, AABB area, @Local Predicate<Entity> filter) {
		if (!(this instanceof LevelExt level))
			return original;

		PortalManager manager = level.portalManager();
		// we need to check a bit larger of an area, since portals outside the area can provide hitboxes that extend into it
		// technically any non-infinite area is incorrect, but 1 more block should be good enough basically everywhere
		AABB portalArea = area.inflate(1);
		Set<PortalReference> portals = manager.lookup().getPortals(portalArea);
		if (portals.isEmpty())
			return original;

		// match vanilla entity search area
		area = area.inflate(1e-7);

		List<VoxelShape> additionalShapes = new ArrayList<>();
		for (PortalReference portal : portals) {
			Optional<PortalReference> maybeOpposite = portal.opposite();
			if (maybeOpposite.isEmpty())
				continue;

			PortalReference linked = maybeOpposite.get();
			SinglePortalTransform transform = new SinglePortalTransform(portal.get(), linked.get());
			OBB transformedArea = transform.apply(area);

			// find all entities intersecting the portal
			List<Entity> entities = this.getIntersectingEntities(except, transformedArea, linked.get(), filter);
			if (entities.isEmpty())
				continue;

			for (Entity entity : entities) {
				// imprecise, but hopefully good enough
				AABB transformedBounds = transform.inverse().apply(entity.getBoundingBox()).encompassingAabb;
				additionalShapes.add(Shapes.create(transformedBounds));
			}
		}

		if (additionalShapes.isEmpty()) {
			return original;
		} else if (original.isEmpty()) {
			return additionalShapes;
		}

		List<VoxelShape> merged = new ArrayList<>(original);
		merged.addAll(additionalShapes);
		return merged;
	}

	@Unique
	private List<Entity> getIntersectingEntities(@Nullable Entity except, OBB area, Portal portal, Predicate<Entity> filter) {
		List<Entity> entities = this.getEntities(except, area.encompassingAabb, filter);
		if (entities.isEmpty())
			return entities;

		List<Entity> intersecting = new ArrayList<>();

		for (Entity entity : entities) {
			AABB bounds = entity.getBoundingBox();
			if (area.intersects(bounds) && portal.quad.intersects(bounds)) {
				intersecting.add(entity);
			}
		}

		return intersecting;
	}
}
