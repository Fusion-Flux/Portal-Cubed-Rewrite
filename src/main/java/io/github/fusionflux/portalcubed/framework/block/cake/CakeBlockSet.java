package io.github.fusionflux.portalcubed.framework.block.cake;

import java.util.EnumMap;
import java.util.IdentityHashMap;
import java.util.Map;

import org.jetbrains.annotations.Nullable;

import io.github.fusionflux.portalcubed.framework.registration.Registrar;
import io.github.fusionflux.portalcubed.framework.registration.block.BlockHelper;
import io.github.fusionflux.portalcubed.framework.registration.block.BlockItemProvider;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;

public class CakeBlockSet {
	private final Block cake;
	private final Block uncoloredCandled;
	private final Map<DyeColor, Block> candled;
	private final Map<Block, Block> candleToCake;

	public CakeBlockSet(String name, Registrar registrar, BlockBehaviour.Properties settings) {
		this.candled = new EnumMap<>(DyeColor.class);
		this.candleToCake = new IdentityHashMap<>();

		BlockHelper blocks = registrar.blocks;

		this.cake = blocks.create(name, s -> new CustomCakeBlock(s, this.candleToCake))
				.properties(settings).build();
		this.uncoloredCandled = blocks.create("candle_" + name, s -> new CustomCandleCakeBlock(this.cake, Blocks.CANDLE, s))
				.properties(settings).item(BlockItemProvider::noItem).build();

		this.candleToCake.put(Blocks.CANDLE, this.uncoloredCandled);

		for (DyeColor color : DyeColor.values()) {
			String candleName = color.getSerializedName() + "_candle";
			ResourceKey<Block> candleKey = ResourceKey.create(Registries.BLOCK, ResourceLocation.withDefaultNamespace(candleName));
			// TODO: PORT
			Block candleBlock = BuiltInRegistries.BLOCK.getOrThrow(candleKey);

			Block candleCake = blocks.create(candleName + "_" + name, s -> new CustomCandleCakeBlock(this.cake, candleBlock, s))
					.properties(settings).item(BlockItemProvider::noItem).build();

			this.candleToCake.put(candleBlock, candleCake);
			this.candled.put(color, candleCake);
		}
	}

	public Block getCake() {
		return cake;
	}

	public Block getCandled(@Nullable DyeColor color) {
		return color == null ? this.uncoloredCandled : this.candled.get(color);
	}
}
