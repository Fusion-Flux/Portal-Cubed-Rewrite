package io.github.fusionflux.portalcubed.mixin.props;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import io.github.fusionflux.portalcubed.data.tags.PortalCubedBlockTags;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.shapes.Shapes;

@Mixin(ClipContext.Block.class)
public enum ClipContext$BlockMixin {
	PORTALCUBED_GRAB((state, level, pos, context) -> {
		if (state.is(PortalCubedBlockTags.CAN_BE_GRABBED_THROUGH))
			return Shapes.empty();

		// we fall back to collision shapes instead of outline shapes because we use this to determine how far away a prop should be held.
		// if we used outline instead, we would have some weirdness with some blocks, like walls.
		return ClipContext.Block.COLLIDER.get(state, level, pos, context);
	});

	@Shadow
    ClipContext$BlockMixin(ClipContext.ShapeGetter getShape) {}
}
