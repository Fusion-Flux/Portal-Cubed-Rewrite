package io.github.fusionflux.portalcubed.framework.construct;

import com.mojang.serialization.Codec;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.StringRepresentable;

import net.minecraft.world.item.Item;

import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;

import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.function.Supplier;

/**
 * A Construct is a structure or set of structures placable by the Construction Cannon.
 */
public abstract class Construct {
	public static final Codec<Construct> CODEC = Type.CODEC.dispatch(
			construct -> construct.type, Type::codec
	);

	public final Type type;
	public final Item material;

	public Construct(Type type, Item material) {
		this.type = type;
		this.material = material;
	}

	public abstract ResourceLocation getStructure(ConstructPlacementContext ctx);

	public record Holder(ResourceLocation id, Construct construct) {
		public Holder(Map.Entry<ResourceLocation, Construct> entry) {
			this(entry.getKey(), entry.getValue());
		}
	}

	public enum Type implements StringRepresentable {
		MONO("monodirectional", () -> MonoConstruct.CODEC),
		BI("bidirectional", () -> BiConstruct.CODEC);

		public static Codec<Type> CODEC = StringRepresentable.fromEnum(Type::values);

		private final String name;
		private final Supplier<Codec<? extends Construct>> supplier;

		Type(String name, Supplier<Codec<? extends Construct>> supplier) {
			this.name = name;
			this.supplier = supplier;
		}

		@Override
		@NotNull
		public String getSerializedName() {
			return this.name;
		}

		public Codec<? extends Construct> codec() {
			return this.supplier.get();
		}
	}
}
