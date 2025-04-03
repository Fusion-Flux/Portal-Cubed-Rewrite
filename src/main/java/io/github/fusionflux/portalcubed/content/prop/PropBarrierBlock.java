package io.github.fusionflux.portalcubed.content.prop;

import org.jetbrains.annotations.NotNull;

import io.github.fusionflux.portalcubed.content.prop.entity.Prop;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.BarrierBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.EntityCollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class PropBarrierBlock extends BarrierBlock {
	public PropBarrierBlock(Properties properties) {
		super(properties);
	}

	@NotNull
	@Override
	public VoxelShape getCollisionShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
		if (context instanceof EntityCollisionContext entityCollisionContext && entityCollisionContext.getEntity() instanceof Prop)
			return Shapes.block();
		return Shapes.empty();
	}

	@NotNull
	@Override
	public VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
		return context.isHoldingItem(this.asItem()) ? super.getShape(state, world, pos, context) : Shapes.empty();
	}
}
