package io.github.fusionflux.portalcubed.content.portal.advancements;

import java.util.Optional;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import io.github.fusionflux.portalcubed.content.portal.ref.PortalReference;
import net.minecraft.advancements.triggers.SimpleCriterionTrigger;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

public class PortalTrigger extends SimpleCriterionTrigger<PortalTrigger.TriggerInstance> {
	@Override
	public Codec<TriggerInstance> codec() {
		return TriggerInstance.CODEC;
	}

	public void trigger(ServerPlayer player, PortalReference portal) {
		this.trigger(player, instance -> instance.matches(portal));
	}

	public record TriggerInstance(Optional<Holder<LootItemCondition>> player, Optional<PortalPredicate> portal) implements SimpleInstance {
		public static final Codec<TriggerInstance> CODEC = RecordCodecBuilder.create(i -> i.group(
				LootItemCondition.CODEC.optionalFieldOf("player").forGetter(TriggerInstance::player),
				PortalPredicate.CODEC.optionalFieldOf("portal").forGetter(TriggerInstance::portal)
		).apply(i, TriggerInstance::new));

		public boolean matches(PortalReference portal) {
			return this.portal.isEmpty() || this.portal.get().test(portal);
		}
	}
}
