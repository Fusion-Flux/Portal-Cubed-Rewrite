package io.github.fusionflux.portalcubed.content.cannon.screen.widget.construct;

import com.mojang.math.Axis;

import io.github.fusionflux.portalcubed.framework.construct.ConfiguredConstruct;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.network.chat.CommonComponents;

public class ConstructButtonWidget extends ConstructWidget {
	private final ConfiguredConstruct construct;

	public ConstructButtonWidget(ConfiguredConstruct construct, int size) {
		super(size, CommonComponents.EMPTY);
		this.construct = construct;
	}

	@Override
	protected void applyConstructTransformations(PoseStack matrices, float delta) {
		matrices.mulPose(Axis.YP.rotationDegrees(45));
	}

	@Override
	protected ConfiguredConstruct getConstruct() {
		return this.construct;
	}
}
