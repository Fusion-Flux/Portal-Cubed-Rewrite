package io.github.fusionflux.portalcubed.mixin.client;

import java.lang.reflect.Type;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;

import net.minecraft.client.renderer.block.model.BlockElement;

@Mixin(targets = "net.minecraft.client.renderer.block.model.BlockElement$Deserializer")
public class BlockElement_DeserializerMixin {
	@ModifyReturnValue(
			method = "deserialize(Lcom/google/gson/JsonElement;Ljava/lang/reflect/Type;Lcom/google/gson/JsonDeserializationContext;)Lnet/minecraft/client/renderer/block/model/BlockElement;",
			at = @At("RETURN")
	)
	private BlockElement addName(BlockElement element,
								 JsonElement json, Type type, JsonDeserializationContext context) {
		JsonObject obj = json.getAsJsonObject();
		JsonElement nameElement = obj.get("name");
		if (nameElement instanceof JsonPrimitive primitive && primitive.isString())
			element.pc$setName(primitive.getAsString());
		return element;
	}
}
