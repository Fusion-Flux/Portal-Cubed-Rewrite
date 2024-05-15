package io.github.fusionflux.portalcubed.content.goo;

import com.mojang.serialization.MapCodec;

import io.github.fusionflux.portalcubed.content.PortalCubedItems;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.cauldron.CauldronInteraction;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.AbstractCauldronBlock;
import net.minecraft.world.level.block.state.BlockState;

import org.jetbrains.annotations.NotNull;

public class GooCauldronBlock extends AbstractCauldronBlock {
	public static final MapCodec<GooCauldronBlock> CODEC = simpleCodec(GooCauldronBlock::new);

	public static final CauldronInteraction.InteractionMap INTERACTION_MAP = Util.make(
			CauldronInteraction.newInteractionMap("toxic_goo"),
			interactionMap -> interactionMap.map().put(Items.BUCKET, (state, world, pos, player, interactionHand, stack) -> CauldronInteraction.fillBucket(
					state, world, pos, player, interactionHand, stack, PortalCubedItems.GOO_BUCKET.getDefaultInstance(), $ -> true, SoundEvents.BUCKET_FILL
			))
	);

	public GooCauldronBlock(Properties properties) {
		super(properties, INTERACTION_MAP);
	}

	@NotNull
	@Override
	public MapCodec<GooCauldronBlock> codec() {
		return CODEC;
	}

	@Override
	protected double getContentHeight(BlockState state) {
		return 0.9375d;
	}

	@Override
	public boolean isFull(BlockState state) {
		return true;
	}

	@Override
	public void entityInside(BlockState state, Level world, BlockPos pos, Entity entity) {
		if (isEntityInsideContent(state, pos, entity))
			GooFluid.hurt(world, entity);
	}

	@Override
	public int getAnalogOutputSignal(BlockState state, Level world, BlockPos pos) {
		return 3;
	}
}
