package io.github.fusionflux.portalcubed.content.lemon;

import org.jetbrains.annotations.Nullable;

import com.mojang.serialization.MapCodec;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.item.properties.conditional.ConditionalItemModelProperty;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

public record Armed() implements ConditionalItemModelProperty {
	public static final MapCodec<Armed> MAP_CODEC = MapCodec.unit(new Armed());

	@Override
	public boolean get(ItemStack stack, @Nullable ClientLevel level, @Nullable LivingEntity entity, int seed, ItemDisplayContext ctx) {
		return LemonadeItem.isArmed(stack);
	}

	@Override
	public MapCodec<Armed> type() {
		return MAP_CODEC;
	}
}
