package io.github.fusionflux.portalcubed.content.cannon.data;

import com.mojang.serialization.Codec;

import com.mojang.serialization.codecs.RecordCodecBuilder;

public record DeviceData(PlacementSettings settings, DeviceInventory inventory) {
	public static final String NBT_KEY = "construction_cannon_data";
	public static final Codec<DeviceData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			PlacementSettings.CODEC.fieldOf("settings").forGetter(DeviceData::settings),
			DeviceInventory.CODEC.fieldOf("inventory").forGetter(DeviceData::inventory)
	).apply(instance, DeviceData::new));
}
