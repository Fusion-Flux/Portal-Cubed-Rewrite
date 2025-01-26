package io.github.fusionflux.portalcubed.content.decoration.signage.component;

import java.util.function.Consumer;

import com.mojang.serialization.Codec;

import io.github.fusionflux.portalcubed.content.decoration.signage.Signage;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipProvider;

public record SelectedLargeSignage(Holder<Signage> signage) implements TooltipProvider {
	public static final Codec<SelectedLargeSignage> CODEC = Signage.LARGE_CODEC.xmap(SelectedLargeSignage::new, SelectedLargeSignage::signage);
	public static final StreamCodec<RegistryFriendlyByteBuf, SelectedLargeSignage> STREAM_CODEC = Signage.LARGE_STREAM_CODEC.map(SelectedLargeSignage::new, SelectedLargeSignage::signage);
	public static final Component TOOLTIP_TITLE = Component.translatable("block.portalcubed.large_signage.image").withStyle(ChatFormatting.GRAY);

	@Override
	public void addToTooltip(Item.TooltipContext context, Consumer<Component> tooltipAdder, TooltipFlag tooltipFlag) {
		Component signageName = this.signage.value().name();
		tooltipAdder.accept(TOOLTIP_TITLE);
		tooltipAdder.accept(CommonComponents.space().append(signageName).withStyle(ChatFormatting.BLUE));
	}
}
