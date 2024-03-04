package io.github.fusionflux.portalcubed.framework.construct.set;

import com.mojang.serialization.Codec;

import io.github.fusionflux.portalcubed.framework.construct.Construct;
import io.github.fusionflux.portalcubed.framework.construct.ConstructPlacementContext;
import io.github.fusionflux.portalcubed.framework.construct.ConfiguredConstruct;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.util.StringRepresentable;

import net.minecraft.world.item.Item;

import org.jetbrains.annotations.NotNull;

import java.util.Locale;
import java.util.Map;
import java.util.function.Supplier;

/**
 * A ConstructSet is a set of one or more Constructs placable by the Construction Cannon.
 */
public abstract class ConstructSet {
	public static final Codec<ConstructSet> CODEC = Type.CODEC.dispatch(
			construct -> construct.type, Type::codec
	);

	public final Type type;
	public final TagKey<Item> material;
	public final ConfiguredConstruct preview;

	public ConstructSet(Type type, TagKey<Item> material, ConfiguredConstruct preview) {
		this.type = type;
		this.material = material;
		this.preview = preview;
	}

	public ConstructSet(Type type, TagKey<Item> material, Construct preview) {
		this(type, material, new ConfiguredConstruct(preview));
	}

	public abstract ConfiguredConstruct choose(ConstructPlacementContext ctx);

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
