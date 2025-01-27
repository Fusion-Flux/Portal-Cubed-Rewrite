package io.github.fusionflux.portalcubed.content.portal.gun;

import com.mojang.serialization.Codec;

import io.github.fusionflux.portalcubed.content.portal.gun.SubmergedTheOperationalEndOfTheDeviceTrigger.TriggerInstance;
import net.minecraft.advancements.critereon.ContextAwarePredicate;
import net.minecraft.advancements.critereon.SimpleCriterionTrigger;
import net.minecraft.server.level.ServerPlayer;

import java.util.Optional;

public class SubmergedTheOperationalEndOfTheDeviceTrigger extends SimpleCriterionTrigger<TriggerInstance> {
	@Override
	public Codec<TriggerInstance> codec() {
		return TriggerInstance.CODEC;
	}

	public void trigger(ServerPlayer player) {
		super.trigger(player, instance -> true);
	}

	public enum TriggerInstance implements SimpleInstance {
		INSTANCE;

		public static final Codec<TriggerInstance> CODEC = Codec.unit(INSTANCE);

		@Override
		public Optional<ContextAwarePredicate> player() {
			return Optional.empty();
		}
	}
}
