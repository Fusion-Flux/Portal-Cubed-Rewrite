package io.github.fusionflux.portalcubed.content.decoration.signage.large;

import java.util.Optional;

import io.github.fusionflux.portalcubed.PortalCubed;
import io.github.fusionflux.portalcubed.content.PortalCubedRegistries;

import org.jetbrains.annotations.Nullable;

import io.github.fusionflux.portalcubed.content.PortalCubedBlockEntityTypes;
import io.github.fusionflux.portalcubed.content.PortalCubedBlocks;
import io.github.fusionflux.portalcubed.content.decoration.signage.SignageBlockEntity;
import io.github.fusionflux.portalcubed.framework.model.dynamictexture.DynamicTextureRenderData;
import io.github.fusionflux.portalcubed.framework.signage.Signage;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.state.BlockState;

public class LargeSignageBlockEntity extends SignageBlockEntity {
	public static final ResourceKey<Signage> LARGE_BLANK = ResourceKey.create(PortalCubedRegistries.LARGE_SIGNAGE, PortalCubed.id("blank"));

	@Nullable
	private Holder<Signage> holder;

	public LargeSignageBlockEntity(BlockPos pos, BlockState state) {
		super(PortalCubedBlockEntityTypes.LARGE_SIGNAGE, pos, state, PortalCubedBlocks.AGED_LARGE_SIGNAGE);
	}

	public Holder<Signage> holder() {
		if (this.holder == null && this.level != null) {
			return this.level.registryAccess()
					.get(LARGE_BLANK)
					.orElse(null);
		}
		return this.holder;
	}

	public void update(Holder<Signage> holder) {
		if (holder != null && holder != this.holder()) {
			this.holder = holder;
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
		Optional.ofNullable(ResourceLocation.tryParse(tag.getString(SIGNAGE_KEY)))
				.map(id -> ResourceKey.create(PortalCubedRegistries.LARGE_SIGNAGE, id))
				.flatMap(registries::get)
				.ifPresent(this::update);
	}

	@Override
	protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
		this.holder().unwrapKey().ifPresent(key -> tag.putString(SIGNAGE_KEY, key.location().toString()));
	}

	@Override
	@Nullable
	public Object getRenderData() {
		DynamicTextureRenderData.Builder builder = new DynamicTextureRenderData.Builder();
		builder.put("#signage", this.holder().value().selectTexture(this.aged));
		return builder.build();
	}
}
