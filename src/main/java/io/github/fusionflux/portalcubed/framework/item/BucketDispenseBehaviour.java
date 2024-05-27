package io.github.fusionflux.portalcubed.framework.item;

import net.minecraft.core.BlockPos;
import net.minecraft.core.dispenser.BlockSource;
import net.minecraft.core.dispenser.DefaultDispenseItemBehavior;
import net.minecraft.world.item.DispensibleContainerItem;
import net.minecraft.world.item.ItemStack;

import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.DispenserBlock;

import org.jetbrains.annotations.NotNull;

public class BucketDispenseBehaviour extends DefaultDispenseItemBehavior {
	// Copied from vanilla with different formatting because it's stuck in the static initializer of DispenseItemBehaviour
	@Override
	@NotNull
	protected ItemStack execute(BlockSource source, ItemStack stack) {
		BlockPos pos = source.pos().relative(source.state().getValue(DispenserBlock.FACING));
		Level world = source.level();
		if (stack.getItem() instanceof DispensibleContainerItem bucket && bucket.emptyContents(null, world, pos, null)) {
			bucket.checkExtraContent(null, world, stack, pos);
			return Items.BUCKET.getDefaultInstance();
		}
		return super.execute(source, stack);
	}
}
