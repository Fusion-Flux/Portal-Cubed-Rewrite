package io.github.fusionflux.portalcubed.content.decoration.signage.large;

import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.github.fusionflux.portalcubed.content.PortalCubedBlockEntityTypes;
import io.github.fusionflux.portalcubed.content.PortalCubedBlocks;
import io.github.fusionflux.portalcubed.content.PortalCubedDataComponents;
import io.github.fusionflux.portalcubed.content.decoration.signage.Signage;
import io.github.fusionflux.portalcubed.content.decoration.signage.SignageBlockEntity;
import io.github.fusionflux.portalcubed.content.decoration.signage.component.SelectedLargeSignage;
import io.github.fusionflux.portalcubed.framework.model.dynamictexture.DynamicTextureRenderData;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.RegistryOps;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class LargeSignageBlockEntity extends SignageBlockEntity {
	private static final Logger logger = LoggerFactory.getLogger(LargeSignageBlockEntity.class);

	private static final String TAG_KEY = "image";

	@Nullable
	private Holder<Signage> image;

	public LargeSignageBlockEntity(BlockPos pos, BlockState state) {
		super(PortalCubedBlockEntityTypes.LARGE_SIGNAGE, pos, state, PortalCubedBlocks.AGED_LARGE_SIGNAGE);
	}

	public Holder<Signage> getImage() {
		if (this.image == null && this.level != null) {
			return this.level.registryAccess()
					.get(Signage.LARGE_BLANK)
					.orElse(null);
		}
		return this.image;
	}

	public void setImage(Holder<Signage> image) {
		if (image != null && image != this.getImage()) {
			this.image = image;
			this.updateImage();
		}
	}

	@Override
	protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
		RegistryOps<Tag> registryOps = registries.createSerializationContext(NbtOps.INSTANCE);
		tag.put(TAG_KEY, Signage.LARGE_CODEC.encodeStart(registryOps, this.getImage()).getOrThrow());
	}

	@Override
	protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
		RegistryOps<Tag> registryOps = registries.createSerializationContext(NbtOps.INSTANCE);
		Signage.LARGE_CODEC
				.parse(registryOps, tag.get(TAG_KEY))
				.resultOrPartial(error -> logger.error("Failed to parse image: '{}'", error))
				.ifPresent(image -> {
					this.image = image;
					this.updateImage();
				});
	}

	@Override
	protected void applyImplicitComponents(BlockEntity.DataComponentInput componentInput) {
		SelectedLargeSignage component = componentInput.get(PortalCubedDataComponents.SELECTED_LARGE_SIGNAGE);
		if (component != null)
			this.image = component.image();
	}

	@Override
	protected void collectImplicitComponents(DataComponentMap.Builder components) {
		components.set(PortalCubedDataComponents.SELECTED_LARGE_SIGNAGE, new SelectedLargeSignage(this.getImage()));
	}

	@SuppressWarnings("deprecation")
	@Override
	public void removeComponentsFromTag(CompoundTag tag) {
		tag.remove(TAG_KEY);
	}

	@Override
	@Nullable
	public Object getRenderData() {
		DynamicTextureRenderData.Builder builder = new DynamicTextureRenderData.Builder();
		this.getImage().value()
				.selectTexture(this.aged)
				.ifPresent(texture -> builder.put("#signage", texture));
		return builder.build();
	}
}
