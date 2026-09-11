package io.github.fusionflux.portalcubed.framework.item;

import java.util.function.Consumer;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;

import io.github.fusionflux.portalcubed.content.PortalCubedDataComponents;
import io.github.fusionflux.portalcubed.framework.registration.block.BlockItemProvider;
import io.github.fusionflux.portalcubed.framework.registration.item.ItemBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Item.TooltipContext;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipProvider;
import net.minecraft.world.level.block.Block;

/// Unit DataComponent that just adds a tooltip.
/// @see PortalCubedDataComponents#CONFIGURABLE_TEST_ELEMENT
public enum ConfigurableTestElement implements TooltipProvider {
	INSTANCE;

	public static final Codec<ConfigurableTestElement> CODEC = MapCodec.unitCodec(INSTANCE);
	public static final StreamCodec<ByteBuf, ConfigurableTestElement> STREAM_CODEC = StreamCodec.unit(INSTANCE);

	public static final Component TOOLTIP_TITLE = Component.translatable("block.portalcubed.hammerable.desc1").withStyle(ChatFormatting.GRAY);
	public static final Component TOOLTIP_DESC = CommonComponents.space().append(
			Component.translatable("block.portalcubed.hammerable.desc2").withStyle(ChatFormatting.BLUE)
	);

	@Override
	public void addToTooltip(TooltipContext context, Consumer<Component> output, TooltipFlag flag, DataComponentGetter components) {
		output.accept(CommonComponents.EMPTY);
		output.accept(TOOLTIP_TITLE);
		output.accept(TOOLTIP_DESC);
	}

	/// Acts as a [BlockItemProvider] that adds this component to the item.
	public static ItemBuilder<Item> addComponent(String ignoredName, Block ignoredBlock, ItemBuilder<Item> builder) {
		return builder.properties(p -> p.component(PortalCubedDataComponents.CONFIGURABLE_TEST_ELEMENT, INSTANCE));
	}
}
