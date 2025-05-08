package io.github.fusionflux.portalcubed.mixin.misc;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import io.github.fusionflux.portalcubed.content.PortalCubedDataComponents;
import io.github.fusionflux.portalcubed.framework.item.FallSound;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin {
	@Shadow
	public abstract ItemStack getItemBySlot(EquipmentSlot slot);

	@Inject(
			method = "checkFallDamage",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/world/entity/Entity;checkFallDamage(DZLnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/core/BlockPos;)V"
			)
	)
	private void playCustomFallSounds(double y, boolean onGround, BlockState state, BlockPos pos, CallbackInfo ci) {
		LivingEntity self = (LivingEntity) (Object) this;
		if (!self.isSilent() && onGround && self.fallDistance > 0) {
			ItemStack stack = this.getItemBySlot(EquipmentSlot.FEET);
			FallSound fallSound = stack.get(PortalCubedDataComponents.FALL_SOUND);
			if (fallSound != null && self.fallDistance >= fallSound.distance()) {
				// the only equipment sound is equip, which uses entity random for the seed
				self.level().playSeededSound(null, self.getX(), self.getY(), self.getZ(), fallSound.sound().value(), self.getSoundSource(), 1, 1, self.getRandom().nextLong());
			}
		}
	}
}
