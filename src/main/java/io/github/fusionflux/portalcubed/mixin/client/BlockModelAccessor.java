package io.github.fusionflux.portalcubed.mixin.client;

import java.util.List;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

import net.minecraft.client.renderer.block.model.BlockElement;
import net.minecraft.client.renderer.block.model.BlockModel;
import net.minecraft.client.renderer.block.model.TextureSlots;

@Mixin(BlockModel.class)
public interface BlockModelAccessor {
	@Accessor
	TextureSlots.Data getTextureSlots();

	@Invoker
	List<BlockElement> callGetElements();
}
