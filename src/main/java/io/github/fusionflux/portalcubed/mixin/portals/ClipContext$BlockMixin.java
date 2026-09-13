package io.github.fusionflux.portalcubed.mixin.portals;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import io.github.fusionflux.portalcubed.content.portal.placement.PortalBumper;
import io.github.fusionflux.portalcubed.data.tags.PortalCubedBlockTags;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.shapes.Shapes;

@Mixin(ClipContext.Block.class)
public enum ClipContext$BlockMixin {
	PORTALCUBED_PORTAL_SHOT((state, level, pos, context) -> {
		if (state.is(PortalCubedBlockTags.NONSOLID_TO_PORTAL_SHOTS))
			return Shapes.empty();

		return PortalBumper.getPortalVisibleShape(state, level, pos, context);
	}),
	PORTALCUBED_NONE((_, _, _, _) -> Shapes.empty());

	@Shadow
	ClipContext$BlockMixin(ClipContext.ShapeGetter getShape) {}
}
