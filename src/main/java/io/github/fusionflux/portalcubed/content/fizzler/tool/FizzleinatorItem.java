package io.github.fusionflux.portalcubed.content.fizzler.tool;

import io.github.fusionflux.portalcubed.content.PortalCubedItems;
import io.github.fusionflux.portalcubed.content.portal.interaction.UsableOnPortals;
import io.github.fusionflux.portalcubed.content.portal.ref.PortalReference;
import io.github.fusionflux.portalcubed.data.tags.PortalCubedEntityTags;
import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.state.BlockState;

public class FizzleinatorItem extends Item implements UsableOnPortals {
	public FizzleinatorItem(Properties properties) {
		super(properties);
	}

	@Override
	public InteractionResult useOn(UseOnContext context) {
		Player player = context.getPlayer();
		if (player == null || !canFizzle(player))
			return InteractionResult.PASS;

		if (context.getLevel() instanceof ServerLevel level) {
			fizzleBlock(level, context.getClickedPos());
		}

		return InteractionResult.SUCCESS;
	}

	@Override
	public InteractionResult useOnPortal(Player user, PortalReference portal, ItemStack stack, InteractionHand hand) {
		if (!canFizzle(user)) {
			return InteractionResult.PASS;
		} else if (user.level() instanceof ServerLevel level) {
			level.portalManager().remove(portal);
			return InteractionResult.SUCCESS_SERVER;
		}

		return InteractionResult.SUCCESS;
	}

	public static void fizzleBlock(ServerLevel level, BlockPos pos) {
		BlockState state = level.getBlockState(pos);
		if (!state.isAir()) {
			FallingBlockEntity.fall(level, pos, state).pc$disintegrate();
		}
	}

	private static boolean canFizzle(Player user) {
		return user.getAbilities().instabuild;
	}

	public static void registerEventListeners() {
		UseEntityCallback.EVENT.register((player, level, hand, entity, hit) -> {
			if (player.isSecondaryUseActive() || level.isClientSide || !canFizzle(player) || player.isSpectator())
				return InteractionResult.PASS;

			if (entity.getType().is(PortalCubedEntityTags.IMMUNE_TO_DISINTEGRATION))
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
