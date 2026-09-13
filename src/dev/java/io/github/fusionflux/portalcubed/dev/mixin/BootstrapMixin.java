package io.github.fusionflux.portalcubed.dev.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import net.minecraft.server.Bootstrap;

@Mixin(Bootstrap.class)
public class BootstrapMixin {
	@Redirect(
			method = "validate",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/commands/Commands;validate()V"
			)
	)
	private static void dontValidate() {
		// command validation has a number of issues in a modded context, let's just skip that.
		// - mod init hasn't run yet, all modded argument types will count as unregistered
		// - only gets vanilla dynamic registry context
		// possibly more, that's where I gave up.
	}
}
