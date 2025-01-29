package io.github.fusionflux.portalcubed_gametests.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.expression.Definition;
import com.llamalad7.mixinextras.expression.Expression;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.serialization.Lifecycle;

import net.minecraft.client.gui.screens.worldselection.WorldOpenFlows;

@Mixin(WorldOpenFlows.class)
public class WorldOpenFlowsMixin {
	@Definition(id = "lifecycle", local = @Local(type = Lifecycle.class, argsOnly = true))
	@Definition(id = "stable", method = "Lcom/mojang/serialization/Lifecycle;stable()Lcom/mojang/serialization/Lifecycle;")
	@Expression("lifecycle == stable()")
	@ModifyExpressionValue(method = "confirmWorldCreation", at = @At("MIXINEXTRAS:EXPRESSION"))
	private static boolean hereBeNoDragons(boolean original) {
		return true;
	}
}
