package io.github.fusionflux.portalcubed.content.goo;

import java.util.Optional;

import org.jetbrains.annotations.NotNull;

import io.github.fusionflux.portalcubed.content.PortalCubedBlocks;
import io.github.fusionflux.portalcubed.content.PortalCubedDamageSources;
import io.github.fusionflux.portalcubed.content.PortalCubedFluids;
import io.github.fusionflux.portalcubed.content.PortalCubedGameRules;
import io.github.fusionflux.portalcubed.content.PortalCubedItems;
import io.github.fusionflux.portalcubed.content.fizzler.FizzleBehaviour;
import io.github.fusionflux.portalcubed.data.tags.PortalCubedEntityTags;
import io.github.fusionflux.portalcubed.data.tags.PortalCubedItemTags;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;

public abstract class GooFluid extends FlowingFluid {
	public static void hurt(Level world, Entity entity) {
		if (world.isClientSide || !entity.isAlive() || entity.getType().is(PortalCubedEntityTags.IMMUNE_TO_TOXIC_GOO) || (entity instanceof ItemEntity itemEntity && itemEntity.getItem().is(PortalCubedItemTags.IMMUNE_TO_TOXIC_GOO)))
			return;

		if (entity.getType().is(PortalCubedEntityTags.DISINTEGRATES_WHEN_FIZZLED)) {
			FizzleBehaviour.DISINTEGRATION.fizzle(entity);
		} else {
			entity.hurt(PortalCubedDamageSources.toxicGoo(world), world.getGameRules().getInt(PortalCubedGameRules.TOXIC_GOO_DAMAGE));
		}
	}

	@NotNull
	@Override
	public Fluid getFlowing() {
		return PortalCubedFluids.FLOWING_GOO;
	}

	@NotNull
	@Override
	public Fluid getSource() {
		return PortalCubedFluids.GOO;
	}

	@NotNull
	@Override
	public Item getBucket() {
		return PortalCubedItems.GOO_BUCKET;
	}

	@Override
	protected boolean canConvertToSource(Level world) {
		return world.getGameRules().getBoolean(PortalCubedGameRules.TOXIC_GOO_SOURCE_CONVERSION);
	}

	@Override
	protected void beforeDestroyingBlock(LevelAccessor world, BlockPos pos, BlockState state) {
		BlockEntity blockEntity = state.hasBlockEntity() ? world.getBlockEntity(pos) : null;
		Block.dropResources(state, world, pos, blockEntity);
	}

	@Override
	protected int getSlopeFindDistance(LevelReader world) {
		return 4;
	}

	@NotNull
	@Override
	protected BlockState createLegacyBlock(FluidState state) {
		return PortalCubedBlocks.GOO.defaultBlockState().setValue(LiquidBlock.LEVEL, getLegacyLevel(state));
	}

	@Override
	public boolean isSame(Fluid fluid) {
		return fluid == PortalCubedFluids.GOO || fluid == PortalCubedFluids.FLOWING_GOO;
	}

	@Override
	protected int getDropOff(LevelReader world) {
		return 1;
	}

	@Override
	public int getTickDelay(LevelReader world) {
		return 15;
	}

	@Override
	protected boolean canBeReplacedWith(FluidState state, BlockGetter world, BlockPos pos, Fluid fluid, Direction direction) {
		return direction == Direction.DOWN && !state.is(FluidTags.WATER);
	}

	@Override
	protected float getExplosionResistance() {
		return 100f;
	}

	@NotNull
	@Override
	public Optional<SoundEvent> getPickupSound() {
		return Optional.of(SoundEvents.BUCKET_FILL);
	}

	public static class Flowing extends GooFluid {
		protected void createFluidStateDefinition(StateDefinition.Builder<Fluid, FluidState> builder) {
			super.createFluidStateDefinition(builder);
			builder.add(LEVEL);
		}

		public int getAmount(FluidState state) {
			return state.getValue(LEVEL);
		}

		public boolean isSource(FluidState state) {
			return false;
		}
	}

	public static class Source extends GooFluid {
		public int getAmount(FluidState state) {
			return 8;
		}

		public boolean isSource(FluidState state) {
			return true;
		}
	}
}
