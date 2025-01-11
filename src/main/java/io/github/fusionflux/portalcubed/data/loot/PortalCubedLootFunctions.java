package io.github.fusionflux.portalcubed.data.loot;

import com.mojang.serialization.MapCodec;

import io.github.fusionflux.portalcubed.PortalCubed;
import io.github.fusionflux.portalcubed.content.prop.CopyPropVariantLootFunction;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType;

public class PortalCubedLootFunctions {
	public static final LootItemFunctionType<CopyPropVariantLootFunction> COPY_PROP_VARIANT = register("copy_prop_variant", CopyPropVariantLootFunction.CODEC);

	public static <T extends LootItemFunction> LootItemFunctionType<T> register(String name, MapCodec<T> codec) {
		return Registry.register(BuiltInRegistries.LOOT_FUNCTION_TYPE, PortalCubed.id(name), new LootItemFunctionType<>(codec));
	}

	public static void init() {
	}
}
