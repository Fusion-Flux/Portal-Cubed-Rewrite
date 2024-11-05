package io.github.fusionflux.portalcubed.content.portal.gun_pedestal;

import org.jetbrains.annotations.NotNull;
import org.quiltmc.qsl.block.entity.api.QuiltBlockEntity;

import io.github.fusionflux.portalcubed.content.PortalCubedBlockEntityTypes;
import io.github.fusionflux.portalcubed.framework.block.TickableBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.entity.AnimationState;
import net.minecraft.world.level.block.state.BlockState;

public class PortalGunPedestalBlockEntity extends TickableBlockEntity implements QuiltBlockEntity {
	public final AnimationState retractAnimationState = new AnimationState();
	public final AnimationState extendAnimationState = new AnimationState();
	public final AnimationState unlockAnimationState = new AnimationState();
	public final AnimationState lockAnimationState = new AnimationState();

	private boolean wasPlayerNearby;

	public PortalGunPedestalBlockEntity(BlockPos pos, BlockState state) {
		super(PortalCubedBlockEntityTypes.PORTAL_GUN_PEDESTAL, pos, state);
	}

	@Override
	public void tick() {
		if (this.level == null)
			return;

		if (!this.level.isClientSide)
			return;

		boolean playerNearby = this.level.getNearestPlayer(this.worldPosition.getX() + .5d, this.worldPosition.getY() + .5d, this.worldPosition.getZ() + .5d, 3d, false) != null;
		if (this.wasPlayerNearby != playerNearby) {
			if (playerNearby) {
				this.unlockAnimationState.start(this.tickCount);
				this.lockAnimationState.stop();
			} else {
				this.lockAnimationState.start(this.tickCount);
				this.unlockAnimationState.stop();
			}
			this.wasPlayerNearby = playerNearby;
		}
	}

	@Override
	public Packet<ClientGamePacketListener> getUpdatePacket() {
		return ClientboundBlockEntityDataPacket.create(this);
	}

	@NotNull
	@Override
	public CompoundTag getUpdateTag() {
		return this.saveWithoutMetadata();
	}
}
