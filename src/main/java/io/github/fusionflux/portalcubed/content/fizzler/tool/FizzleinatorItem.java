package io.github.fusionflux.portalcubed.content.fizzler.tool;

import io.github.fusionflux.portalcubed.content.PortalCubedItems;
import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.state.BlockState;

public class FizzleinatorItem extends Item {
	public FizzleinatorItem(Properties properties) {
		super(properties);
	}

	@Override
	public InteractionResult useOn(UseOnContext context) {
		Player player = context.getPlayer();
		if (player == null || !player.getAbilities().instabuild)
			return InteractionResult.PASS;

		if (context.getLevel() instanceof ServerLevel level) {
			fizzleBlock(level, context.getClickedPos());
		}

		return InteractionResult.SUCCESS;
	}

	public static void fizzleBlock(ServerLevel level, BlockPos pos) {
		BlockState state = level.getBlockState(pos);
		if (!state.isAir()) {
			FallingBlockEntity.fall(level, pos, state).pc$disintegrate();
		}
	}

	public static void registerEventListeners() {
		UseEntityCallback.EVENT.register((player, level, hand, entity, hit) -> {
			if (player.isSecondaryUseActive() || level.isClientSide || !player.getAbilities().instabuild)
				return InteractionResult.PASS;

			ItemStack held = player.getItemInHand(hand);
			if (held.is(PortalCubedItems.FIZZLEINATOR)) {
				entity.pc$disintegrate();
				return InteractionResult.SUCCESS;
			}

			return InteractionResult.PASS;
		});
	}
}
