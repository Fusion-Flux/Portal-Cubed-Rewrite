package io.github.fusionflux.portalcubed.content.misc;

import com.mojang.serialization.Codec;

import com.mojang.serialization.DataResult;

import com.mojang.serialization.codecs.RecordCodecBuilder;

import io.github.fusionflux.portalcubed.content.PortalCubedRegistries;
import io.github.fusionflux.portalcubed.content.misc.ConfigureTestElementTrigger.Instance;
import io.github.fusionflux.portalcubed.framework.util.PortalCubedCodecs;
import net.minecraft.advancements.critereon.ContextAwarePredicate;
import net.minecraft.advancements.critereon.SimpleCriterionTrigger;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

public class ConfigureTestElementTrigger extends SimpleCriterionTrigger<Instance> {
	@Override
	public Codec<Instance> codec() {
		return Instance.CODEC;
	}

	public void trigger(ServerPlayer player, Set<ResourceLocation> changedSettings) {
		this.trigger(player, instance -> instance.matches(changedSettings));
	}

	public record Instance(Set<ResourceLocation> settings) implements SimpleInstance {
		public static final Codec<Set<ResourceLocation>> SETTINGS_CODEC = PortalCubedCodecs.singleOrStrictSetOf(
				PortalCubedRegistries.TEST_ELEMENT_SETTINGS.byNameCodec()
		);

		public static final Codec<Instance> CODEC = RecordCodecBuilder.create(i -> i.group(
				SETTINGS_CODEC.fieldOf("test_element_settings").forGetter(Instance::settings)
		).apply(i, Instance::new));

		private boolean matches(Set<ResourceLocation> changedSettings) {
			for (ResourceLocation setting : changedSettings) {
				if (this.settings.contains(setting)) {
					return true;
				}
			}

			return false;
		}

		@Override
		public Optional<ContextAwarePredicate> player() {
			return Optional.empty();
		}
	}
}
