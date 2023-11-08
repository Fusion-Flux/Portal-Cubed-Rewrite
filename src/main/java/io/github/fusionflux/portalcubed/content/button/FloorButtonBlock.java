package io.github.fusionflux.portalcubed.content.button;

import io.github.fusionflux.portalcubed.framework.block.AbstractMultiBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;

public class FloorButtonBlock extends AbstractMultiBlock {
	public static final SizeProperties SIZE_PROPERTIES = SizeProperties.create(2, 2, 1);
	public static final BooleanProperty ACTIVE = BooleanProperty.create("active");

	public FloorButtonBlock(Properties properties) {
		super(properties);
		this.registerDefaultState(this.stateDefinition.any().setValue(ACTIVE, false));
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		super.createBlockStateDefinition(builder);
		builder.add(ACTIVE);
	}

	@Override
	public SizeProperties sizeProperties() {
		return SIZE_PROPERTIES;
	}
}
