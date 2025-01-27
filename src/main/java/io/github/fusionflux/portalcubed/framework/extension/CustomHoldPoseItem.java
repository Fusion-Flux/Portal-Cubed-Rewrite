package io.github.fusionflux.portalcubed.framework.extension;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.HumanoidModel.ArmPose;
import net.minecraft.world.item.ItemStack;

public interface CustomHoldPoseItem {
	@Environment(EnvType.CLIENT)
	ArmPose getHoldPose(ItemStack stack);
}
