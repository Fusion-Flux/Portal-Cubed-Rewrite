package io.github.fusionflux.portalcubed.framework.signage;

import java.util.Locale;
import java.util.Optional;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import io.github.fusionflux.portalcubed.framework.util.EvenMoreCodecs;
import io.github.fusionflux.portalcubed.framework.util.PortalCubedStreamCodecs;
import io.netty.buffer.ByteBuf;
import net.minecraft.Optionull;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.StringRepresentable;

public record Signage(Optional<ResourceLocation> cleanTexture, Optional<ResourceLocation> agedTexture, Component name, Size size) {
	public static final Codec<Signage> CODEC = EvenMoreCodecs.validate(
			RecordCodecBuilder.create(instance -> instance.group(
					ResourceLocation.CODEC.optionalFieldOf("clean_texture").forGetter(Signage::cleanTexture),
					ResourceLocation.CODEC.optionalFieldOf("aged_texture").forGetter(Signage::agedTexture),
					ComponentSerialization.CODEC.fieldOf("name").forGetter(Signage::name),
					Size.CODEC.fieldOf("size").forGetter(Signage::size)
			).apply(instance, Signage::new)),
			Signage::validate
	);

	public static final StreamCodec<ByteBuf, Signage> STREAM_CODEC = StreamCodec.composite(
			ByteBufCodecs.optional(ResourceLocation.STREAM_CODEC), Signage::cleanTexture,
			ByteBufCodecs.optional(ResourceLocation.STREAM_CODEC), Signage::agedTexture,
			ComponentSerialization.TRUSTED_CONTEXT_FREE_STREAM_CODEC, Signage::name,
			Size.STREAM_CODEC, Signage::size,
			Signage::new
	);

	public ResourceLocation selectTexture(boolean aged) {
		if (this.cleanTexture.isPresent() && !aged)
			return this.cleanTexture.get();
		if (this.agedTexture.isPresent() && aged)
			return this.agedTexture.get();
		return this.cleanTexture.orElse(this.agedTexture.orElseThrow());
	}

	private static DataResult<Signage> validate(Signage signage) {
		if (signage.cleanTexture.isEmpty() && signage.agedTexture.isEmpty())
			return DataResult.error(() -> "Signage must have at least one texture present");
		return DataResult.success(signage);
	}

	public static final class Holder implements Comparable<Holder> {
		public static final StreamCodec<ByteBuf, @Nullable Holder> STREAM_CODEC = PortalCubedStreamCodecs
				.nullable(ResourceLocation.STREAM_CODEC)
				.map(SignageManager.INSTANCE::get, holder -> Optionull.map(holder, Holder::id));

		private final ResourceLocation id;
		private Signage value;

		public Holder(ResourceLocation id, Signage value) {
			this.id = id;
			this.value = value;
		}

		public ResourceLocation id() {
			return this.id;
		}

		@Nullable
		public Signage value() {
			return this.value;
		}

		void bindValue(Signage value) {
			this.value = value;
		}

		@Override
		public int hashCode() {
			return this.id.hashCode();
		}

		@Override
		public boolean equals(Object obj) {
			if (!(obj instanceof Holder other))
				return false;
			return this.id.equals(other.id);
		}

		@Override
		public int compareTo(@NotNull Signage.Holder o) {
			return this.id.compareTo(o.id);
		}
	}

	public enum Size implements StringRepresentable {
		LARGE,
		SMALL;

		public static final Codec<Size> CODEC = StringRepresentable.fromEnum(Size::values);
		public static final StreamCodec<ByteBuf, Size> STREAM_CODEC = PortalCubedStreamCodecs.ofEnum(Size.class);

		public final String name = this.name().toLowerCase(Locale.ROOT);

		@Override
		@NotNull
		public String getSerializedName() {
			return this.name;
		}
	}
}
