package io.github.fusionflux.portalcubed.content.button;

import java.util.Optional;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.advancements.critereon.BlockPredicate;
import net.minecraft.advancements.critereon.ContextAwarePredicate;
import net.minecraft.advancements.critereon.EntityPredicate;
import net.minecraft.advancements.critereon.SimpleCriterionTrigger;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;

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
			return this.button.matches(player.serverLevel(), pos) && this.entity.matches(player, entity);
		}

		@Override
		public Optional<ContextAwarePredicate> player() {
			return Optional.empty();
		}
	}
}
