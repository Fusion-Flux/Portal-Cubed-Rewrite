package io.github.fusionflux.portalcubed.framework.extension;

import net.minecraft.world.level.block.state.BlockBehaviour;

public interface BlockBehaviourPropertiesExt {
	default BlockBehaviour.Properties pc$disableRequiresCorrectToolForDrops() {
		throw new AbstractMethodError();
	}
}
