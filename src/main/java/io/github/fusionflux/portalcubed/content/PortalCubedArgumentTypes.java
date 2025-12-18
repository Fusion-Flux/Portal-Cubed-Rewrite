package io.github.fusionflux.portalcubed.content;

import com.mojang.brigadier.arguments.ArgumentType;

import io.github.fusionflux.portalcubed.PortalCubed;
import io.github.fusionflux.portalcubed.content.portal.command.argument.portal.PortalArgument;
import io.github.fusionflux.portalcubed.framework.command.argument.ColorArgumentType;
import io.github.fusionflux.portalcubed.framework.command.argument.DirectionArgumentType;
import io.github.fusionflux.portalcubed.framework.command.argument.FizzleBehaviourArgumentType;
import io.github.fusionflux.portalcubed.framework.command.argument.FlagArgumentType;
import io.github.fusionflux.portalcubed.framework.command.argument.PolarityArgumentType;
import io.github.fusionflux.portalcubed.framework.command.argument.PortalColorArgumentType;
import io.github.fusionflux.portalcubed.framework.command.argument.PortalKeyArgumentType;
import io.github.fusionflux.portalcubed.framework.command.argument.PortalValidatorArgumentType;
import io.github.fusionflux.portalcubed.framework.command.argument.QuaternionArgumentType;
import io.github.fusionflux.portalcubed.framework.command.argument.TriStateArgumentType;
import io.github.fusionflux.portalcubed.mixin.commands.ArgumentTypeInfosAccessor;
import net.minecraft.commands.synchronization.ArgumentTypeInfo;
import net.minecraft.commands.synchronization.SingletonArgumentInfo;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;

public class PortalCubedArgumentTypes {
	public static void init() {
		register("color", ColorArgumentType.class, SingletonArgumentInfo.contextFree(ColorArgumentType::color));
		register("direction", DirectionArgumentType.class, SingletonArgumentInfo.contextFree(DirectionArgumentType::direction));
		register("polarity", PolarityArgumentType.class, SingletonArgumentInfo.contextFree(PolarityArgumentType::polarity));
		register("quaternion", QuaternionArgumentType.class, SingletonArgumentInfo.contextFree(QuaternionArgumentType::quaternion));
		register("tri_state", TriStateArgumentType.class, SingletonArgumentInfo.contextFree(TriStateArgumentType::triState));
		register("portal_key", PortalKeyArgumentType.class, SingletonArgumentInfo.contextFree(PortalKeyArgumentType::portalKey));
		register("flag", FlagArgumentType.class, FlagArgumentType.Serializer.INSTANCE);
		register("fizzle_behaviour", FizzleBehaviourArgumentType.class, SingletonArgumentInfo.contextFree(FizzleBehaviourArgumentType::fizzleBehaviour));
		register("portal_validator", PortalValidatorArgumentType.class, SingletonArgumentInfo.contextFree(PortalValidatorArgumentType::portalValidator));
		register("portal_color", PortalColorArgumentType.class, SingletonArgumentInfo.contextFree(PortalColorArgumentType::portalColor));
		register("portal", PortalArgument.class, SingletonArgumentInfo.contextAware(PortalArgument::portal));
	}

	private static <A extends ArgumentType<?>, T extends ArgumentTypeInfo.Template<A>> void register(
			String name, Class<? extends A> clazz, ArgumentTypeInfo<A, T> info) {
		ArgumentTypeInfosAccessor.getBY_CLASS().put(clazz, info);
		Registry.register(BuiltInRegistries.COMMAND_ARGUMENT_TYPE, PortalCubed.id(name), info);
	}
}
