package io.github.fusionflux.portalcubed.mixin;

import io.github.fusionflux.portalcubed.content.PortalCubedItems;
import net.fabricmc.fabric.api.recipe.v1.ingredient.DefaultCustomIngredients;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.SmithingTrimRecipe;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import java.util.Optional;

@Mixin(SmithingTrimRecipe.class)
public class SmithingTrimRecipeMixin {
	@Unique
	private static final Ingredient NON_TRIMMABLE_ARMOR = Ingredient.of(PortalCubedItems.ADVANCED_KNEE_REPLACEMENTS);

	@ModifyVariable(method = "<init>", at = @At("CTOR_HEAD"), argsOnly = true, ordinal = 1)
	private Optional<Ingredient> dontTrimKneeReplacements(Optional<Ingredient> base) {
		return base.map(ingredient -> DefaultCustomIngredients.difference(ingredient, NON_TRIMMABLE_ARMOR));
	}
}
