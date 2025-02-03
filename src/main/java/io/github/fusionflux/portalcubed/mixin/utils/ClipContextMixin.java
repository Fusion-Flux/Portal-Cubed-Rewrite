package io.github.fusionflux.portalcubed.mixin.utils;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import io.github.fusionflux.portalcubed.framework.extension.ClipContextExt;
import net.minecraft.world.level.ClipContext;

@Mixin(ClipContext.class)
public class ClipContextMixin implements ClipContextExt {
	@Unique
	private boolean ignoreInteractionOverride;

	@Override
	public void pc$setIgnoreInteractionOverride(boolean ignore) {
		this.ignoreInteractionOverride = ignore;
	}

	@Override
	public boolean pc$ignoreInteractionOverride() {
		return this.ignoreInteractionOverride;
	}
}
