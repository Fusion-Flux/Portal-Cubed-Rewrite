package io.github.fusionflux.portalcubed.content.prop;

import java.util.List;
import java.util.Optional;

import net.minecraft.ChatFormatting;
import net.minecraft.Optionull;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class PropItem extends Item {
	private final PropType type;

	public PropItem(Properties settings, PropType type) {
		super(settings);
		this.type = type;
	}

	@Override
	@NotNull
	public InteractionResult useOn(UseOnContext context) {
		if (context.getLevel() instanceof ServerLevel level) {
			var clickedPos = context.getClickedPos();
			var clickedFace = context.getClickedFace();
			var state = level.getBlockState(clickedPos);
			if (!state.getCollisionShape(level, clickedPos).isEmpty())
				clickedPos = clickedPos.relative(clickedFace);

			use(level, clickedPos, clickedFace, context.getItemInHand(), context.getPlayer());
			return InteractionResult.CONSUME;
		}
		return InteractionResult.SUCCESS;
	}

	@Override
	@NotNull
	public String getDescriptionId(ItemStack stack) {
		int variant = getVariant(stack);
		return variant > 0 ? this.getDescriptionId() + "." + variant : super.getDescriptionId(stack);
	}

	@Override
	public void appendHoverText(ItemStack stack, @Nullable Level world, List<Component> tooltip, TooltipFlag context) {
		super.appendHoverText(stack, world, tooltip, context);
		if (context.isCreative() && (this.type.randomVariantOnSpawn && getVariant(stack) <= 0))
			tooltip.add(translate("tooltip.random").withStyle(ChatFormatting.GRAY));
	}

	public boolean use(ServerLevel level, BlockPos pos, Direction direction, ItemStack stack, Entity spawner) {
		int variant = getVariant(stack);
		Component customName = stack.getTagElement("display") != null ? stack.getHoverName() : null;

		if (this.type.spawn(level, pos, direction == Direction.DOWN ? this.type.dimensions.height : 0, variant, variant <= 0, customName)) {
			stack.shrink(1);
			level.gameEvent(spawner, GameEvent.ENTITY_PLACE, pos);
			return true;
		}
		return false;
	}

	public MutableComponent translate(String key) {
		return Component.translatable(this.getDescriptionId() + "." + key);
	}

	public static int getVariant(ItemStack stack) {
		return stack.hasTag() ? stack.getTag().getInt("CustomModelData") : 0;
	}
}
