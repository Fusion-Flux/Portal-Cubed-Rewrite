package io.github.fusionflux.portalcubed.content.decoration.signage.large;

import org.jetbrains.annotations.Nullable;

import io.github.fusionflux.portalcubed.content.PortalCubedBlockEntityTypes;
import io.github.fusionflux.portalcubed.content.PortalCubedBlocks;
import io.github.fusionflux.portalcubed.content.PortalCubedDataComponents;
import io.github.fusionflux.portalcubed.content.decoration.signage.Signage;
import io.github.fusionflux.portalcubed.content.decoration.signage.SignageBlockEntity;
import io.github.fusionflux.portalcubed.content.decoration.signage.component.SelectedLargeSignage;
import io.github.fusionflux.portalcubed.framework.model.dynamictexture.DynamicTextureRenderData;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

public class LargeSignageBlockEntity extends SignageBlockEntity {
	private static final String IMAGE_KEY = "image";

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
	protected void saveAdditional(ValueOutput output) {
		output.store(IMAGE_KEY, Signage.LARGE_CODEC, this.getImage());
	}

	@Override
	protected void loadAdditional(ValueInput input) {
		input.read(IMAGE_KEY, Signage.LARGE_CODEC).ifPresent(image -> {
			this.image = image;
			this.updateImage();
		});
	}

	@Override
	protected void applyImplicitComponents(DataComponentGetter components) {
		SelectedLargeSignage component = components.get(PortalCubedDataComponents.SELECTED_LARGE_SIGNAGE);
		if (component != null)
			this.image = component.image();
	}

	@Override
	protected void collectImplicitComponents(DataComponentMap.Builder components) {
		components.set(PortalCubedDataComponents.SELECTED_LARGE_SIGNAGE, new SelectedLargeSignage(this.getImage()));
	}

	@SuppressWarnings("deprecation")
	@Override
	public void removeComponentsFromTag(ValueOutput output) {
		output.discard(IMAGE_KEY);
	}

	@Override
	@Nullable
	public Object getRenderData() {
		DynamicTextureRenderData.Builder builder = DynamicTextureRenderData.builder();
		this.getImage().value()
				.selectTexture(this.aged)
				.ifPresent(texture -> builder.set("#signage", texture));
		return builder.build();
	}
}
