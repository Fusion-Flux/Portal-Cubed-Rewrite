package io.github.fusionflux.portalcubed.framework.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import org.jetbrains.annotations.NotNull;

public class NoCollisionMultifaceBlock extends SimpleMultifaceBlock {
	public NoCollisionMultifaceBlock(Properties properties) {
		super(properties);
	}

	@Override
	@NotNull
	public VoxelShape getCollisionShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
		return Shapes.empty();
	}
}
