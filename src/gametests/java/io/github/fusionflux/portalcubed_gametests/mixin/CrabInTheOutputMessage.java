package io.github.fusionflux.portalcubed_gametests.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;

import net.minecraft.gametest.framework.TestCommand;

/*
 * crab in the code is on break, so here to take his place is crab in the output message. we don't know when crab in the code will be back,
 * he probably went out for lunch at this point, and his favorite restaurant is pretty far away. we'll update you when he returns,
 * but for now we hope you enjoy the company of crab in the output message.
 */
@Mixin(TestCommand.TestSummaryDisplayer.class)
public class CrabInTheOutputMessage {
	@ModifyExpressionValue(
			method = "showTestSummaryIfAllDone",
			at = @At(
					value = "CONSTANT",
					args = "stringValue=All required tests passed :)"
			)
	)
	private static String crab(String original) {
		return "All required tests passed \uD83E\uDD80";
	}
}
