package io.github.fusionflux.portalcubed.content.fizzler.tool;

import java.util.function.Predicate;

import io.github.fusionflux.portalcubed.content.PortalCubedItems;
import io.github.fusionflux.portalcubed.data.tags.PortalCubedEntityTags;
import io.github.fusionflux.portalcubed.framework.raycast.RaycastOptions;
import io.github.fusionflux.portalcubed.framework.raycast.RaycastResult;
import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public class FizzleinatorItem extends Item {
	public static final Predicate<Entity> CAN_BE_FIZZLED = EntitySelector.CAN_BE_PICKED.and(
			entity -> !entity.getType().is(PortalCubedEntityTags.IMMUNE_TO_DISINTEGRATION)
	);

	public static final RaycastOptions OPTIONS = RaycastOptions.DEFAULT.edit()
			.entities(CAN_BE_FIZZLED)
			.portals(RaycastOptions.PortalMode.HIT)
			.build();

	public FizzleinatorItem(Properties properties) {
		super(properties);
	}

	@Override
	public InteractionResult use(Level level, Player player, InteractionHand hand) {
		Vec3 start = player.getEyePosition();
		Vec3 direction = player.calculateViewVector(player.getXRot(), player.getYRot());
		double distance = player.blockInteractionRange();

		RaycastResult result = OPTIONS.edit().forPlayer(player).build().raycast(level, start, direction, distance);

		// only handle portals here, entities and blocks use the standard methods
		if (result instanceof RaycastResult.Portal portal) {
			if (level instanceof ServerLevel serverLevel) {
				serverLevel.portalManager().remove(portal.portal);
			}

			return InteractionResult.SUCCESS;
		}

		return super.use(level, player, hand);
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

			ItemStack held = player.getItemInHand(hand);
			if (held.is(PortalCubedItems.FIZZLEINATOR)) {
				entity.pc$disintegrate();
				return InteractionResult.SUCCESS;
			}

			return InteractionResult.PASS;
		});
	}
}
