package io.github.fusionflux.portalcubed.mixin.client;

import java.lang.reflect.Type;

import org.joml.Quaternionf;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.mojang.math.Transformation;

import net.minecraft.client.renderer.block.model.Variant;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.Mth;

@Mixin(Variant.Deserializer.class)
public class Variant_DeserializerMixin {
	@ModifyReturnValue(
			method = "deserialize(Lcom/google/gson/JsonElement;Ljava/lang/reflect/Type;Lcom/google/gson/JsonDeserializationContext;)Lnet/minecraft/client/renderer/block/model/Variant;",
			at = @At("RETURN")
	)
	private Variant deserialize(Variant variant, JsonElement element, Type type, JsonDeserializationContext ctx) {
		JsonObject json = element.getAsJsonObject();
		float shiftX = GsonHelper.getAsFloat(json, "portalcubed:shift_x", 0);
		float shiftY = GsonHelper.getAsFloat(json, "portalcubed:shift_y", 0);
		float shiftZ = GsonHelper.getAsFloat(json, "portalcubed:shift_z", 0);
		int rotZ = GsonHelper.getAsInt(json, "portalcubed:rot_z", 0);
		boolean isRotZLocal = GsonHelper.getAsBoolean(json, "portalcubed:local_rot_z", false);

		if (shiftX != 0 || shiftY != 0 || shiftZ != 0 || rotZ != 0) {
			Transformation oldTransformation = variant.pc$transformation();
			Quaternionf oldRotation = oldTransformation.getLeftRotation();
			float rotZRadians = -rotZ * Mth.DEG_TO_RAD;
			variant.pc$transformation(new Transformation(
				oldTransformation.getTranslation().add(shiftX / 16f, shiftY / 16f, shiftZ / 16f),
				isRotZLocal ? oldRotation.rotateLocalZ(rotZRadians) : oldRotation.rotateZ(rotZRadians),
				oldTransformation.getScale(),
				oldTransformation.getRightRotation()
			));
		}

		return variant;
	}
}
