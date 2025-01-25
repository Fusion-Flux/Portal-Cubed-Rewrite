package io.github.fusionflux.portalcubed.content;

import io.github.fusionflux.portalcubed.PortalCubed;
import io.github.fusionflux.portalcubed.content.fizzler.FizzleBehaviour;
import io.github.fusionflux.portalcubed.data.tags.PortalCubedEntityTags;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;

public class PortalCubedFizzleBehaviours {
	public static final FizzleBehaviour DISINTEGRATION = register("disintegration", entity -> !entity.getType().is(PortalCubedEntityTags.IMMUNE_TO_DISINTEGRATION) && entity.pc$disintegrate());
	public static final FizzleBehaviour PORTAL_CLEARING = register("portal_clearing", entity -> false);
	public static final FizzleBehaviour PAINT_CLEARING = register("paint_clearing", entity -> false);

	private static FizzleBehaviour register(String name, FizzleBehaviour fizzleBehaviour) {
		ResourceLocation id = PortalCubed.id(name);
		return Registry.register(PortalCubedRegistries.FIZZLE_BEHAVIOUR, id, fizzleBehaviour);
	}

	public static void init() {
	}
}
