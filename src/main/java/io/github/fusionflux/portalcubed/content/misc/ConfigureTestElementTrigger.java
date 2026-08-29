package io.github.fusionflux.portalcubed.content.misc;

import java.util.Optional;
import java.util.Set;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import io.github.fusionflux.portalcubed.content.PortalCubedRegistries;
import io.github.fusionflux.portalcubed.content.misc.ConfigureTestElementTrigger.Instance;
import io.github.fusionflux.portalcubed.framework.util.PortalCubedCodecs;
import net.minecraft.advancements.triggers.SimpleCriterionTrigger;
import net.minecraft.core.Holder;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

public class ConfigureTestElementTrigger extends SimpleCriterionTrigger<Instance> {
	@Override
	public Codec<Instance> codec() {
		return Instance.CODEC;
	}

	public void trigger(ServerPlayer player, Set<Identifier> changedSettings) {
		this.trigger(player, instance -> instance.matches(changedSettings));
	}

	public record Instance(Set<Identifier> settings) implements SimpleInstance {
		public static final Codec<Set<Identifier>> SETTINGS_CODEC = PortalCubedCodecs.singleOrStrictSetOf(
				PortalCubedRegistries.TEST_ELEMENT_SETTINGS.byNameCodec()
		);

		public static final Codec<Instance> CODEC = RecordCodecBuilder.create(i -> i.group(
				SETTINGS_CODEC.fieldOf("test_element_settings").forGetter(Instance::settings)
		).apply(i, Instance::new));

		private boolean matches(Set<Identifier> changedSettings) {
			for (Identifier setting : changedSettings) {
				if (this.settings.contains(setting)) {
					return true;
				}
			}

			return false;
		}

		@Override
		public Optional<Holder<LootItemCondition>> player() {
			return Optional.empty();
		}
	}
}
