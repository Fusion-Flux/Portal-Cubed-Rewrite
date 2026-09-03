package io.github.fusionflux.portalcubed.content.goo;

import org.jetbrains.annotations.NotNull;

import io.github.fusionflux.portalcubed.content.PortalCubedItems;
import io.github.fusionflux.portalcubed.mixin.goo.CauldronInteraction$DispatcherAccessor;
import io.github.fusionflux.portalcubed.mixin.goo.CauldronInteractionsAccessor;
import io.github.fusionflux.portalcubed.mixin.goo.LavaCauldronBlockAccessor;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.cauldron.CauldronInteraction;
import net.minecraft.core.cauldron.CauldronInteractions;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Util;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.InsideBlockEffectApplier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.AbstractCauldronBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.VoxelShape;

public class GooCauldronBlock extends AbstractCauldronBlock {
	public static final CauldronInteraction.Dispatcher INTERACTIONS = Util.make(() -> {
		CauldronInteraction.Dispatcher dispatcher = CauldronInteractionsAccessor.callNewDispatcher("toxic_goo");
		CauldronInteraction$DispatcherAccessor accessor = (CauldronInteraction$DispatcherAccessor) dispatcher;
		accessor.callPut(Items.BUCKET, (state, world, pos, player, interactionHand, stack) -> CauldronInteractions.fillBucket(
				state, world, pos, player, interactionHand, stack, PortalCubedItems.GOO_BUCKET.getDefaultInstance(), _ -> true, SoundEvents.BUCKET_FILL
		));

		return dispatcher;
	});

	public GooCauldronBlock(Properties properties) {
		super(properties, INTERACTIONS);
	}

	@NotNull
	@Override
	protected ItemStack getCloneItemStack(LevelReader level, BlockPos pos, BlockState state, boolean includeData) {
		return Items.CAULDRON.getDefaultInstance();
	}

	@Override
	protected double getContentHeight(BlockState state) {
		// same as lava cauldrons
		return 0.9375;
	}

	@Override
	public boolean isFull(BlockState state) {
		return true;
	}

	@Override
	protected VoxelShape getEntityInsideCollisionShape(BlockState state, BlockGetter level, BlockPos pos, Entity entity) {
		return LavaCauldronBlockAccessor.getFILLED_SHAPE();
	}

	@Override
	protected void entityInside(BlockState state, Level level, BlockPos pos, Entity entity, InsideBlockEffectApplier effectApplier, boolean isPrecise) {
		if (level instanceof ServerLevel serverLevel) {
			GooFluid.hurt(serverLevel, entity);
		}
	}

	@Override
	protected int getAnalogOutputSignal(BlockState state, Level level, BlockPos pos, Direction direction) {
		return 3;
	}
}
