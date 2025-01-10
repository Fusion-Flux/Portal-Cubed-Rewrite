package io.github.fusionflux.portalcubed.content.misc;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;

import io.github.fusionflux.portalcubed.PortalCubed;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.fabricmc.loader.api.metadata.Person;
import net.minecraft.Util;
import net.minecraft.util.RandomSource;

/**
 * Message Of The Launch
 */
public class MOTL {
	private static final List<Supplier<String>> providers = new ArrayList<>();

	static {
		register(() -> {
			ModContainer container = FabricLoader.getInstance().getModContainer(PortalCubed.ID).orElseThrow();
			List<String> names = container.getMetadata().getAuthors().stream().map(Person::getName).toList();

			return Util.getRandomSafe(names, RandomSource.create()).map(
					name -> "I loved the part when " + name + " said \"It's Portalin' time\" and portal'd all over the place"
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
