package io.github.fusionflux.portalcubed.mixin.stat;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import io.github.fusionflux.portalcubed.content.PortalCubedStats;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.ServerStatsCounter;
import net.minecraft.stats.Stat;
import net.minecraft.stats.Stats;
import net.minecraft.stats.StatsCounter;

@Mixin(ServerStatsCounter.class)
public abstract class ServerStatsCounterMixin extends StatsCounter {
	@Inject(method = "sendStats", at = @At("HEAD"))
	private void beforeSendingStats(ServerPlayer player, CallbackInfo ci) {
		this.randomizePoints(PortalCubedStats.SCIENCE_COLLABORATION_POINTS, player);
		this.randomizePoints(PortalCubedStats.OPPORTUNITY_ADVISEMENT_POINTS, player);
	}

	@Unique
	private void randomizePoints(ResourceLocation id, ServerPlayer player) {
		Stat<ResourceLocation> stat = Stats.CUSTOM.get(id);
		int current = this.getValue(stat);
		int change = player.getRandom().nextIntBetweenInclusive(-75, 75);
		this.setValue(player, stat, current + change);
	}
}
