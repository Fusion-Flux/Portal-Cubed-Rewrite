package io.github.fusionflux.portalcubed.content.test;

import io.github.fusionflux.portalcubed.framework.shape.VoxelShenanigans;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.EntityCollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

import org.joml.Quaternionf;

public class TestBlock extends Block {
	private static final VoxelShape shape = Block.box(4, 4, 4, 10, 10, 10);

	public TestBlock(Properties properties) {
		super(properties);
	}

	@Override
	public VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
		int deg = context instanceof EntityCollisionContext e && e.getEntity() != null ? e.getEntity().tickCount : 0;
		Quaternionf dynamicRot = new Quaternionf().rotateY(Mth.DEG_TO_RAD * deg * 2)
				.rotateX(Mth.DEG_TO_RAD * deg * 1.5f)
				.rotateZ(Mth.DEG_TO_RAD * deg * 0.7f);
		Quaternionf staticRot = new Quaternionf().rotationXYZ(
				0,
				Mth.DEG_TO_RAD * 30,
				0
		);
		return VoxelShenanigans.rotateShape(shape, staticRot);
	}

	@Override
	public RenderShape getRenderShape(BlockState state) {
		return RenderShape.INVISIBLE;
	}
}
