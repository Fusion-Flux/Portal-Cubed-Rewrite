package io.github.fusionflux.portalcubed.framework.construct.set;

import com.mojang.serialization.Codec;

import io.github.fusionflux.portalcubed.framework.construct.Construct;
import io.github.fusionflux.portalcubed.framework.construct.ConstructPlacementContext;
import io.github.fusionflux.portalcubed.framework.construct.ConfiguredConstruct;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.util.StringRepresentable;

import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Rotation;

import io.github.fusionflux.portalcubed.framework.gui.util.AdvancedTooltip;

import org.jetbrains.annotations.NotNull;

import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

/**
 * A ConstructSet is a set of one or more Constructs placeable by the Construction Cannon.
 */
public abstract class ConstructSet {
	public static final Codec<ConstructSet> CODEC = Type.CODEC.dispatch(
			construct -> construct.type, Type::codec
	);

	public final Type type;
	public final TagKey<Item> material;
	public final int cost;
	public final ConfiguredConstruct preview;

	public ConstructSet(Type type, TagKey<Item> material, int cost, ConfiguredConstruct preview) {
		this.type = type;
		this.material = material;
		this.cost = cost;
		this.preview = preview;
	}

	public ConstructSet(Type type, TagKey<Item> material, int cost, Construct preview) {
		this(type, material, cost, new ConfiguredConstruct(preview));
	}

	public abstract ConfiguredConstruct choose(ConstructPlacementContext ctx);

	public void appendTooltip(AdvancedTooltip.Builder builder) {
	}

	public static Component getName(ResourceLocation id) {
		String key = "construct_set." + id.toString().replace(':', '.').replace('/', '.');
		return Component.translatable(key);
	}

	public static int getCost(Optional<Integer> cost, Construct construct) {
		return cost.orElseGet(() -> construct.getBlocks(Rotation.NONE).size());
	}

	public record Holder(ResourceLocation id, ConstructSet constructSet) {
		public Holder(Map.Entry<ResourceLocation, ConstructSet> entry) {
			this(entry.getKey(), entry.getValue());
		}
	}

	public enum Type implements StringRepresentable {
		SINGLE(() -> SingleConstructSet.CODEC),
		PILLAR(() -> PillarConstructSet.CODEC);

		public static Codec<Type> CODEC = StringRepresentable.fromEnum(Type::values);

		private final String name;
		private final Supplier<Codec<? extends ConstructSet>> supplier;

		Type(Supplier<Codec<? extends ConstructSet>> supplier) {
			this.name = this.name().toLowerCase(Locale.ROOT);
			this.supplier = supplier;
		}

		@Override
		@NotNull
		public String getSerializedName() {
			return this.name;
		}

		public Codec<? extends ConstructSet> codec() {
			return this.supplier.get();
		}
	}
}
