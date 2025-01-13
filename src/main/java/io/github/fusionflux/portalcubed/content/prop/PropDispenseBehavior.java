package io.github.fusionflux.portalcubed.content.prop;

import io.github.fusionflux.portalcubed.content.prop.entity.Prop;
import net.minecraft.core.BlockPos;

import net.minecraft.world.entity.EntitySpawnReason;

import net.minecraft.world.level.gameevent.GameEvent;

import org.jetbrains.annotations.NotNull;

import net.minecraft.core.Direction;
import net.minecraft.core.dispenser.BlockSource;
import net.minecraft.core.dispenser.DefaultDispenseItemBehavior;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.DispenserBlock;

public class PropDispenseBehavior extends DefaultDispenseItemBehavior {
	private final PropItem item;

	public PropDispenseBehavior(PropItem item) {
		this.item = item;
	}

	@Override
	@NotNull
	protected ItemStack execute(BlockSource source, ItemStack stack) {
		if (!stack.is(this.item))
			return stack;

		// based on DispenseItemBehavior for SpawnEggItems
		Direction direction = source.state().getValue(DispenserBlock.FACING);
		BlockPos pos = source.pos().relative(direction);
		Integer variant = PropItem.getVariant(stack);

		Prop prop = this.item.type.spawn(source.level(), pos, stack, null, EntitySpawnReason.DISPENSER, variant, direction != Direction.UP, false);

		stack.shrink(1); // intentionally not in the if statement
		if (prop != null) {
			prop.gameEvent(GameEvent.ENTITY_PLACE, null);
		}

		return stack;
	}
}
