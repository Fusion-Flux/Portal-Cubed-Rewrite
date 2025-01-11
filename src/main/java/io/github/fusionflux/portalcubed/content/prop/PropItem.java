package io.github.fusionflux.portalcubed.content.prop;

import java.util.List;
import java.util.Optional;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import io.github.fusionflux.portalcubed.content.PortalCubedDataComponents;
import net.minecraft.ChatFormatting;
import net.minecraft.Optionull;
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
	private final PropType type;

	public PropItem(Properties settings, PropType type) {
		super(settings);
		this.type = type;
	}

	@Override
	@NotNull
	public InteractionResult useOn(UseOnContext context) {
		if (context.getLevel() instanceof ServerLevel world) {
			BlockPos clickedPos = context.getClickedPos();
			Direction clickedFace = context.getClickedFace();
			BlockState state = world.getBlockState(clickedPos);
			// TODO: rework this when the game launches
//			boolean invertY = false;
//			if (!state.getCollisionShape(world, clickedPos).isEmpty()) {
//				clickedPos = clickedPos.relative(clickedFace);
//				invertY = clickedFace == Direction.UP;
//			}

			this.use(world, clickedPos, context.getItemInHand(), context.getPlayer());
			return InteractionResult.SUCCESS;
		}
		return InteractionResult.SUCCESS;
	}

	@Override
	@NotNull
	public Component getName(ItemStack stack) {
		return getVariant(stack)
				.map(variant -> (Component) Component.translatable(this.getDescriptionId() + "." + variant))
				.orElseGet(() -> super.getName(stack));
	}

	@Override
	public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag flag) {
		super.appendHoverText(stack, context, tooltipComponents, flag);
		if (flag.isCreative() && (this.type.randomVariantOnSpawn && getVariant(stack).isEmpty()))
			tooltipComponents.add(translate("tooltip.random").withStyle(ChatFormatting.GRAY));
	}

	public boolean use(ServerLevel world, BlockPos pos, ItemStack stack, @Nullable Player spawner) {
		Optional<Integer> maybeVariant = getVariant(stack);
		return Optionull.mapOrDefault(
				this.type.spawn(world, pos, stack, spawner, maybeVariant.orElse(0), maybeVariant.isEmpty()),
				prop -> {
					stack.shrink(1);
					prop.gameEvent(GameEvent.ENTITY_PLACE, spawner);
					return true;
				},
				false
		);
	}

	public MutableComponent translate(String key) {
		return Component.translatable(this.getDescriptionId() + "." + key);
	}

	public static Optional<Integer> getVariant(ItemStack stack) {
		return Optional.ofNullable(stack.get(PortalCubedDataComponents.PROP_VARIANT));
	}
}
