package io.github.fusionflux.portalcubed.content.boots;

import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantments;

public class LongFallBoots {
	public static final int BASE_BLOCKS_PER_POINT = 4;
	public static final int DAMAGE_INTERVAL_SIZE = 50;

	public static int calculateDamage(RegistryAccess registryAccess, ItemStack stack, double absorption, int fallDist) {
		int unbreakingLevel = registryAccess
				.lookup(Registries.ENCHANTMENT)
				.flatMap(registry -> registry.get(Enchantments.UNBREAKING))
				.map(stack.getEnchantments()::getLevel)
				.orElse(0);
		int blocksPerPoint = BASE_BLOCKS_PER_POINT + unbreakingLevel;
		double bootDamage = 0;

		// loop through intervals
		while (fallDist > 0) {
			double pointsPerBlock = 1d / blocksPerPoint;
			// distance fallen this interval, maxed at interval size
			int dist = Math.min(DAMAGE_INTERVAL_SIZE, fallDist);
			fallDist -= dist;
			bootDamage += pointsPerBlock * dist;

			// every interval deals more damage per block
			if (blocksPerPoint > 1)
				blocksPerPoint--;
		}

		return Mth.floor(bootDamage * absorption);
	}
}
