package io.github.fusionflux.portalcubed.content.lemon;

import net.minecraft.core.Position;
import net.minecraft.core.dispenser.BlockSource;
import net.minecraft.core.dispenser.DefaultDispenseItemBehavior;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraft.world.level.block.LevelEvent;
import net.minecraft.world.phys.Vec3;

public class LemonadeDispenseBehavior extends DefaultDispenseItemBehavior {
	public static final LemonadeDispenseBehavior INSTANCE = new LemonadeDispenseBehavior();

	@Override
	protected ItemStack execute(BlockSource source, ItemStack stack) {
		ServerLevel level = source.level();
		Lemonade lemonade = new Lemonade(level, stack, null, Lemonade.MAX_ARM_TIME);
		Vec3 spawnPos = getDispensePosition(source);
		Vec3 facing = source.state().getValue(DispenserBlock.FACING).getUnitVec3();
		lemonade.doThrow(spawnPos, facing, LemonadeItem.MAX_THROW_POWER);
		level.addFreshEntity(lemonade);

		stack.shrink(1);
		return stack;
	}

	@Override
	protected void playSound(BlockSource source) {
		source.level().levelEvent(LevelEvent.SOUND_DISPENSER_PROJECTILE_LAUNCH, source.pos(), 0);
	}

	private static Vec3 getDispensePosition(BlockSource source) {
		Position pos = DispenserBlock.getDispensePosition(source);
		return pos instanceof Vec3 vec3 ? vec3 : new Vec3(pos.x(), pos.y(), pos.z());
	}
}
