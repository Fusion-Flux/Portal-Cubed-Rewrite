package io.github.fusionflux.portalcubed.content.decoration.signage.small;

import java.util.EnumMap;
import java.util.Map;

import org.jetbrains.annotations.Nullable;

import io.github.fusionflux.portalcubed.content.PortalCubedBlockEntityTypes;
import io.github.fusionflux.portalcubed.content.PortalCubedBlocks;
import io.github.fusionflux.portalcubed.content.decoration.signage.SignageBlockEntity;
import io.github.fusionflux.portalcubed.framework.model.dynamictexture.DynamicTextureRenderData;
import io.github.fusionflux.portalcubed.framework.signage.Signage;
import io.github.fusionflux.portalcubed.framework.signage.SignageManager;
import net.minecraft.Optionull;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.state.BlockState;

public class SmallSignageBlockEntity extends SignageBlockEntity {
	private final EnumMap<SmallSignageBlock.Quadrant, Signage.Holder> quadrants;

	public SmallSignageBlockEntity(BlockPos pos, BlockState state) {
		super(PortalCubedBlockEntityTypes.SMALL_SIGNAGE, pos, state, PortalCubedBlocks.AGED_SMALL_SIGNAGE);
		this.quadrants = new EnumMap<>(SmallSignageBlock.Quadrant.class);
		for (SmallSignageBlock.Quadrant quadrant : SmallSignageBlock.Quadrant.VALUES) {
			this.quadrants.put(quadrant, SignageManager.INSTANCE.getBlank(Signage.Size.SMALL));
		}
	}

	public Signage.Holder getQuadrant(SmallSignageBlock.Quadrant quadrant) {
		return this.quadrants.get(quadrant);
	}

	public void updateQuadrant(SmallSignageBlock.Quadrant quadrant, Signage.Holder holder) {
		Signage.Holder currentHolder = this.getQuadrant(quadrant);
		if (holder != null && !holder.equals(currentHolder)) {
			this.quadrants.put(quadrant, holder);
			if (this.level != null) {
				if (this.level.isClientSide) {
					this.updateModel();
				} else {
					this.setChangedAndSync();
				}
			}
		}
	}

	@Override
	protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
		CompoundTag quadrantsTag = tag.getCompound(SIGNAGE_KEY);
		for (SmallSignageBlock.Quadrant quadrant : SmallSignageBlock.Quadrant.VALUES) {
			Signage.Holder signage = SignageManager.INSTANCE.get(ResourceLocation.tryParse(quadrantsTag.getString(quadrant.name)));
			if (signage != null)
				this.updateQuadrant(quadrant, signage);
		}
	}

	@Override
	protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
		CompoundTag quadrantsTag = new CompoundTag();
		for (Map.Entry<SmallSignageBlock.Quadrant, Signage.Holder> entry : this.quadrants.entrySet()) {
			quadrantsTag.putString(entry.getKey().name, entry.getValue().id().toString());
		}
		tag.put(SIGNAGE_KEY, quadrantsTag);
	}

	@Override
	@Nullable
	public Object getRenderData() {
		DynamicTextureRenderData.Builder builder = new DynamicTextureRenderData.Builder();
		for (Map.Entry<SmallSignageBlock.Quadrant, Signage.Holder> entry : this.quadrants.entrySet()) {
			Optionull.map(
					entry.getValue().value(),
					signage -> builder.put("#signage_" + entry.getKey().name, signage.selectTexture(this.aged))
			);
		}
		return builder.build();
	}
}
