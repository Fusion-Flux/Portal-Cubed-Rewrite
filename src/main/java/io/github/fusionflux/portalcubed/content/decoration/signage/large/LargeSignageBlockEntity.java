package io.github.fusionflux.portalcubed.content.decoration.signage.large;

import org.jetbrains.annotations.Nullable;

import io.github.fusionflux.portalcubed.content.PortalCubedBlockEntityTypes;
import io.github.fusionflux.portalcubed.content.PortalCubedBlocks;
import io.github.fusionflux.portalcubed.content.decoration.signage.SignageBlockEntity;
import io.github.fusionflux.portalcubed.framework.model.dynamictexture.DynamicTextureRenderData;
import io.github.fusionflux.portalcubed.framework.signage.Signage;
import io.github.fusionflux.portalcubed.framework.signage.SignageManager;
import net.minecraft.Optionull;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.state.BlockState;

public class LargeSignageBlockEntity extends SignageBlockEntity {
	@Nullable
	private Signage.Holder holder;

	public LargeSignageBlockEntity(BlockPos pos, BlockState state) {
		super(PortalCubedBlockEntityTypes.LARGE_SIGNAGE, pos, state, PortalCubedBlocks.AGED_LARGE_SIGNAGE);
		this.holder = SignageManager.INSTANCE.getBlank(Signage.Size.LARGE);
	}

	public Signage.Holder holder() {
		return this.holder;
	}

	public void update(Signage.Holder holder) {
		if (holder != null && (holder.id() != Optionull.map(this.holder, Signage.Holder::id))) {
			this.holder = holder;
			if (this.level != null) {
				if (!this.level.isClientSide) {
					this.sync();
				} else {
					this.updateModel();
				}
			}
		}
	}

	@Override
	public void load(CompoundTag nbt) {
		ResourceLocation signageId = ResourceLocation.tryParse(nbt.getString(SIGNAGE_KEY));
		if (signageId != null)
			this.update(SignageManager.INSTANCE.get(signageId));
	}

	@Override
	protected void saveAdditional(CompoundTag nbt) {
		if (this.holder != null)
			nbt.putString(SIGNAGE_KEY, this.holder.id().toString());
	}

	@Override
	@Nullable
	public Object getRenderData() {
		DynamicTextureRenderData.Builder builder = new DynamicTextureRenderData.Builder();
		Signage signage = Optionull.map(this.holder, Signage.Holder::value);
		if (signage != null)
			builder.put("#signage", signage.selectTexture(this.aged));
		return builder.build();
	}
}
