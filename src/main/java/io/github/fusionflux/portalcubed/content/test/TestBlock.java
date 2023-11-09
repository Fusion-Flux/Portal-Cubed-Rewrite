package io.github.fusionflux.portalcubed.content.test;

import io.github.fusionflux.portalcubed.framework.shape.VoxelShenanigans;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.EntityCollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import org.joml.Quaternionf;

public class TestBlock extends Block {
	private static final VoxelShape shape = Block.box(4, 4, 4, 12,12, 12);
	public TestBlock(Properties properties) {
		super(properties);
	}

	@Override
	public VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
		int deg = context instanceof EntityCollisionContext e && e.getEntity() != null ? e.getEntity().tickCount : 0;
		Quaternionf rotation = new Quaternionf().rotateY(Mth.DEG_TO_RAD * deg)
				.rotateZ(Mth.DEG_TO_RAD * deg * 1.5f)
				.rotateX((Mth.DEG_TO_RAD * deg * 0.5f));
		VoxelShape rotated = VoxelShenanigans.rotateShape(shape, rotation);
		return rotated;//Shapes.join(rotated, Block.box(7.5, 7.5, 7.5, 8.5, 8.5, 8.5), BooleanOp.OR);
	}

	@Override
	public RenderShape getRenderShape(BlockState state) {
		return RenderShape.INVISIBLE;
	}
}
