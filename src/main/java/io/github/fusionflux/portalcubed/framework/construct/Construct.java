package io.github.fusionflux.portalcubed.framework.construct;

import com.mojang.serialization.Codec;

import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import io.github.fusionflux.portalcubed.framework.util.EvenMoreCodecs;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;

import org.jetbrains.annotations.Nullable;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Similar to a {@link StructureTemplate}, stores a map of relative block positions to block states.
 * Always contains at least one block.
 */
public class Construct {
	public static final Codec<Construct> CODEC = ExtraCodecs.validate(
			Codec.unboundedMap(
					EvenMoreCodecs.BLOCKPOS_STRING,
					BlockInfo.CODEC
			).xmap(Construct::new, construct -> construct.blocks),
			construct -> construct.blocks.isEmpty()
					? DataResult.error(() -> "Construct contains no blocks")
					: DataResult.success(construct)
	);

	private final Map<BlockPos, BlockInfo> blocks;
	private final Map<Rotation, Map<BlockPos, BlockInfo>> rotatedBlockCache;
	private final Map<Rotation, BoundingBox> boundsCache;

	private Construct(Map<BlockPos, BlockInfo> blocks) {
		this.blocks = blocks;
		this.rotatedBlockCache = new EnumMap<>(Rotation.class);
		this.boundsCache = new EnumMap<>(Rotation.class);

		this.rotatedBlockCache.put(Rotation.NONE, this.blocks);
	}

	public Map<BlockPos, BlockInfo> getBlocks(Rotation rotation) {
		return this.rotatedBlockCache.computeIfAbsent(rotation, $ -> {
			Map<BlockPos, BlockInfo> map = new HashMap<>();
			this.blocks.forEach((pos, info) -> {
				BlockPos rotatedPos = StructureTemplate.transform(pos, Mirror.NONE, rotation, BlockPos.ZERO);
				BlockInfo rotatedInfo = new BlockInfo(info.state.rotate(rotation), info.maybeNbt);
				map.put(rotatedPos, rotatedInfo);
			});
			return map;
		});
	}

	public BoundingBox getBounds(Rotation rotation) {
		return this.boundsCache.computeIfAbsent(rotation, $ -> {
			Map<BlockPos, BlockInfo> blocks = this.getBlocks(rotation);
			return BoundingBox.encapsulatingPositions(blocks.keySet()).orElseThrow();
		});
	}

	public void place(ServerLevel level, BlockPos origin, Rotation rotation) {
		this.getBlocks(rotation).forEach((relativePos, info) -> {
			BlockPos pos = origin.offset(relativePos);
			level.setBlockAndUpdate(pos, info.state());
			info.maybeNbt.ifPresent(nbt -> {
				BlockEntity be = level.getBlockEntity(pos);
				if (be != null) {
					be.load(nbt);
				}
			});
		});
	}

	public static class Builder {
		private final Map<BlockPos, BlockInfo> blocks = new HashMap<>();

		public Builder put(BlockPos pos, Block block) {
			return this.put(pos, new BlockInfo(block.defaultBlockState()));
		}

		public Builder put(BlockPos pos, BlockState state) {
			return this.put(pos, new BlockInfo(state));
		}

		public Builder put(BlockPos pos, BlockState state, @Nullable CompoundTag nbt) {
			return this.put(pos, new BlockInfo(state, nbt));
		}

		public Builder put(BlockPos pos, BlockInfo info) {
			if (this.blocks.put(pos, info) != null) {
				throw new IllegalArgumentException("Duplicate pos: " + pos);
			}
			return this;
		}

		public Construct build() {
			if (this.blocks.isEmpty())
				throw new IllegalStateException("No blocks added to builder");
			return new Construct(this.blocks);
		}
	}

	public record BlockInfo(BlockState state, Optional<CompoundTag> maybeNbt) {
		private static final Codec<BlockInfo> fullCodec = RecordCodecBuilder.create(instance -> instance.group(
				EvenMoreCodecs.BLOCKSTATE.fieldOf("state").forGetter(BlockInfo::state),
				CompoundTag.CODEC.optionalFieldOf("nbt").forGetter(BlockInfo::maybeNbt)
		).apply(instance, BlockInfo::new));

		private static final Codec<BlockInfo> byState = EvenMoreCodecs.BLOCKSTATE.flatComapMap(
				BlockInfo::new, info -> {
					if (info.maybeNbt.isPresent()) {
						return DataResult.error(() -> "NBT is present");
					}
					return DataResult.success(info.state);
				}
		);

		public static final Codec<BlockInfo> CODEC = EvenMoreCodecs.multiFormat(
				byState, fullCodec, info -> info.maybeNbt.isPresent()
		);

		public BlockInfo(BlockState state, @Nullable CompoundTag nbt) {
			this(state, Optional.ofNullable(nbt));
		}

		public BlockInfo(BlockState state) {
			this(state, Optional.empty());
		}

		@Nullable
		public CompoundTag nbt() {
			return this.maybeNbt.orElse(null);
		}
	}
}
