package io.github.fusionflux.portalcubed.framework.block;

import net.minecraft.world.level.material.PushReaction;

import org.jetbrains.annotations.NotNull;

import com.mojang.serialization.MapCodec;

import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.MultifaceBlock;
import net.minecraft.world.level.block.state.BlockState;

public class FacadeBlock extends MultifaceBlock {
	public static final MapCodec<FacadeBlock> CODEC = simpleCodec(FacadeBlock::new);

	public FacadeBlock(Properties properties) {
		super(modifyProperties(properties));
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

	private static Properties modifyProperties(Properties properties) {
		// must be set to DESTROY to avoid dupes
		properties.pushReaction(PushReaction.DESTROY);
		// disable needing correct tool since they can just be popped off
		properties.pc$disableRequiresCorrectToolForDrops();

		return properties;
	}
}
