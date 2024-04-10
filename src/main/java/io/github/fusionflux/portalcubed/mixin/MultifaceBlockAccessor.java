package io.github.fusionflux.portalcubed.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import net.minecraft.world.level.block.MultifaceBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BooleanProperty;

@Mixin(MultifaceBlock.class)
public interface MultifaceBlockAccessor {
	@Invoker
	static BlockState callRemoveFace(BlockState state, BooleanProperty direction) {
		throw new AssertionError();
	}
}
