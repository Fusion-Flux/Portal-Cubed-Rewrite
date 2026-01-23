package io.github.fusionflux.portalcubed.mixin.portals;

import java.util.ArrayList;
import java.util.List;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;

import io.github.fusionflux.portalcubed.content.portal.placement.PortalShotClipContextMode;
import io.github.fusionflux.portalcubed.mixin.utils.accessors.ClipContext$BlockAccessor;
import net.minecraft.world.level.ClipContext;

@Mixin(ClipContext.Block.class)
public class ClipContext$BlockMixin {
	@ModifyReturnValue(method = "$values", at = @At("RETURN"))
	private static ClipContext.Block[] addPortalShot(ClipContext.Block[] original) {
		List<ClipContext.Block> list = new ArrayList<>(List.of(original));
		list.add(ClipContext$BlockAccessor.pc$create(
				PortalShotClipContextMode.NAME, list.size(), PortalShotClipContextMode::getPortalShotVisibleShape
		));
		return list.toArray(ClipContext.Block[]::new);
	}
}
