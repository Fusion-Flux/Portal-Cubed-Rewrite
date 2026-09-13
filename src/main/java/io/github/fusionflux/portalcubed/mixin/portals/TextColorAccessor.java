package io.github.fusionflux.portalcubed.mixin.portals;

import java.util.Map;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.network.chat.TextColor;

@Mixin(TextColor.class)
public interface TextColorAccessor {
	@Accessor
	static Map<String, TextColor> getNAMED_COLORS() {
		throw new AbstractMethodError();
	}
}
