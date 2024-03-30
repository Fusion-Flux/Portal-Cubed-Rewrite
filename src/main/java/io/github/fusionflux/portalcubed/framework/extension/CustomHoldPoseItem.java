package io.github.fusionflux.portalcubed.framework.extension;

import org.quiltmc.loader.api.minecraft.ClientOnly;

import net.minecraft.client.model.HumanoidModel.ArmPose;
import net.minecraft.world.item.ItemStack;

@ClientOnly
public interface CustomHoldPoseItem {
	ArmPose getHoldPose(ItemStack stack);
}
