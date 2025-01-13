package io.github.fusionflux.portalcubed.content.prop;

import java.util.List;

import io.github.fusionflux.portalcubed.content.prop.entity.Prop;

import net.minecraft.world.entity.EntitySpawnReason;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import io.github.fusionflux.portalcubed.content.PortalCubedDataComponents;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;

public class PropItem extends Item {
	public final PropType type;

	public PropItem(Properties settings, PropType type) {
		super(settings);
		this.type = type;
	}

	@Override
	@NotNull
	public InteractionResult useOn(UseOnContext context) {
		if (!(context.getLevel() instanceof ServerLevel level))
			return InteractionResult.SUCCESS;

		// based on SpawnEggItem
		ItemStack held = context.getItemInHand();
		BlockPos pos = context.getClickedPos();
		Direction face = context.getClickedFace();
		BlockState state = level.getBlockState(pos);
		Player player = context.getPlayer();

		BlockPos effectivePos = pos;
		if (!state.getCollisionShape(level, pos).isEmpty()) {
			effectivePos = pos.relative(face);
		}

		boolean offsetYMore = !pos.equals(effectivePos) && face == Direction.UP;

		Integer variant = getVariant(held);
		Prop prop = this.type.spawn(level, effectivePos, held, player, EntitySpawnReason.SPAWN_ITEM_USE, variant, true, offsetYMore);
		if (prop != null) {
			held.shrink(1);
			prop.gameEvent(GameEvent.ENTITY_PLACE, player);
		}

		return InteractionResult.SUCCESS;
	}

	@Override
	@NotNull
	public Component getName(ItemStack stack) {
		Integer variant = getVariant(stack);
		return variant == null ? super.getName(stack) : Component.translatable(this.getDescriptionId() + "." + variant);
	}

	@Override
	public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag flag) {
		super.appendHoverText(stack, context, tooltipComponents, flag);
		if (flag.isCreative() && (this.type.randomVariantOnSpawn && getVariant(stack) == null))
			tooltipComponents.add(this.translate("tooltip.random").withStyle(ChatFormatting.GRAY));
	}

	private MutableComponent translate(String key) {
		return Component.translatable(this.getDescriptionId() + "." + key);
	}

	@Nullable
	public static Integer getVariant(ItemStack stack) {
		return stack.get(PortalCubedDataComponents.PROP_VARIANT);
	}
}
