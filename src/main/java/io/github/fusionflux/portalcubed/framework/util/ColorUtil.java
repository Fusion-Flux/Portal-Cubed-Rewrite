package io.github.fusionflux.portalcubed.framework.util;

import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;
import java.util.Optional;

import io.github.fusionflux.portalcubed.data.tags.PortalCubedBlockTags;
import net.minecraft.Util;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

public class ColorUtil {
	public static final Map<DyeColor, Block> CANDLES = Util.make(() -> {
		Map<DyeColor, Block> map = new EnumMap<>(DyeColor.class);
		map.put(DyeColor.WHITE, Blocks.WHITE_CANDLE);
		map.put(DyeColor.ORANGE, Blocks.ORANGE_CANDLE);
		map.put(DyeColor.MAGENTA, Blocks.MAGENTA_CANDLE);
		map.put(DyeColor.LIGHT_BLUE, Blocks.LIGHT_BLUE_CANDLE);
		map.put(DyeColor.YELLOW, Blocks.YELLOW_CANDLE);
		map.put(DyeColor.LIME, Blocks.LIME_CANDLE);
		map.put(DyeColor.PINK, Blocks.PINK_CANDLE);
		map.put(DyeColor.GRAY, Blocks.GRAY_CANDLE);
		map.put(DyeColor.LIGHT_GRAY, Blocks.LIGHT_GRAY_CANDLE);
		map.put(DyeColor.CYAN, Blocks.CYAN_CANDLE);
		map.put(DyeColor.PURPLE, Blocks.PURPLE_CANDLE);
		map.put(DyeColor.BLUE, Blocks.BLUE_CANDLE);
		map.put(DyeColor.BROWN, Blocks.BROWN_CANDLE);
		map.put(DyeColor.GREEN, Blocks.GREEN_CANDLE);
		map.put(DyeColor.RED, Blocks.RED_CANDLE);
		map.put(DyeColor.BLACK, Blocks.BLACK_CANDLE);
		return Collections.unmodifiableMap(map);
	});

	public static Optional<Block> randomConfettiBlock(RandomSource random) {
		return BuiltInRegistries.BLOCK.getRandomElementOf(PortalCubedBlockTags.CONFETTI, random).map(Holder::value);
	}
}
