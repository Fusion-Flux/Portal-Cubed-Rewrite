package io.github.fusionflux.portalcubed.content.prop.renderer;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.mojang.serialization.MapCodec;

import io.github.fusionflux.portalcubed.content.prop.PropItem;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.item.properties.numeric.RangeSelectItemModelProperty;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

public enum PropVariantProperty implements RangeSelectItemModelProperty {
	INSTANCE;

	public static final MapCodec<PropVariantProperty> MAP_CODEC = MapCodec.unit(INSTANCE);

	@Override
	public float get(ItemStack stack, @Nullable ClientLevel level, @Nullable LivingEntity entity, int seed) {
		Integer propVariant = PropItem.getVariant(stack);
		if (propVariant != null)
			return propVariant;
		return 0;
	}

	@Override
	@NotNull
	public MapCodec<PropVariantProperty> type() {
		return MAP_CODEC;
	}
}
