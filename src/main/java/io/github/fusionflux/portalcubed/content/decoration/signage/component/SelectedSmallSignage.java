package io.github.fusionflux.portalcubed.content.decoration.signage.component;

import java.util.Map;
import java.util.function.Consumer;

import com.mojang.serialization.Codec;

import io.github.fusionflux.portalcubed.content.decoration.signage.Signage;
import io.github.fusionflux.portalcubed.content.decoration.signage.small.SmallSignageBlock;
import io.github.fusionflux.portalcubed.content.decoration.signage.small.SmallSignageBlockEntity;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipProvider;

public record SelectedSmallSignage(SmallSignageBlockEntity.Quadrants quadrants) implements TooltipProvider {
	public static final Codec<SelectedSmallSignage> CODEC = SmallSignageBlockEntity.Quadrants.CODEC.xmap(SelectedSmallSignage::new, SelectedSmallSignage::quadrants);
	public static final StreamCodec<RegistryFriendlyByteBuf, SelectedSmallSignage> STREAM_CODEC = SmallSignageBlockEntity.Quadrants.STREAM_CODEC.map(SelectedSmallSignage::new, SelectedSmallSignage::quadrants);
	public static final Component TOOLTIP_TITLE = Component.translatable("block.portalcubed.small_signage.images").withStyle(ChatFormatting.GRAY);

	@Override
	public void addToTooltip(Item.TooltipContext context, Consumer<Component> tooltipAdder, TooltipFlag tooltipFlag) {
		tooltipAdder.accept(TOOLTIP_TITLE);
		for (Map.Entry<SmallSignageBlock.Quadrant, Holder<Signage>> entry : this.quadrants.map().entrySet()) {
			Holder<Signage> signage = entry.getValue();
			Component signageName = signage.value().name();
			tooltipAdder.accept(CommonComponents.space().append(signageName).withStyle(ChatFormatting.BLUE));
		}
	}
}
