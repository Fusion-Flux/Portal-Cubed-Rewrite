package io.github.fusionflux.portalcubed.content;

import org.lwjgl.glfw.GLFW;
import org.quiltmc.qsl.lifecycle.api.client.event.ClientTickEvents;

import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.KeyMapping;

public enum PortalCubedKeyBindings {
	GRAB("grab", GLFW.GLFW_KEY_G, () -> System.out.println("I'm with the science team!"));

	public final String name;
	public final Runnable onPress;
	private final KeyMapping mapping;

	PortalCubedKeyBindings(String name, int keyCode, Runnable onPress) {
		this.name = name;
		this.onPress = onPress;
		this.mapping = new KeyMapping("key.portalcubed." + name, keyCode, "key.categories.portalcubed");
		KeyBindingHelper.registerKeyBinding(mapping);
	}

	public static void init() {
		ClientTickEvents.START.register(client -> {
			for (var keyBinding : values()) {
				while (keyBinding.mapping.consumeClick())
					keyBinding.onPress.run();
			}
		});
	}

	// @ClientOnly
	// public static class Client {
	// 	public static final KeyMapping GRAB_PROP = register("grab", GLFW.GLFW_KEY_G);

	// 	private static KeyMapping register(String name, int keyCode) {
	// 		return new KeyMapping("key.portalcubed." + name, keyCode, "key.categories.portalcubed");
	// 	}

	// 	public static void init() {
	// 	}
	// }

	// private static record KeyBinding(Consumer<Minecraft> onClick, ResourceLocation id, @ClientOnly KeyMapping mapping) { }
}
