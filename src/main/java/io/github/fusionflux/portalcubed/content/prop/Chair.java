package io.github.fusionflux.portalcubed.content.prop;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

public class Chair extends Prop {
	public Chair(PropType type, EntityType<?> entityType, Level level) {
		super(type, entityType, level);
	}

	@Override
	public InteractionResult interact(Player player, InteractionHand hand) {
		var level = level();
		if (!isVehicle()) {
			if (!level.isClientSide) player.startRiding(this);
			return InteractionResult.sidedSuccess(level.isClientSide);
		}
		return super.interact(player, hand);
	}
}
