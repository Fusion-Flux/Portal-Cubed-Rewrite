package io.github.fusionflux.portalcubed.content.button;

import java.util.Optional;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.advancements.predicates.BlockPredicate;
import net.minecraft.advancements.predicates.entity.EntityPredicate;
import net.minecraft.advancements.triggers.SimpleCriterionTrigger;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

public class EntityOnButtonTrigger extends SimpleCriterionTrigger<EntityOnButtonTrigger.Instance> {
	@Override
	public Codec<Instance> codec() {
		return Instance.CODEC;
	}

	public void trigger(ServerPlayer player, BlockPos buttonPos, Entity entity) {
		this.trigger(player, instance -> instance.matches(player, buttonPos, entity));
	}

	public record Instance(BlockPredicate button, EntityPredicate entity) implements SimpleInstance {
		public static final Codec<Instance> CODEC = RecordCodecBuilder.create(i -> i.group(
				BlockPredicate.CODEC.fieldOf("button").forGetter(Instance::button),
				EntityPredicate.CODEC.fieldOf("entity").forGetter(Instance::entity)
		).apply(i, Instance::new));

		public boolean matches(ServerPlayer player, BlockPos pos, Entity entity) {
			return this.button.matches(player.level(), pos) && this.entity.matches(player, entity);
		}

		@Override
		public Optional<Holder<LootItemCondition>> player() {
			return Optional.empty();
		}
	}
}
