package io.github.fusionflux.portalcubed.content.cannon.screen.widget.construct;

import com.mojang.math.Axis;

import io.github.fusionflux.portalcubed.framework.construct.ConfiguredConstruct;
import io.github.fusionflux.portalcubed.framework.construct.set.ConstructSet;
import io.github.fusionflux.portalcubed.framework.gui.util.AdvancedTooltip;
import io.github.fusionflux.portalcubed.framework.gui.util.TagWithCountTooltipComponent;
import io.github.fusionflux.portalcubed.framework.construct.ConstructModelPool;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;

public class ConstructButtonWidget extends ConstructWidget {
	private final ConfiguredConstruct construct;
	private final AdvancedTooltip tooltip;

	public ConstructButtonWidget(ConstructSet constructSet, ResourceLocation id, TagKey<Item> material, int size, ConstructModelPool modelPool) {
		super(size, ConstructSet.getName(id), modelPool);
		this.construct = constructSet.preview;
		this.tooltip = new AdvancedTooltip(builder -> {
			builder.add(ConstructSet.getName(id));
			if (builder.advanced) {
				builder.add(Component.literal(id.toString()).withStyle(ChatFormatting.DARK_GRAY));
			}
			constructSet.appendTooltip(builder);
			builder.add(new TagWithCountTooltipComponent(material, constructSet.cost));
		});
	}

	@Override
	protected void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
		super.renderWidget(graphics, mouseX, mouseY, delta);
		if (this.isHovered()) {
			PoseStack matrices = graphics.pose();
			matrices.pushPose();
			// extra Z to render on top of the really high side panels
			matrices.translate(0, 0, 500);
			this.tooltip.render(graphics, mouseX, mouseY);
			matrices.popPose();
		}
	}

	@Override
	protected void applyConstructTransformations(PoseStack matrices, float delta) {
		matrices.mulPose(Axis.YN.rotationDegrees(45));
	}

	@Override
	protected ConfiguredConstruct getConstruct() {
		return this.construct;
	}
}
