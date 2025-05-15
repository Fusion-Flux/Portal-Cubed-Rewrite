package io.github.fusionflux.portalcubed.content.cannon;

import java.util.Optional;
import java.util.function.Consumer;

import org.apache.commons.lang3.mutable.MutableBoolean;
import org.jetbrains.annotations.Nullable;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import io.github.fusionflux.portalcubed.framework.construct.ConstructManager;
import io.github.fusionflux.portalcubed.framework.construct.set.ConstructSet;
import io.github.fusionflux.portalcubed.framework.item.TagTranslation;
import io.netty.buffer.ByteBuf;
import net.minecraft.ChatFormatting;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipProvider;

public record CannonSettings(
		Optional<TagKey<Item>> material,
		Optional<ResourceLocation> construct,
		boolean preview,
		float previewOpacity,
		boolean replaceMode
) implements TooltipProvider {
	public static final Codec<CannonSettings> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			TagKey.codec(Registries.ITEM).optionalFieldOf("material").forGetter(CannonSettings::material),
			ResourceLocation.CODEC.optionalFieldOf("construct").forGetter(CannonSettings::construct),
			Codec.BOOL.fieldOf("preview").forGetter(CannonSettings::preview),
			Codec.FLOAT.fieldOf("preview_opacity").forGetter(CannonSettings::previewOpacity),
			Codec.BOOL.fieldOf("replace_mode").forGetter(CannonSettings::replaceMode)
	).apply(instance, CannonSettings::new));

	public static final StreamCodec<ByteBuf, CannonSettings> STREAM_CODEC = StreamCodec.composite(
			ByteBufCodecs.optional(TagKey.streamCodec(Registries.ITEM)), CannonSettings::material,
			ByteBufCodecs.optional(ResourceLocation.STREAM_CODEC), CannonSettings::construct,
			ByteBufCodecs.BOOL, CannonSettings::preview,
			ByteBufCodecs.FLOAT, CannonSettings::previewOpacity,
			ByteBufCodecs.BOOL, CannonSettings::replaceMode,
			CannonSettings::new
	);

	public static final Component MATERIAL_TOOLTIP = ConstructionCannonItem.translate("material").withStyle(ChatFormatting.GRAY);
	public static final Component CONSTRUCT_TOOLTIP = ConstructionCannonItem.translate("construct_set").withStyle(ChatFormatting.GRAY);

	public static final CannonSettings DEFAULT = builder().build();

	@Nullable
	public Configured validate() {
		return this.material.flatMap(
				material -> this.construct.map(ConstructManager.INSTANCE::getConstructSet)
						.map(set -> new Configured(material, set))
		).orElse(null);
    }

	public static CannonSettings.Builder builder(CannonSettings settings) {
		return new CannonSettings.Builder(settings);
	}

	public static CannonSettings.Builder builder() {
		return new CannonSettings.Builder();
	}

	@Override
	public void addToTooltip(Item.TooltipContext context, Consumer<Component> tooltipAdder, TooltipFlag tooltipFlag) {
		MutableBoolean first = new MutableBoolean(true);

		this.material().ifPresent(material -> {
			if (first.isTrue()) {
				tooltipAdder.accept(CommonComponents.EMPTY);
				first.setFalse();
			}

			tooltipAdder.accept(MATERIAL_TOOLTIP);
			Component name = TagTranslation.translate(material);
			tooltipAdder.accept(CommonComponents.space().append(name).withStyle(ChatFormatting.BLUE));
		});

		this.construct().ifPresent(construct -> {
			if (first.isTrue()) {
				tooltipAdder.accept(CommonComponents.EMPTY);
				first.setFalse();
			}

			tooltipAdder.accept(CONSTRUCT_TOOLTIP);
			Component name = ConstructSet.getName(this.construct().get());
			tooltipAdder.accept(CommonComponents.space().append(name).withStyle(ChatFormatting.BLUE));
		});
	}

	public record Configured(TagKey<Item> material, ConstructSet construct) {
	}

	public static final class Builder {
		private Optional<TagKey<Item>> material = Optional.empty();
		private Optional<ResourceLocation> construct = Optional.empty();
		private boolean preview = true;
		private float previewOpacity = .55f;
		private boolean replaceMode = false;

		Builder() {
		}

		Builder(CannonSettings settings) {
			this.material = settings.material;
			this.construct = settings.construct;
			this.preview = settings.preview;
			this.previewOpacity = settings.previewOpacity;
			this.replaceMode = settings.replaceMode;
		}

		public CannonSettings.Builder setMaterial(TagKey<Item> material) {
			this.material = Optional.of(material);
			return this;
		}

		public CannonSettings.Builder setConstruct(ResourceLocation construct) {
			this.construct = Optional.of(construct);
			return this;
		}

		public CannonSettings.Builder setPreview(boolean preview) {
			this.preview = preview;
			return this;
		}

		public CannonSettings.Builder setPreviewOpacity(float previewOpacity) {
			this.previewOpacity = previewOpacity;
			return this;
		}

		public CannonSettings.Builder setReplaceMode(boolean replaceMode) {
			this.replaceMode = replaceMode;
			return this;
		}

		public CannonSettings build() {
			return new CannonSettings(this.material, this.construct, this.preview, this.previewOpacity, this.replaceMode);
		}
	}
}
