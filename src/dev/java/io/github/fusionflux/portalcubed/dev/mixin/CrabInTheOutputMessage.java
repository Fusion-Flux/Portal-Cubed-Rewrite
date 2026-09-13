package io.github.fusionflux.portalcubed.dev.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;

import net.minecraft.gametest.framework.TestCommand;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;

/// crab in the code is on break, so here to take his place is crab in the output message. we don't know when crab in the code will be back,
/// he probably went out for lunch at this point, and his favorite restaurant is pretty far away. we'll update you when he returns,
/// but for now we hope you enjoy the company of crab in the output message.
@Mixin(TestCommand.TestSummaryDisplayer.class)
public class CrabInTheOutputMessage {
	@ModifyReturnValue(method = "lambda$showTestSummaryIfAllDone$1", at = @At("RETURN"))
	private static Component crab(Component original) {
		Style style = original.getStyle();
		return Component.literal("All required tests passed \uD83E\uDD80").withStyle(style);
	}
}
