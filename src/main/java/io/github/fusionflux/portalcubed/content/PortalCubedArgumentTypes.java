package io.github.fusionflux.portalcubed.content;

import com.mojang.brigadier.arguments.ArgumentType;

import io.github.fusionflux.portalcubed.PortalCubed;
import io.github.fusionflux.portalcubed.framework.command.argument.ColorArgumentType;
import io.github.fusionflux.portalcubed.framework.command.argument.DirectionArgumentType;
import io.github.fusionflux.portalcubed.framework.command.argument.PortalTypeArgumentType;
import io.github.fusionflux.portalcubed.framework.command.argument.PortalShapeArgumentType;
import io.github.fusionflux.portalcubed.framework.command.argument.QuaternionArgumentType;
import io.github.fusionflux.portalcubed.framework.command.argument.TriStateArgumentType;
import io.github.fusionflux.portalcubed.mixin.ArgumentTypeInfosAccessor;
import net.minecraft.commands.synchronization.ArgumentTypeInfo;
import net.minecraft.commands.synchronization.SingletonArgumentInfo;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;

public class PortalCubedArgumentTypes {
	public static void init() {
		register("color", ColorArgumentType.class, SingletonArgumentInfo.contextFree(ColorArgumentType::color));
		register("direction", DirectionArgumentType.class, SingletonArgumentInfo.contextFree(DirectionArgumentType::direction));
		register("polarity", PortalTypeArgumentType.class, SingletonArgumentInfo.contextFree(PortalTypeArgumentType::portalType));
		register("shape", PortalShapeArgumentType.class, SingletonArgumentInfo.contextFree(PortalShapeArgumentType::shape));
		register("quaternion", QuaternionArgumentType.class, SingletonArgumentInfo.contextFree(QuaternionArgumentType::quaternion));
		register("tri_state", TriStateArgumentType.class, SingletonArgumentInfo.contextFree(TriStateArgumentType::triState));
	}

	private static <A extends ArgumentType<?>, T extends ArgumentTypeInfo.Template<A>> void register(
			String name, Class<? extends A> clazz, ArgumentTypeInfo<A, T> info) {
		ArgumentTypeInfosAccessor.getBY_CLASS().put(clazz, info);
		Registry.register(BuiltInRegistries.COMMAND_ARGUMENT_TYPE, PortalCubed.id(name), info);
	}
}
