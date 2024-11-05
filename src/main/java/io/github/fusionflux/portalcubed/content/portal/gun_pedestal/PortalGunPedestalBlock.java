package io.github.fusionflux.portalcubed.content.portal.gun_pedestal;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.mojang.serialization.MapCodec;

import io.github.fusionflux.portalcubed.content.PortalCubedBlockEntityTypes;
import io.github.fusionflux.portalcubed.framework.block.TickableBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;

public class PortalGunPedestalBlock extends BaseEntityBlock implements EntityBlock {
	public static final MapCodec<PortalGunPedestalBlock> CODEC = simpleCodec(PortalGunPedestalBlock::new);

	public static final BooleanProperty EXTENDED = BlockStateProperties.EXTENDED;

	public PortalGunPedestalBlock(Properties properties) {
		super(properties);
		registerDefaultState(defaultBlockState().setValue(EXTENDED, false));
	}

	@Override
	@NotNull
	protected MapCodec<PortalGunPedestalBlock> codec() {
		return CODEC;
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(EXTENDED);
	}

	@Override
	@NotNull
	public RenderShape getRenderShape(BlockState state) {
		return RenderShape.MODEL;
	}

	@Nullable
	@Override
	public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		return new PortalGunPedestalBlockEntity(pos, state);
	}

	@Nullable
	@Override
	public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level world, BlockState state, BlockEntityType<T> type) {
		return createTickerHelper(type, PortalCubedBlockEntityTypes.PORTAL_GUN_PEDESTAL, TickableBlockEntity::tick);
	}
}
