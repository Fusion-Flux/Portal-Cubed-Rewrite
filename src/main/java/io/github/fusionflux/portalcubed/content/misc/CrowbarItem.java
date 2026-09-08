package io.github.fusionflux.portalcubed.content.misc;

import org.jspecify.annotations.Nullable;

import io.github.fusionflux.portalcubed.content.PortalCubedGameEvents;
import io.github.fusionflux.portalcubed.content.PortalCubedParticles;
import io.github.fusionflux.portalcubed.content.PortalCubedSounds;
import io.github.fusionflux.portalcubed.data.tags.PortalCubedBlockTags;
import io.github.fusionflux.portalcubed.framework.item.AttackListeningItem;
import io.github.fusionflux.portalcubed.framework.item.CreativeNonBlockBreakingItem;
import io.github.fusionflux.portalcubed.packet.PortalCubedPackets;
import io.github.fusionflux.portalcubed.packet.clientbound.SimpleParticlePacket;
import io.github.fusionflux.portalcubed.packet.serverbound.CrowbarSwingPacket;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stats;
import net.minecraft.util.TriState;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent.Context;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public class CrowbarItem extends CreativeNonBlockBreakingItem implements AttackListeningItem {
	public CrowbarItem(Properties settings) {
		super(settings);
	}

	public void onSwing(Player player, ItemStack stack, @Nullable HitResult hit, boolean didSwingAnim) {
		if (player.isSpectator())
			return;

		player.playSound(PortalCubedSounds.CROWBAR_SWING);
		Level level = player.level();
		if (!didSwingAnim) {
			player.swing(InteractionHand.MAIN_HAND, stack.getAttackAnimation(), !level.isClientSide());
		}

		if (player instanceof ServerPlayer serverPlayer) {
			player.awardStat(Stats.ITEM_USED.get(this));

			if (!(hit instanceof BlockHitResult blockHit))
				return;

			BlockState state = level.getBlockState(blockHit.getBlockPos());
			Vec3 pos = hit.getLocation();
			level.gameEvent(PortalCubedGameEvents.CROWBAR_HIT, pos, new Context(player, state));

			if (!state.is(PortalCubedBlockTags.CROWBAR_MAKES_HOLES))
				return;

			BulletHoleMaterial.forState(state).ifPresent(material -> {
				level.playSound(null, pos.x, pos.y, pos.z, material.impactSound, player.getSoundSource());
				Direction dir = blockHit.getDirection();
				SimpleParticlePacket packet = new SimpleParticlePacket(PortalCubedParticles.BULLET_HOLE, pos.x, pos.y, pos.z, dir.getStepX(), dir.getStepY(), dir.getStepZ());
				for (ServerPlayer tracking : PlayerLookup.tracking(serverPlayer.level(), blockHit.getBlockPos())) {
					PortalCubedPackets.sendToClient(tracking, packet);
				}
			});
		} else if (player.isLocalPlayer()) {
			PortalCubedPackets.sendToServer(new CrowbarSwingPacket(hit, didSwingAnim));
		}
	}

	@Override
	public TriState onAttack(Level level, Player player, ItemStack stack, @Nullable HitResult hitResult) {
		this.onSwing(player, stack, hitResult, false);
		return TriState.DEFAULT;
	}
}
