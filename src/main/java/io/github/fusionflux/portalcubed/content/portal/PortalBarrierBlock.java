package io.github.fusionflux.portalcubed.content.portal;

import com.google.common.collect.ImmutableMap;
import com.mojang.serialization.MapCodec;

import io.github.fusionflux.portalcubed.framework.shape.voxel.VoxelShaper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.MultifaceBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class PortalBarrierBlock extends MultifaceBlock {
	public static final MapCodec<PortalBarrierBlock> CODEC = simpleCodec(PortalBarrierBlock::new);

	private static final VoxelShaper SHAPE = VoxelShaper.forDirectional(Block.box(1, 15, 1, 15, 16, 15), Direction.UP);

	private final ImmutableMap<BlockState, VoxelShape> shapesCache;

	public PortalBarrierBlock(Properties properties) {
		super(properties);
		this.shapesCache = this.getShapeForEachState(PortalBarrierBlock::calculateMultifaceShape);
	}

	private static VoxelShape calculateMultifaceShape(BlockState state) {
		VoxelShape multiFaceShape = Shapes.empty();

		for (Direction direction : DIRECTIONS) {
			if (hasFace(state, direction)) {
				multiFaceShape = Shapes.or(multiFaceShape, SHAPE.get(direction));
			}
		}

		return multiFaceShape.isEmpty() ? Shapes.block() : multiFaceShape;
	}

	@Override
	protected boolean propagatesSkylightDown(BlockState state) {
		return state.getFluidState().isEmpty();
	}

	@Override
	protected RenderShape getRenderShape(BlockState state) {
		return RenderShape.INVISIBLE;
	}

	@Override
	protected float getShadeBrightness(BlockState state, BlockGetter level, BlockPos pos) {
		return 1.0F;
	}

	@Override
	public VoxelShape getCollisionShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
		return Shapes.empty();
	}

	@Override
	public VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
		return context.isHoldingItem(this.asItem()) ? this.shapesCache.getOrDefault(state, Shapes.empty()) : Shapes.empty();
	}
}
