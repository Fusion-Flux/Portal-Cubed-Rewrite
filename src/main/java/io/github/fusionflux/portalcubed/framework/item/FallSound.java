package io.github.fusionflux.portalcubed.framework.item;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.ExtraCodecs;

public record FallSound(Holder<SoundEvent> sound, int distance) {
	public static final Codec<FallSound> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			SoundEvent.CODEC.fieldOf("sound").forGetter(FallSound::sound),
			ExtraCodecs.POSITIVE_INT.fieldOf("distance").forGetter(FallSound::distance)
	).apply(instance, FallSound::new));
	public static final StreamCodec<RegistryFriendlyByteBuf, FallSound> STREAM_CODEC = StreamCodec.composite(
			SoundEvent.STREAM_CODEC, FallSound::sound,
			ByteBufCodecs.INT, FallSound::distance,
			FallSound::new
	);
}
