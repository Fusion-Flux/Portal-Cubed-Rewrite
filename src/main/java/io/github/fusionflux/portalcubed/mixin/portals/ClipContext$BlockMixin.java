package io.github.fusionflux.portalcubed.mixin.portals;

import java.util.Arrays;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.gen.Invoker;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import io.github.fusionflux.portalcubed.content.portal.placement.PortalShotClipContextMode;
import net.minecraft.world.level.ClipContext;

@Mixin(ClipContext.Block.class)
public class ClipContext$BlockMixin {
	@Shadow
	@Final
	@Mutable
	private static ClipContext.Block[] $VALUES;

	@Invoker("<init>")
	private static ClipContext.Block pc$create(String name, int ordinal, ClipContext.ShapeGetter shapeGetter) {
		throw new AbstractMethodError();
	}

	@Inject(method = "<clinit>", at = @At("TAIL"))
	private static void addPortalShot(CallbackInfo ci) {
		int ordinal = $VALUES.length;
		ClipContext.Block type = pc$create(PortalShotClipContextMode.NAME, ordinal, PortalShotClipContextMode::getPortalShotVisibleShape);
		ClipContext.Block[] newArray = Arrays.copyOf($VALUES, $VALUES.length + 1);
		newArray[$VALUES.length] = type;
		$VALUES = newArray;
	}
}
