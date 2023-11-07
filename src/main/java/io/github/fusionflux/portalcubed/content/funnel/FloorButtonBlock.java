package io.github.fusionflux.portalcubed.content.funnel;

import io.github.fusionflux.portalcubed.framework.block.AbstractMultiBlock;

public class FloorButtonBlock extends AbstractMultiBlock {
	public static final SizeProperties SIZE_PROPERTIES = SizeProperties.create(2, 2, 1);

	public FloorButtonBlock(Properties properties) {
		super(properties);
	}

	@Override
	public SizeProperties sizeProperties() {
		return SIZE_PROPERTIES;
	}
}
