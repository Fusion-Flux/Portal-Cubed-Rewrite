package io.github.fusionflux.portalcubed.content.cannon.screen.widget.construct;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;

import io.github.fusionflux.portalcubed.framework.construct.ConfiguredConstruct;
import io.github.fusionflux.portalcubed.framework.construct.set.ConstructSet;
import io.github.fusionflux.portalcubed.framework.gui.util.AdvancedTooltip;
import io.github.fusionflux.portalcubed.framework.gui.util.TagWithCountTooltipComponent;
import io.github.fusionflux.portalcubed.framework.gui.widget.TexturedStickyButton.Textures;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;

public class ConstructButtonWidget extends ConstructWidget {
	private final ConfiguredConstruct construct;
	private final Textures textures;
	private final Runnable onSelect;
	private final AdvancedTooltip tooltip;

	private boolean selected;

	public ConstructButtonWidget(Runnable onSelect, ConstructSet constructSet, ResourceLocation id, TagKey<Item> material, Textures textures, int size) {
		super(size, ConstructSet.getName(id));
		this.construct = constructSet.preview;
		this.textures = textures;
		this.onSelect = onSelect;
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
		if (!this.isActive())
			return;

		super.renderWidget(graphics, mouseX, mouseY, delta);
		ResourceLocation texture = this.textures.choose(this.isHovered(), this.selected);
		graphics.blitSprite(texture, this.getX(), this.getY(), this.getWidth(), this.getHeight());

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
	public void onClick(double mouseX, double mouseY) {
		if (!this.selected) {
			this.onSelect.run();
			this.selected = true;
		}
	}

	public void select() {
		this.selected = true;
	}

	public void deselect() {
		this.selected = false;
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
