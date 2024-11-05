package io.github.fusionflux.portalcubed.framework.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public abstract class TickableBlockEntity extends BlockEntity {
	public int tickCount;

	public TickableBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
		super(type, pos, state);
	}

	public static <T extends TickableBlockEntity> void tick(Level world, BlockPos pos, BlockState state, T entity) {
		entity.tick();
		entity.tickCount++;
	}

	public abstract void tick();
}
