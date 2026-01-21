package io.github.fusionflux.portalcubed.mixin.portals;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Cancellable;
import com.llamalad7.mixinextras.sugar.Local;

import io.github.fusionflux.portalcubed.content.portal.MirrorTestDeathMessageType;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.Style;
import net.minecraft.world.damagesource.CombatTracker;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DeathMessageType;
import net.minecraft.world.entity.LivingEntity;

@Mixin(CombatTracker.class)
public class CombatTrackerMixin {
	@Shadow
	@Final
	private LivingEntity mob;
	@Unique
	private static final Style MIRROR_TEST_STYLE = Style.EMPTY
			.withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, "https://en.wikipedia.org/wiki/Mirror_test"))
			.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Component.translatable("death.attack.portalcubed.self.hover")));

	@ModifyExpressionValue(
			method = "getDeathMessage",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/world/damagesource/DamageType;deathMessageType()Lnet/minecraft/world/damagesource/DeathMessageType;"
			)
	)
	private DeathMessageType handleMirrorTestType(DeathMessageType type, @Local DamageSource source, @Cancellable CallbackInfoReturnable<Component> cir) {
		if (type == MirrorTestDeathMessageType.get()) {
			String keyPrefix = "death.attack." + source.getMsgId();
			Component link = ComponentUtils.wrapInSquareBrackets(Component.translatable(keyPrefix + ".link")).withStyle(MIRROR_TEST_STYLE);
			Component message = Component.translatable(keyPrefix + ".message", this.mob.getDisplayName(), link);
			cir.setReturnValue(message);
		}

		return type;
	}
}
