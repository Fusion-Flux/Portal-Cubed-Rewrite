package io.github.fusionflux.portalcubed.content.misc;

import net.minecraft.util.Mth;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.DyeableArmorItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;

// This exists purely because the default color for dyed items is brown.
public class LongFallBoots extends DyeableArmorItem {
	public LongFallBoots(ArmorMaterial armorMaterial, Type type, Properties properties) {
		super(armorMaterial, type, properties);
	}

	public static final int BASE_BLOCKS_PER_POINT = 4;
	public static final int DAMAGE_INTERVAL_SIZE = 50;

	// Logic in LivingEntityMixin
	public static int calculateFallDamage(ItemStack stack, int fallDist) {
		int blocksPerPoint = BASE_BLOCKS_PER_POINT + EnchantmentHelper.getItemEnchantmentLevel(Enchantments.UNBREAKING, stack);
		float bootDamage = 0;

		// loop through intervals
		while (fallDist > 0) {
			float pointsPerBlock = 1f / blocksPerPoint;
			// distance fallen this interval, maxed at interval size
			int dist = Math.min(DAMAGE_INTERVAL_SIZE, fallDist);
			fallDist -= dist;
			bootDamage += pointsPerBlock * dist;

			// every interval deals more damage per block
			if (blocksPerPoint > 1)
				blocksPerPoint--;
		}

		return Mth.floor(bootDamage);
	}

	@Override
	public int getColor(ItemStack stack) {
		int color = super.getColor(stack);
		return color == DyeableArmorItem.DEFAULT_LEATHER_COLOR ? 0xFFFFFFFF : color;
	}
}
