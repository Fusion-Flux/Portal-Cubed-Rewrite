package io.github.fusionflux.portalcubed.content.decoration.signage.large;

import java.util.Optional;

import org.jetbrains.annotations.Nullable;

import io.github.fusionflux.portalcubed.PortalCubed;
import io.github.fusionflux.portalcubed.content.PortalCubedBlockEntityTypes;
import io.github.fusionflux.portalcubed.content.PortalCubedBlocks;
import io.github.fusionflux.portalcubed.content.PortalCubedDataComponents;
import io.github.fusionflux.portalcubed.content.PortalCubedRegistries;
import io.github.fusionflux.portalcubed.content.decoration.signage.Signage;
import io.github.fusionflux.portalcubed.content.decoration.signage.SignageBlockEntity;
import io.github.fusionflux.portalcubed.content.decoration.signage.component.SelectedLargeSignage;
import io.github.fusionflux.portalcubed.framework.model.dynamictexture.DynamicTextureRenderData;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.entity.BlockEntity;
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
	protected void applyImplicitComponents(BlockEntity.DataComponentInput componentInput) {
		SelectedLargeSignage component = componentInput.get(PortalCubedDataComponents.SELECTED_LARGE_SIGNAGE);
		if (component != null)
			this.update(component.signage());
	}

	@Override
	protected void collectImplicitComponents(DataComponentMap.Builder components) {
		components.set(PortalCubedDataComponents.SELECTED_LARGE_SIGNAGE, new SelectedLargeSignage(this.holder()));
	}

	@SuppressWarnings("deprecation")
	@Override
	public void removeComponentsFromTag(CompoundTag tag) {
		tag.remove(SIGNAGE_KEY);
	}

	@Override
	@Nullable
	public Object getRenderData() {
		DynamicTextureRenderData.Builder builder = new DynamicTextureRenderData.Builder();
		this.holder().value()
				.selectTexture(this.aged)
				.ifPresent(texture -> builder.put("#signage", texture));
		return builder.build();
	}
}
