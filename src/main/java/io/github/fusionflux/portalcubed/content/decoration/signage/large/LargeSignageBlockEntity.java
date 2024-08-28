package io.github.fusionflux.portalcubed.content.decoration.signage.large;

import io.github.fusionflux.portalcubed.content.PortalCubedBlockEntityTypes;
import io.github.fusionflux.portalcubed.framework.model.dynamictexture.DynamicTextureRenderData;
import io.github.fusionflux.portalcubed.framework.signage.Signage;
import io.github.fusionflux.portalcubed.framework.signage.SignageManager;
import net.minecraft.Optionull;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;

import net.minecraft.world.level.block.state.BlockState;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.quiltmc.qsl.block.entity.api.QuiltBlockEntity;

public class LargeSignageBlockEntity extends BlockEntity implements QuiltBlockEntity {
	public static final String SIGNAGE_KEY = "signage";

	@Nullable
	private Signage.Holder holder;

	public LargeSignageBlockEntity(BlockPos pos, BlockState state) {
		super(PortalCubedBlockEntityTypes.LARGE_SIGNAGE_PANEL, pos, state);
		this.holder = SignageManager.INSTANCE.getBlank(Signage.Size.LARGE);
	}

	private void updateSignage(Signage.Holder holder) {
		if (holder != null && (holder.id() != Optionull.map(this.holder, Signage.Holder::id))) {
			this.holder = holder;
			if (this.level != null && this.level.isClientSide) {
				BlockState state = this.getBlockState();
				this.level.sendBlockUpdated(this.worldPosition, state, state, Block.UPDATE_IMMEDIATE);
			}
		}
	}

	@Override
	public void load(CompoundTag nbt) {
		ResourceLocation signageId = ResourceLocation.tryParse(nbt.getString(SIGNAGE_KEY));
		if (signageId != null)
			this.updateSignage(SignageManager.INSTANCE.get(signageId));
	}

	@Override
	protected void saveAdditional(CompoundTag nbt) {
		if (this.holder != null)
			nbt.putString(SIGNAGE_KEY, this.holder.id().toString());
	}

	@Override
	public Packet<ClientGamePacketListener> getUpdatePacket() {
		return ClientboundBlockEntityDataPacket.create(this);
	}

	@NotNull
	@Override
	public CompoundTag getUpdateTag() {
		return saveWithoutMetadata();
	}

	@Override
	@Nullable
	public Object getRenderData() {
		DynamicTextureRenderData.Builder builder = new DynamicTextureRenderData.Builder();
		Signage signage = Optionull.map(this.holder, Signage.Holder::value);
		if (signage != null)
			builder.put("#signage", signage.selectTexture(false));
		return builder.build();
	}
}
