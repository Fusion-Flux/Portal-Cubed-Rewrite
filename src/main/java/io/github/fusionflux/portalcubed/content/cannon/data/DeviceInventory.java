package io.github.fusionflux.portalcubed.content.cannon.data;

import com.mojang.serialization.Codec;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.item.Item;

import java.util.Map;

public record DeviceInventory(Map<Item, Integer> items) {
	public static final Codec<DeviceInventory> CODEC = Codec.unboundedMap(
			BuiltInRegistries.ITEM.byNameCodec(),
			ExtraCodecs.POSITIVE_INT
	).xmap(DeviceInventory::new, DeviceInventory::items);
}
