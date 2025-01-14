package io.github.fusionflux.portalcubed.content.portal.gun;

import java.util.Locale;

import net.minecraft.util.ExtraCodecs;

import org.jetbrains.annotations.Nullable;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import io.github.fusionflux.portalcubed.content.portal.Polarity;
import net.minecraft.client.color.item.ItemTintSource;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

public record PortalGunTintSource(Selection selection, int defaultColor) implements ItemTintSource {
	public static final MapCodec<PortalGunTintSource> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
			Selection.CODEC.fieldOf("polarity").forGetter(PortalGunTintSource::selection),
			ExtraCodecs.RGB_COLOR_CODEC.optionalFieldOf("default", 0).forGetter(PortalGunTintSource::defaultColor)
	).apply(i, PortalGunTintSource::new));

	@Override
	public int calculate(ItemStack stack, @Nullable ClientLevel level, @Nullable LivingEntity entity) {
		PortalGunSettings settings = PortalGunItem.getGunSettings(stack);
		if (settings == null) {
			return this.defaultColor;
		}

		Polarity polarity = switch (this.selection) {
			case ACTIVE -> settings.active();
			case PRIMARY -> Polarity.PRIMARY;
			case SECONDARY -> Polarity.SECONDARY;
		};
		return settings.portalSettingsOf(polarity).color();
	}

	@Override
	public MapCodec<? extends ItemTintSource> type() {
		return CODEC;
	}

	public enum Selection implements StringRepresentable {
		ACTIVE, PRIMARY, SECONDARY;

		private final String serialized = this.name().toLowerCase(Locale.ROOT);

		public static final Codec<Selection> CODEC = StringRepresentable.fromEnum(Selection::values);

		@Override
		public String getSerializedName() {
			return this.serialized;
		}
	}
}
