package io.github.fusionflux.portalcubed.content.boots;

import org.jetbrains.annotations.NotNull;

import io.github.fusionflux.portalcubed.PortalCubed;
import io.github.fusionflux.portalcubed.content.PortalCubedItems;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.ArmorMaterials;
import net.minecraft.world.item.crafting.Ingredient;

public enum LongFallBootsMaterial implements ArmorMaterial {
	INSTANCE;

	public static final String NAME = "long_fall_boots";
	public static final Ingredient REPAIR_INGREDIENT = Ingredient.of(PortalCubedItems.MAGNESIUM_INGOT);
	public static final ResourceLocation TEXTURE = PortalCubed.id("textures/models/armor/" + NAME);

	@Override
	public int getDurabilityForType(ArmorItem.Type slot) {
		return ArmorMaterials.IRON.getDurabilityForType(slot);
	}

	@Override
	public int getDefenseForType(ArmorItem.Type slot) {
		return ArmorMaterials.IRON.getDefenseForType(slot);
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
		return ArmorMaterials.IRON.getToughness();
	}

	@Override
	public float getKnockbackResistance() {
		return ArmorMaterials.IRON.getKnockbackResistance();
	}

	@Override
	@Environment(EnvType.CLIENT)
	@NotNull
	public ResourceLocation getTexture() {
		return TEXTURE;
	}
}
