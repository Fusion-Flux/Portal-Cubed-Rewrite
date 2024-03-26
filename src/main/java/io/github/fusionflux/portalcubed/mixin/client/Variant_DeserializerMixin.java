package io.github.fusionflux.portalcubed.mixin.client;

import java.lang.reflect.Type;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.mojang.math.Transformation;

import io.github.fusionflux.portalcubed.framework.extension.VariantExt;
import net.minecraft.client.renderer.block.model.Variant;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.Mth;

@Mixin(Variant.Deserializer.class)
public class Variant_DeserializerMixin {
	@Inject(method = "deserialize", at = @At("RETURN"))
	private void deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext, CallbackInfoReturnable<Variant> cir) {
		var jsonObject = jsonElement.getAsJsonObject();
		float shiftX = GsonHelper.getAsFloat(jsonObject, "portalcubed:shift_x", 0);
		float shiftY = GsonHelper.getAsFloat(jsonObject, "portalcubed:shift_y", 0);
		float shiftZ = GsonHelper.getAsFloat(jsonObject, "portalcubed:shift_z", 0);
		int rotZ = GsonHelper.getAsInt(jsonObject, "portalcubed:rot_z", 0);
		boolean isRotZLocal = GsonHelper.getAsBoolean(jsonObject, "portalcubed:local_rot_z", false);

		if ((shiftX != 0 || shiftY != 0 || shiftZ != 0 || rotZ != 0) && cir.getReturnValue() instanceof VariantExt variant) {
			var oldTransformation = variant.pc$transformation();
			var oldRotation = oldTransformation.getLeftRotation();
			float rotZRadians = -rotZ * Mth.DEG_TO_RAD;
			variant.pc$transformation(new Transformation(
				oldTransformation.getTranslation().add(shiftX / 16f, shiftY / 16f, shiftZ / 16f),
				isRotZLocal ? oldRotation.rotateLocalZ(rotZRadians) : oldRotation.rotateZ(rotZRadians),
				oldTransformation.getScale(),
				oldTransformation.getRightRotation()
			));
		}
	}
}
