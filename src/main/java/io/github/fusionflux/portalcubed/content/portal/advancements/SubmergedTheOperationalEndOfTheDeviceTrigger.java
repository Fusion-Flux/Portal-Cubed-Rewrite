package io.github.fusionflux.portalcubed.content.portal.advancements;

import java.util.Optional;

import com.mojang.serialization.Codec;

import com.mojang.serialization.MapCodec;

import io.github.fusionflux.portalcubed.content.portal.advancements.SubmergedTheOperationalEndOfTheDeviceTrigger.TriggerInstance;
import net.minecraft.advancements.triggers.SimpleCriterionTrigger;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

public class SubmergedTheOperationalEndOfTheDeviceTrigger extends SimpleCriterionTrigger<TriggerInstance> {
	@Override
	public Codec<TriggerInstance> codec() {
		return TriggerInstance.CODEC;
	}

	public void trigger(ServerPlayer player) {
		super.trigger(player, _ -> true);
	}

	public enum TriggerInstance implements SimpleInstance {
		INSTANCE;

		public static final Codec<TriggerInstance> CODEC = MapCodec.unitCodec(INSTANCE);

		@Override
		public Optional<Holder<LootItemCondition>> player() {
			return Optional.empty();
		}
	}
}
