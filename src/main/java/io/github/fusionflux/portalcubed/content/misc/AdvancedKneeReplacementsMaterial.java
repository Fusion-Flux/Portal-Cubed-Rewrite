package io.github.fusionflux.portalcubed.content.misc;

import io.github.fusionflux.portalcubed.PortalCubed;
import io.github.fusionflux.portalcubed.content.PortalCubedItems;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.ArmorMaterials;
import net.minecraft.world.item.crafting.Ingredient;

import org.jetbrains.annotations.NotNull;
import org.quiltmc.loader.api.minecraft.ClientOnly;

public enum AdvancedKneeReplacementsMaterial implements ArmorMaterial {
	INSTANCE;

	public static final String NAME = "advanced_knee_replacements";
	public static final Ingredient REPAIR_INGREDIENT = Ingredient.of(PortalCubedItems.MAGNESIUM_INGOT);
	public static final ResourceLocation TEXTURE = PortalCubed.id("textures/models/armor/" + NAME);

	@Override
	public int getDurabilityForType(ArmorItem.Type slot) {
		return ArmorMaterials.IRON.getDurabilityForType(slot);
	}

	@Override
	public int getDefenseForType(ArmorItem.Type slot) {
		return 0;
	}

	@Override
	public int getEnchantmentValue() {
		return ArmorMaterials.IRON.getEnchantmentValue();
	}

	@Override
	@NotNull
	public SoundEvent getEquipSound() {
		return ArmorMaterials.IRON.getEquipSound();
	}

	@Override
	@NotNull
	public Ingredient getRepairIngredient() {
		return REPAIR_INGREDIENT;
	}

	@Override
	@NotNull
	public String getName() {
		return NAME;
	}

	@Override
	public float getToughness() {
		return 0;
	}

	@Override
	public float getKnockbackResistance() {
		return 0;
	}

	@Override
	@ClientOnly
	@NotNull
	public ResourceLocation getTexture() {
		return TEXTURE;
	}
}
