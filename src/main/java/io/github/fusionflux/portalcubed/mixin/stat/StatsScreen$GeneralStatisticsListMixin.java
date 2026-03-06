package io.github.fusionflux.portalcubed.mixin.stat;

import java.util.Iterator;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

import com.google.common.collect.Iterators;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;

import io.github.fusionflux.portalcubed.content.PortalCubedStats;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.stats.Stat;
import net.minecraft.util.RandomSource;

@Mixin(targets = "net.minecraft.client.gui.screens.achievement.StatsScreen$GeneralStatisticsList")
public class StatsScreen$GeneralStatisticsListMixin {
	@Unique
	private static final RandomSource random = RandomSource.createNewThreadLocalInstance();

	@ModifyExpressionValue(
			method = "<init>",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/stats/StatType;iterator()Ljava/util/Iterator;"
			)
	)
	private Iterator<Stat<ResourceLocation>> maybeSkipPoints(Iterator<Stat<ResourceLocation>> original) {
		return Iterators.filter(original, stat -> {
			if (!stat.getValue().equals(PortalCubedStats.OPPORTUNITY_ADVISEMENT_POINTS))
				return true;

			// only a 10% chance to show Opportunity Advisement Points
			return random.nextFloat() < 0.1;
		});
	}
}
