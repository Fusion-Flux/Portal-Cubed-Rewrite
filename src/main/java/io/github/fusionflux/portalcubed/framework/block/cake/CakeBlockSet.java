package io.github.fusionflux.portalcubed.framework.block.cake;

import java.util.EnumMap;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;

import org.jetbrains.annotations.Nullable;

import io.github.fusionflux.portalcubed.framework.registration.Registrar;
import io.github.fusionflux.portalcubed.framework.registration.block.BlockBuilder;
import io.github.fusionflux.portalcubed.framework.registration.block.BlockFactory;
import io.github.fusionflux.portalcubed.framework.registration.block.BlockItemProvider;
import io.github.fusionflux.portalcubed.framework.util.ColorUtil;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

public final class CakeBlockSet {
	private final Block base;
	private final Block uncoloredCandled;
	private final Map<DyeColor, Block> candled;
	private final Map<Block, Block> candleToCake;

	private CakeBlockSet(Block base, Block uncoloredCandled, Map<DyeColor, Block> candled, Map<Block, Block> candleToCake) {
		this.base = base;
		this.uncoloredCandled = uncoloredCandled;
		this.candled = candled;
		this.candleToCake = candleToCake;
	}

	public Block getBase() {
		return this.base;
	}

	public Block getCandled(@Nullable DyeColor color) {
		return color == null ? this.uncoloredCandled : this.candled.get(color);
	}

	public Block getCandled(Block candle) {
		return this.candleToCake.get(candle);
	}

	public static Builder builder(String name, Registrar registrar) {
		return new Builder(name, registrar);
	}

	public static final class Builder {
		private final BlockBuilder<?> base;
		private final BlockBuilder<?> uncoloredCandled;
		private final Map<BlockBuilder<?>, CandledInfo> candled;
		// this needs to be created now for the base to reference it
		private final Map<Block, Block> candleToCake;

		// field used to smuggle the base cake into candled variant factories
		private Block builtBase = null;

		private Builder(String name, Registrar registrar) {
			this.candled = new IdentityHashMap<>();
			this.candleToCake = new IdentityHashMap<>();

			this.base = registrar.blocks.create(name, props -> new CustomCakeBlock(props, this.candleToCake));
			this.uncoloredCandled = registrar.blocks.create("candle_" + name, this.candledFactory(Blocks.CANDLE))
					.item(BlockItemProvider::noItem);

			for (DyeColor color : DyeColor.values()) {
				Block candle = ColorUtil.CANDLES.get(color);
				String blockName = color.getName() + "_candle_" + name;
				BlockBuilder<?> builder = registrar.blocks.create(blockName, this.candledFactory(candle))
						.item(BlockItemProvider::noItem);
				this.candled.put(builder, new CandledInfo(color, candle));
			}
		}

		public Builder all(Consumer<BlockBuilder<?>> consumer) {
			consumer.accept(this.base);
			consumer.accept(this.uncoloredCandled);
			this.candled.keySet().forEach(consumer);
			return this;
		}

		public Builder base(Consumer<BlockBuilder<?>> consumer) {
			consumer.accept(this.base);
			return this;
		}

		private Block getBase() {
			return Objects.requireNonNull(this.builtBase, "Base accessed too early");
		}

		private BlockFactory<CustomCandleCakeBlock> candledFactory(Block candle) {
			return props -> new CustomCandleCakeBlock(this.getBase(), candle, props);
		}

		public CakeBlockSet build() {
			this.builtBase = this.base.build();

			Block uncoloredCandled = this.uncoloredCandled.build();
			this.candleToCake.put(Blocks.CANDLE, uncoloredCandled);

			Map<DyeColor, Block> candled = new EnumMap<>(DyeColor.class);
			this.candled.forEach((builder, info) -> {
				Block block = builder.build();
				candled.put(info.color, block);
				this.candleToCake.put(info.candle, block);
			});

			return new CakeBlockSet(this.builtBase, uncoloredCandled, candled, this.candleToCake);
		}

		private record CandledInfo(DyeColor color, Block candle) {
		}
	}
}
