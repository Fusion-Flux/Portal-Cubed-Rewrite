package io.github.fusionflux.portalcubed.content.prop;

import java.util.Optional;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
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

			use(level, clickedPos, clickedFace, context.getItemInHand(), context.getPlayer());
			return InteractionResult.SUCCESS;
		}
		return InteractionResult.CONSUME;
	}

	public boolean use(ServerLevel level, BlockPos pos, Direction direction, ItemStack stack, Entity spawner) {
		int variant = stack.hasTag() ? stack.getTag().getInt("CustomModelData") : 0;
		Optional<Component> customName = stack.getTagElement("display") != null ? Optional.of(stack.getHoverName()) : Optional.empty();

		if (type.spawn(level, pos, direction == Direction.DOWN ? type.dimensions.height : 0, variant, variant <= 0, customName)) {
			stack.shrink(1);
			level.gameEvent(spawner, GameEvent.ENTITY_PLACE, pos);
			return true;
		}
		return false;
	}

	@Override
	public String getDescriptionId(ItemStack stack) {
		int variant = stack.hasTag() ? stack.getTag().getInt("CustomModelData") : 0;
		if (variant == 0)
			return super.getDescriptionId(stack);
		return String.format("%s.%d", super.getDescriptionId(stack), variant);
	}
}
