package io.github.fusionflux.portalcubed.mixin.props;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.expression.Definition;
import com.llamalad7.mixinextras.expression.Expression;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;

import io.github.fusionflux.portalcubed.content.prop.entity.Chair;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.world.entity.decoration.Cushion;

@Mixin(ClientPacketListener.class)
public class ClientPacketListenerMixin {
	@Definition(id = "Cushion", type = Cushion.class)
	@Expression("? instanceof Cushion")
	@WrapOperation(method = "handleSetEntityPassengersPacket", at = @At("MIXINEXTRAS:EXPRESSION"))
	private boolean chairsUseCushionTerminology(Object vehicle, Operation<Boolean> original) {
		return original.call(vehicle) || vehicle instanceof Chair;
	}
}
