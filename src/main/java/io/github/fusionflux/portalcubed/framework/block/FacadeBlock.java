package io.github.fusionflux.portalcubed.framework.block;

import java.util.Objects;

import org.jetbrains.annotations.NotNull;

import com.google.common.collect.ImmutableMap;
import com.mojang.serialization.MapCodec;

import io.github.fusionflux.portalcubed.content.portal.PortalBarrierBlock;
import io.github.fusionflux.portalcubed.content.portal.placement.PortalCollisionContext;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.MultifaceBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class FacadeBlock extends MultifaceBlock {
	public static final MapCodec<FacadeBlock> CODEC = simpleCodec(FacadeBlock::new);

	private final ImmutableMap<BlockState, VoxelShape> portalShapesCache;

	public FacadeBlock(Properties properties) {
		super(modifyProperties(properties));
		this.portalShapesCache = this.getShapeForEachState(PortalBarrierBlock::calculateShapeForPortals);
	}

	@Override
	@NotNull
	protected MapCodec<? extends MultifaceBlock> codec() {
		return CODEC;
	}

	@Override
	public boolean canBeReplaced(BlockState state, BlockPlaceContext context) {
		// all vanilla multifaces are always replaceable
		// facades should only be replaceable with themselves for adding faces
		return context.getItemInHand().is(this.asItem());
	}

	@Override
	protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
		if (context instanceof PortalCollisionContext) {
			// use a smaller hitbox in portal contexts, to avoid facades in corners causing false positives
			return Objects.requireNonNull(this.portalShapesCache.get(state));
		}

		return super.getShape(state, level, pos, context);
	}

	private static Properties modifyProperties(Properties properties) {
		// must be set to DESTROY to avoid dupes
		properties.pushReaction(PushReaction.DESTROY);
		// disable needing correct tool since they can just be popped off
		properties.pc$disableRequiresCorrectToolForDrops();

		return properties;
	}
}
