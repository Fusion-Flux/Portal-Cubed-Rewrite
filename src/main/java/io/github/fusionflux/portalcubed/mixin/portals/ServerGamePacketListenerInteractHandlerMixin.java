package io.github.fusionflux.portalcubed.mixin.portals;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.expression.Definition;
import com.llamalad7.mixinextras.expression.Expression;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;

@Mixin(targets = "net/minecraft/server/network/ServerGamePacketListenerImpl$1")
public class ServerGamePacketListenerInteractHandlerMixin {
	@Definition(id = "field_28963", field = "Lnet/minecraft/server/network/ServerGamePacketListenerImpl$1;field_28963:Lnet/minecraft/server/network/ServerGamePacketListenerImpl;")
	@Definition(id = "player", field = "Lnet/minecraft/server/network/ServerGamePacketListenerImpl;player:Lnet/minecraft/server/level/ServerPlayer;")
	@Expression("? != this.field_28963.player")
	@ModifyExpressionValue(method = "onAttack", at = @At("MIXINEXTRAS:EXPRESSION"))
	private boolean allowAttackingSelf(boolean original) {
		return true;
	}
}
