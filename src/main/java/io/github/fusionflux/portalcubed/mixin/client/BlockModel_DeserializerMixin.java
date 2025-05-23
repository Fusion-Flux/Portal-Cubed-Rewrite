package io.github.fusionflux.portalcubed.mixin.client;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;

import io.github.fusionflux.portalcubed.framework.model.RenderMaterials;
import net.fabricmc.fabric.api.renderer.v1.material.BlendMode;
import net.minecraft.client.renderer.block.model.BlockElement;
import net.minecraft.client.renderer.block.model.BlockModel;
import net.minecraft.client.renderer.block.model.BlockModel.Deserializer;

@Mixin(Deserializer.class)
public class BlockModel_DeserializerMixin {
	@ModifyReturnValue(
			method = "deserialize(Lcom/google/gson/JsonElement;Ljava/lang/reflect/Type;Lcom/google/gson/JsonDeserializationContext;)Lnet/minecraft/client/renderer/block/model/BlockModel;",
			at = @At("RETURN")
	)
	private BlockModel addRenderTypeInfo(BlockModel original, JsonElement blockElement, Type type, JsonDeserializationContext ctx) {
		if (!RenderMaterials.ARE_SUPPORTED)
			return original;

		JsonObject json = blockElement.getAsJsonObject();
		if (json.has("portalcubed:render_types")) {
			Map<String, BlendMode> elementModes = new HashMap<>();

			// read the material map
			// typeName -> elementName[]
			JsonObject typeMap = json.getAsJsonObject("portalcubed:render_types");
			typeMap.asMap().forEach((typeName, element) -> {
				BlendMode mode = RenderMaterials.parseBlendMode(typeName);
				JsonArray array = element.getAsJsonArray();
				array.forEach(nameElement -> {
					String elementName = nameElement.getAsString();
					elementModes.put(elementName, mode);
				});
			});

			// apply read materials to elements
			List<BlockElement> elements = ((BlockModelAccessor) original).callGetElements();
			for (BlockElement element : elements) {
				String name = element.pc$name();
				if (name != null && elementModes.containsKey(name))
					element.pc$setBlendMode(elementModes.get(name));
			}
		}

		return original;
	}
}
