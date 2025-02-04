package io.github.fusionflux.portalcubed.content.decoration.signage.small;

import java.util.EnumMap;
import java.util.Map;

import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mojang.serialization.Codec;

import io.github.fusionflux.portalcubed.content.PortalCubedBlockEntityTypes;
import io.github.fusionflux.portalcubed.content.PortalCubedBlocks;
import io.github.fusionflux.portalcubed.content.PortalCubedDataComponents;
import io.github.fusionflux.portalcubed.content.decoration.signage.Signage;
import io.github.fusionflux.portalcubed.content.decoration.signage.SignageBlockEntity;
import io.github.fusionflux.portalcubed.content.decoration.signage.component.SelectedSmallSignage;
import io.github.fusionflux.portalcubed.framework.model.dynamictexture.DynamicTextureRenderData;
import io.github.fusionflux.portalcubed.framework.util.PortalCubedStreamCodecs;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.RegistryOps;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class SmallSignageBlockEntity extends SignageBlockEntity {
	private static final Logger logger = LoggerFactory.getLogger(SmallSignageBlockEntity.class);

	private static final String TAG_KEY = "quadrants";

	private Quadrants quadrants = new Quadrants();

	public SmallSignageBlockEntity(BlockPos pos, BlockState state) {
		super(PortalCubedBlockEntityTypes.SMALL_SIGNAGE, pos, state, PortalCubedBlocks.AGED_SMALL_SIGNAGE);
	}

	public Holder<Signage> getQuadrantImage(SmallSignageBlock.Quadrant quadrant) {
		Holder<Signage> holder = this.quadrants.get(quadrant);
		if (holder == null && this.level != null) {
			return this.level.registryAccess()
					.get(Signage.SMALL_BLANK)
					.orElse(null);
		}
		return holder;
	}

	public void setQuadrantImage(SmallSignageBlock.Quadrant quadrant, Holder<Signage> image) {
		if (image != null && image != this.getQuadrantImage(quadrant)) {
			this.quadrants.put(quadrant, image);
			this.updateImage();
		}
	}

	@Override
	protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
		RegistryOps<Tag> registryOps = registries.createSerializationContext(NbtOps.INSTANCE);
		tag.put(TAG_KEY, Quadrants.CODEC.encodeStart(registryOps, this.quadrants).getOrThrow());
	}

	@Override
	protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
		RegistryOps<Tag> registryOps = registries.createSerializationContext(NbtOps.INSTANCE);
		Quadrants.CODEC
				.parse(registryOps, tag.get(TAG_KEY))
				.resultOrPartial(error -> logger.error("Failed to parse image: '{}'", error))
				.ifPresent(quadrants -> {
					this.quadrants = quadrants;
					this.updateImage();
				});
	}

	@Override
	protected void applyImplicitComponents(BlockEntity.DataComponentInput componentInput) {
		SelectedSmallSignage component = componentInput.get(PortalCubedDataComponents.SELECTED_SMALL_SIGNAGE);
		if (component != null)
			this.quadrants = component.quadrants();
	}

	@Override
	protected void collectImplicitComponents(DataComponentMap.Builder components) {
		components.set(PortalCubedDataComponents.SELECTED_SMALL_SIGNAGE, new SelectedSmallSignage(this.quadrants));
	}

	@SuppressWarnings("deprecation")
	@Override
	public void removeComponentsFromTag(CompoundTag tag) {
		tag.remove(TAG_KEY);
	}

	@Override
	@Nullable
	public Object getRenderData() {
		DynamicTextureRenderData.Builder builder = DynamicTextureRenderData.builder();
		for (SmallSignageBlock.Quadrant quadrant : SmallSignageBlock.Quadrant.VALUES) {
			this.getQuadrantImage(quadrant).value()
					.selectTexture(this.aged)
					.ifPresent(texture -> builder.set("#signage_" + quadrant.name, texture));
		}
		return builder.build();
	}

	public record Quadrants(EnumMap<SmallSignageBlock.Quadrant, Holder<Signage>> map) {
		public static final Codec<Quadrants> CODEC = Codec.unboundedMap(SmallSignageBlock.Quadrant.CODEC, Signage.SMALL_CODEC).xmap(Quadrants::new, Quadrants::map);
		public static final StreamCodec<RegistryFriendlyByteBuf, Quadrants> STREAM_CODEC = PortalCubedStreamCodecs
				.map(SmallSignageBlock.Quadrant.STREAM_CODEC, Signage.SMALL_STREAM_CODEC)
				.map(Quadrants::new, Quadrants::map);

		public Quadrants() {
			this(new EnumMap<>(SmallSignageBlock.Quadrant.class));
		}

		public Quadrants(Map<SmallSignageBlock.Quadrant, Holder<Signage>> map) {
			this(map.isEmpty() ? new EnumMap<>(SmallSignageBlock.Quadrant.class) : new EnumMap<>(map));
		}

		public Holder<Signage> get(SmallSignageBlock.Quadrant quadrant) {
			return this.map.get(quadrant);
		}

		public void put(SmallSignageBlock.Quadrant quadrant, Holder<Signage> signage) {
			this.map.put(quadrant, signage);
		}
	}
}
