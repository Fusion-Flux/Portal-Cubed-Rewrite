package io.github.fusionflux.portalcubed.content.crowbar;

import io.github.fusionflux.portalcubed.content.PortalCubedParticles;
import io.github.fusionflux.portalcubed.content.PortalCubedSounds;
import io.github.fusionflux.portalcubed.content.misc.BulletHoleMaterial;
import io.github.fusionflux.portalcubed.data.tags.PortalCubedBlockTags;
import io.github.fusionflux.portalcubed.framework.item.DirectClickItem;
import io.github.fusionflux.portalcubed.packet.PortalCubedPackets;
import io.github.fusionflux.portalcubed.packet.clientbound.SimpleParticlePacket;
import io.github.fusionflux.portalcubed.packet.serverbound.CrowbarSwingPacket;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import org.jetbrains.annotations.Nullable;
import org.quiltmc.qsl.base.api.util.TriState;
import org.quiltmc.qsl.networking.api.PlayerLookup;

public class CrowbarItem extends Item implements DirectClickItem {
	public CrowbarItem(Properties settings) {
		super(settings);
	}

	public void onSwing(Player player, @Nullable HitResult hit, boolean didSwingAnim) {
		player.playSound(PortalCubedSounds.CROWBAR_SWING);
		Level world = player.level();
		if (!didSwingAnim) player.swing(InteractionHand.MAIN_HAND, !world.isClientSide);

		if (player instanceof ServerPlayer serverPlayer) {
			if (!(hit instanceof BlockHitResult blockHit)) return;
			BlockState state = world.getBlockState(blockHit.getBlockPos());
			if (!state.is(PortalCubedBlockTags.CROWBAR_MAKES_HOLES))
				return;
			BulletHoleMaterial.forState(state).ifPresent(material -> {
				Vec3 location = hit.getLocation();
				world.playSound(null, location.x, location.y, location.z, material.impactSound, player.getSoundSource());
				Direction dir = blockHit.getDirection();
				SimpleParticlePacket packet = new SimpleParticlePacket(PortalCubedParticles.BULLET_HOLE, location.x, location.y, location.z, dir.getStepX(), dir.getStepY(), dir.getStepZ());
				for (ServerPlayer tracking : PlayerLookup.tracking(serverPlayer.serverLevel(), blockHit.getBlockPos())) {
					PortalCubedPackets.sendToClient(tracking, packet);
				}
			});
		} else if (player.isLocalPlayer()) {
			PortalCubedPackets.sendToServer(new CrowbarSwingPacket(hit, didSwingAnim));
		}
	}

	@Override
	public TriState onAttack(Level level, Player player, ItemStack stack, @Nullable HitResult hitResult) {
		this.onSwing(player, hitResult, false);
		return TriState.FALSE;
	}

	@Override
	public TriState onUse(Level level, Player player, ItemStack stack, @Nullable HitResult hitResult, InteractionHand hand) {
		return TriState.DEFAULT;
	}

	@Override
	public boolean mineBlock(ItemStack stack, Level world, BlockState state, BlockPos pos, LivingEntity miner) {
		if (state.getDestroySpeed(world, pos) != 0.0F) {
			stack.hurtAndBreak(2, miner, e -> e.broadcastBreakEvent(EquipmentSlot.MAINHAND));
		}
		return true;
	}

	@Override
	public boolean canAttackBlock(BlockState state, Level world, BlockPos pos, Player miner) {
		return !miner.isCreative();
	}
}
