package io.github.fusionflux.portalcubed.content.prop;

import org.jetbrains.annotations.NotNull;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.dispenser.BlockSource;
import net.minecraft.core.dispenser.DefaultDispenseItemBehavior;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraft.world.level.block.state.BlockState;

public class PropDispenseBehavior extends DefaultDispenseItemBehavior {

	private final PropItem item;

	public PropDispenseBehavior(PropItem item) {
		this.item = item;
	}

	@Override
	@NotNull
	protected ItemStack execute(BlockSource pointer, ItemStack stack) {
		if (!stack.is(this.item))
			return stack;

		ServerLevel level = pointer.level();
		Direction direction = pointer.state().getValue(DispenserBlock.FACING);
		BlockPos pos = pointer.pos().relative(direction);
		BlockState state = level.getBlockState(pos);

		if (!state.getCollisionShape(level, pos).isEmpty())
			pos = pos.above();

		this.item.use(level, pos, direction, stack, null);
		return stack;
	}
}
