package io.github.fusionflux.portalcubed.framework.construct;

import org.jetbrains.annotations.Nullable;

import io.github.fusionflux.portalcubed.framework.construct.Construct.BlockInfo;
import io.github.fusionflux.portalcubed.framework.util.VirtualBlockGetter;
import it.unimi.dsi.fastutil.longs.Long2ReferenceOpenHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public final class VirtualConstructEnvironment extends VirtualBlockGetter {
	private final Long2ReferenceOpenHashMap<BlockEntity> cachedBlockEntities = new Long2ReferenceOpenHashMap<>();

	private final ConfiguredConstruct construct;

	public VirtualConstructEnvironment(ConfiguredConstruct construct) {
		super(pos -> 0, pos -> 15);
		this.construct = construct;
	}

	public BlockInfo getBlockInfo(BlockPos pos) {
		return construct.blocks.getOrDefault(pos, BlockInfo.EMPTY);
	}

	@Override
	@Nullable
	public BlockEntity getBlockEntity(BlockPos pos) {
		return cachedBlockEntities.computeIfAbsent(pos.asLong(), $ -> {
			var blockInfo = getBlockInfo(pos);
			var maybeNbt = blockInfo.maybeNbt();
			return maybeNbt.map(nbt -> BlockEntity.loadStatic(pos, blockInfo.state(), nbt)).orElse(null);
		});
	}

	@Override
	public BlockState getBlockState(BlockPos pos) {
		return getBlockInfo(pos).state();
	}

	@Override
	public int getHeight() {
		return construct.bounds.getYSpan();
	}

	@Override
	public int getMinBuildHeight() {
		return 0;
	}

	@Override
	public float getShade(Direction direction, boolean shaded) {
		// Vanilla shading values pulled from ClientLevel
		if (!shaded) {
			return 1f;
		} else {
			switch(direction) {
				case DOWN:
					return .5f;
				case UP:
					return 1f;
				case NORTH:
				case SOUTH:
					return .8f;
				case WEST:
				case EAST:
					return .6f;
				default:
					return 1f;
			}
		}
	}
}
