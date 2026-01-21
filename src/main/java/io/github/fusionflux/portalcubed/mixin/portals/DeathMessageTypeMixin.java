package io.github.fusionflux.portalcubed.mixin.portals;

import java.util.Arrays;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;

import io.github.fusionflux.portalcubed.content.portal.MirrorTestDeathMessageType;
import net.minecraft.world.damagesource.DeathMessageType;

@Mixin(DeathMessageType.class)
public class DeathMessageTypeMixin {
	@Invoker("<init>")
	private static DeathMessageType pc$create(String name, int ordinal, String serializedName) {
		throw new AbstractMethodError();
	}

	@ModifyReturnValue(method = "$values", at = @At("RETURN"))
	private static DeathMessageType[] addMirrorTest(DeathMessageType[] original) {
		int ordinal = original.length;
		DeathMessageType type = pc$create(MirrorTestDeathMessageType.NAME, ordinal, MirrorTestDeathMessageType.SERIALIZED_NAME);
		DeathMessageType[] newArray = Arrays.copyOf(original, original.length + 1);
		newArray[ordinal] = type;
		return newArray;
	}
}
