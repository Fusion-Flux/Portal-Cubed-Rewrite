package io.github.fusionflux.portalcubed.mixin.commands;

import java.util.Map;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.commands.synchronization.ArgumentTypeInfo;
import net.minecraft.commands.synchronization.ArgumentTypeInfos;

@Mixin(ArgumentTypeInfos.class)
public interface ArgumentTypeInfosAccessor {
	@Accessor
	static Map<Class<?>, ArgumentTypeInfo<?, ?>> getBY_CLASS() {
		throw new AbstractMethodError();
	}
}
