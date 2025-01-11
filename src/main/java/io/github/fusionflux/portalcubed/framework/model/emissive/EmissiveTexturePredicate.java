package io.github.fusionflux.portalcubed.framework.model.emissive;

import java.util.Locale;
import java.util.function.Predicate;

import org.jetbrains.annotations.NotNull;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;

import io.github.fusionflux.portalcubed.framework.util.EvenMoreCodecs;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.ResourceLocationPattern;
import net.minecraft.util.StringRepresentable;

public interface EmissiveTexturePredicate extends Predicate<ResourceLocation> {
	Codec<EmissiveTexturePredicate> CODEC = Codec.withAlternative(
			Type.CODEC.dispatch(EmissiveTexturePredicate::type, Type::codec),
			Single.CODEC
	);

	Type type();

	record Single(ResourceLocation id) implements EmissiveTexturePredicate {
		public static final Codec<Single> CODEC = EvenMoreCodecs.MOD_ID.xmap(Single::new, Single::id);

		@Override
		public boolean test(ResourceLocation resourceLocation) {
			return resourceLocation.equals(id);
		}

		@Override
		public Type type() {
			return Type.SINGLE;
		}
	}

	record Folder(ResourceLocation id) implements EmissiveTexturePredicate {
		public static final Codec<Folder> CODEC = EvenMoreCodecs.MOD_ID.xmap(Folder::new, Folder::id);

		@Override
		public boolean test(ResourceLocation resourceLocation) {
			return resourceLocation.getNamespace().equals(id.getNamespace()) && resourceLocation.getPath().startsWith(id.getPath() + "/");
		}

		@Override
		public Type type() {
			return Type.FOLDER;
		}
	}

	record Pattern(ResourceLocationPattern pattern) implements EmissiveTexturePredicate {
		public static final Codec<Pattern> CODEC = ResourceLocationPattern.CODEC.xmap(Pattern::new, Pattern::pattern);

		@Override
		public boolean test(ResourceLocation resourceLocation) {
			return pattern.locationPredicate().test(resourceLocation);
		}

		@Override
		public Type type() {
			return Type.PATTERN;
		}
	}

	enum Type implements StringRepresentable {
		SINGLE(Single.CODEC),
		FOLDER(Folder.CODEC),
		PATTERN(Pattern.CODEC);

		public static final Codec<Type> CODEC = StringRepresentable.fromEnum(Type::values);

		private final String name;
		private final MapCodec<? extends EmissiveTexturePredicate> codec;

		Type(Codec<? extends EmissiveTexturePredicate> codec) {
			this.name = this.name().toLowerCase(Locale.ROOT);
			this.codec = codec.fieldOf(this.name);
		}

		@Override
		@NotNull
		public String getSerializedName() {
			return this.name;
		}

		public MapCodec<? extends EmissiveTexturePredicate> codec() {
			return this.codec;
		}
	}
}
