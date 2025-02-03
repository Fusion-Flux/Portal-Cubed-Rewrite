package io.github.fusionflux.portalcubed.mixin.utils;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import io.github.fusionflux.portalcubed.framework.extension.BlockBehaviourPropertiesExt;
import net.minecraft.world.level.block.state.BlockBehaviour;

@Mixin(BlockBehaviour.Properties.class)
public class BlockBehaviour$PropertiesMixin implements BlockBehaviourPropertiesExt {
	@Shadow
	boolean requiresCorrectToolForDrops;

	@Override
	public BlockBehaviour.Properties pc$disableRequiresCorrectToolForDrops() {
		this.requiresCorrectToolForDrops = false;
		return (BlockBehaviour.Properties) (Object) this;
	}
}
