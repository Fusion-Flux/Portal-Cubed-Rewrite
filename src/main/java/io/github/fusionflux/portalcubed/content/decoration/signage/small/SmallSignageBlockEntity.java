package io.github.fusionflux.portalcubed.content.decoration.signage.small;

import java.util.EnumMap;
import java.util.Map;
import java.util.Optional;

import org.jetbrains.annotations.Nullable;

import com.mojang.serialization.Codec;

import io.github.fusionflux.portalcubed.PortalCubed;
import io.github.fusionflux.portalcubed.content.PortalCubedBlockEntityTypes;
import io.github.fusionflux.portalcubed.content.PortalCubedBlocks;
import io.github.fusionflux.portalcubed.content.PortalCubedDataComponents;
import io.github.fusionflux.portalcubed.content.PortalCubedRegistries;
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
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class SmallSignageBlockEntity extends SignageBlockEntity {
	public static final ResourceKey<Signage> SMALL_BLANK = ResourceKey.create(PortalCubedRegistries.SMALL_SIGNAGE, PortalCubed.id("blank"));

	private final Quadrants quadrants = new Quadrants();

	public SmallSignageBlockEntity(BlockPos pos, BlockState state) {
		super(PortalCubedBlockEntityTypes.SMALL_SIGNAGE, pos, state, PortalCubedBlocks.AGED_SMALL_SIGNAGE);
	}

	public Holder<Signage> getQuadrant(SmallSignageBlock.Quadrant quadrant) {
		Holder<Signage> holder = this.quadrants.get(quadrant);
		if (holder == null && this.level != null) {
			return this.level.registryAccess()
					.get(SMALL_BLANK)
					.orElse(null);
		}
		return holder;
	}

	public void updateQuadrant(SmallSignageBlock.Quadrant quadrant, Holder<Signage> holder) {
		Holder<Signage> currentHolder = this.getQuadrant(quadrant);
		if (holder != null && holder != currentHolder) {
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
			Optional.ofNullable(ResourceLocation.tryParse(quadrantsTag.getString(quadrant.name)))
					.map(id -> ResourceKey.create(PortalCubedRegistries.SMALL_SIGNAGE, id))
					.flatMap(registries::get)
					.ifPresent(signage -> this.updateQuadrant(quadrant, signage));
		}
	}

	@Override
	protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
		CompoundTag quadrantsTag = new CompoundTag();
		for (SmallSignageBlock.Quadrant quadrant : SmallSignageBlock.Quadrant.VALUES) {
			this.getQuadrant(quadrant)
					.unwrapKey()
					.ifPresent(key -> quadrantsTag.putString(quadrant.name, key.location().toString()));
		}
		tag.put(SIGNAGE_KEY, quadrantsTag);
	}

	@Override
	protected void applyImplicitComponents(BlockEntity.DataComponentInput componentInput) {
		SelectedSmallSignage component = componentInput.get(PortalCubedDataComponents.SELECTED_SMALL_SIGNAGE);
		if (component != null)
			component.quadrants().map.forEach(this::updateQuadrant);
	}

	@Override
	protected void collectImplicitComponents(DataComponentMap.Builder components) {
		components.set(PortalCubedDataComponents.SELECTED_SMALL_SIGNAGE, new SelectedSmallSignage(this.quadrants));
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
		for (SmallSignageBlock.Quadrant quadrant : SmallSignageBlock.Quadrant.VALUES) {
			this.getQuadrant(quadrant).value()
					.selectTexture(this.aged)
					.ifPresent(texture -> builder.put("#signage_" + quadrant.name, texture));
		}
		return builder.build();
	}

	public record Quadrants(Map<SmallSignageBlock.Quadrant, Holder<Signage>> map) {
		public static final Codec<Quadrants> CODEC = Codec.unboundedMap(SmallSignageBlock.Quadrant.CODEC, Signage.SMALL_CODEC).xmap(Quadrants::new, Quadrants::map);
		public static final StreamCodec<RegistryFriendlyByteBuf, Quadrants> STREAM_CODEC = PortalCubedStreamCodecs
				.map(SmallSignageBlock.Quadrant.STREAM_CODEC, Signage.SMALL_STREAM_CODEC)
				.map(Quadrants::new, Quadrants::map);

		public Quadrants() {
			this(new EnumMap<>(SmallSignageBlock.Quadrant.class));
		}

		public Holder<Signage> get(SmallSignageBlock.Quadrant quadrant) {
			return this.map.get(quadrant);
		}

		public void put(SmallSignageBlock.Quadrant quadrant, Holder<Signage> signage) {
			this.map.put(quadrant, signage);
		}
	}
}
