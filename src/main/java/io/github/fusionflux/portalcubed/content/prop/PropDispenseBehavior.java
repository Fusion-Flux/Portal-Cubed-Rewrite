package io.github.fusionflux.portalcubed.content.prop;

import org.jetbrains.annotations.NotNull;

import net.minecraft.core.Direction;
import net.minecraft.core.dispenser.BlockSource;
import net.minecraft.core.dispenser.DefaultDispenseItemBehavior;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.DispenserBlock;

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
		this.item.use(level, pointer.pos().relative(direction), MobSpawnType.DISPENSER, direction != Direction.UP, false, stack, null);
		return stack;
	}
}
