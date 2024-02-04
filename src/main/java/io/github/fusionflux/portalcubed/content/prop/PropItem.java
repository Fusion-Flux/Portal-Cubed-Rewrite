package io.github.fusionflux.portalcubed.content.prop;

import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.gameevent.GameEvent;

public class PropItem extends Item {
	private final PropType type;

	public PropItem(Properties settings, PropType type) {
		super(settings);
		this.type = type;
	}

	@Override
	public InteractionResult useOn(UseOnContext context) {
		if (context.getLevel() instanceof ServerLevel level) {
			var clickedPos = context.getClickedPos();
			var clickedFace = context.getClickedFace();
			var state = level.getBlockState(clickedPos);
			if (!state.getCollisionShape(level, clickedPos).isEmpty())
				clickedPos = clickedPos.relative(clickedFace);

			var stack = context.getItemInHand();
			int variant = stack.hasTag() ? stack.getTag().getInt("CustomModelData") : -1;
			if (variant == -1)
				variant = level.random.nextInt(type.variants.length);

			if (type.spawn(level, clickedPos, clickedFace == Direction.DOWN ? type.dimensions.height : 0, variant)) {
				stack.shrink(1);
				level.gameEvent(context.getPlayer(), GameEvent.ENTITY_PLACE, clickedPos);
			}
			return InteractionResult.CONSUME;
		}
		return InteractionResult.SUCCESS;
	}
}
