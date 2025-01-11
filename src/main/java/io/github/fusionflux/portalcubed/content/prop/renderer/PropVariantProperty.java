package io.github.fusionflux.portalcubed.content.prop.renderer;

import com.mojang.serialization.MapCodec;
import io.github.fusionflux.portalcubed.content.PortalCubedDataComponents;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.item.properties.select.SelectItemModelProperty;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public record PropVariantProperty() implements SelectItemModelProperty<Integer> {
	public static final SelectItemModelProperty.Type<PropVariantProperty, Integer> TYPE = SelectItemModelProperty.Type.create(
			MapCodec.unit(new PropVariantProperty()), ExtraCodecs.NON_NEGATIVE_INT
	);

	@Override
	@Nullable
	public Integer get(ItemStack stack, @Nullable ClientLevel level, @Nullable LivingEntity entity, int seed, ItemDisplayContext displayContext) {
		return stack.get(PortalCubedDataComponents.PROP_VARIANT);
	}

	@Override
	@NotNull
	public Type<? extends SelectItemModelProperty<Integer>, Integer> type() {
		return TYPE;
	}
}
