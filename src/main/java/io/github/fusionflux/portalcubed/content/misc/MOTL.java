package io.github.fusionflux.portalcubed.content.misc;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;

import org.quiltmc.loader.api.ModMetadata;
import org.quiltmc.loader.api.QuiltLoader;

import io.github.fusionflux.portalcubed.PortalCubed;
import net.minecraft.Util;
import net.minecraft.util.RandomSource;

/**
 * Message Of The Launch
 */
public class MOTL {
	private static final List<Supplier<String>> providers = new ArrayList<>();

	static {
		register(() -> {
			ModMetadata meta = QuiltLoader.getModContainer(PortalCubed.ID).orElseThrow().metadata();
			return Util.getRandomSafe(List.copyOf(meta.contributors()), RandomSource.create()).map(
					contributor -> "I loved the part when " + contributor.name() + " said \"It's Portalin' time\" and portal'd all over the place"
			).orElse(null);
		});
	}

	private static void register(String string) {
		providers.add(() -> string);
	}

	private static void register(Supplier<String> provider) {
		providers.add(provider);
	}

	public static String get() {
		Collections.shuffle(providers);
		for (Supplier<String> provider : providers) {
			String message = provider.get();
			if (message != null) {
				return message;
			}
		}
		return "Sorry, no MOTL :(";
	}
}
