package io.github.fusionflux.portalcubed.content.button;

import java.util.Optional;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.advancements.predicates.entity.EntityPredicate;
import net.minecraft.advancements.triggers.SimpleCriterionTrigger;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.Validatable;
import net.minecraft.world.level.storage.loot.ValidationContextSource;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

public class EntityOnButtonTrigger extends SimpleCriterionTrigger<EntityOnButtonTrigger.Instance> {
	@Override
	public Codec<Instance> codec() {
		return Instance.CODEC;
	}

	public void trigger(ServerPlayer player, Entity entity) {
		LootContext context = EntityPredicate.createContext(player, entity);
		this.trigger(player, instance -> instance.matches(context));
	}

	public record Instance(Optional<Holder<LootItemCondition>> player, Optional<Holder<LootItemCondition>> entity) implements SimpleInstance {
		public static final Codec<Instance> CODEC = RecordCodecBuilder.create(i -> i.group(
				LootItemCondition.CODEC.optionalFieldOf("player").forGetter(Instance::player),
				LootItemCondition.CODEC.optionalFieldOf("entity").forGetter(Instance::entity)
		).apply(i, Instance::new));

		public boolean matches(LootContext context) {
			return this.entity.isEmpty() || this.entity.get().value().test(context);
		}

		@Override
		public void validate(ValidationContextSource validator) {
			SimpleInstance.super.validate(validator);
			Validatable.validateHolder(validator.entityContext(), "entity", this.entity);
		}
	}
}
