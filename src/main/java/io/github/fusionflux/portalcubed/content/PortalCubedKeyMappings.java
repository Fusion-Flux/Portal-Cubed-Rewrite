package io.github.fusionflux.portalcubed.content;

import java.util.ArrayList;
import java.util.List;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.datafixers.util.Pair;

import io.github.fusionflux.portalcubed.framework.key.GrabKeyMappingAction;
import io.github.fusionflux.portalcubed.framework.key.KeyMappingAction;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.KeyMapping;

public class PortalCubedKeyMappings {
	public static final String CATEGORY = "key.categories.portalcubed";

	private static final List<Pair<KeyMapping, KeyMappingAction>> actions = new ArrayList<>();

	public static void init() {
		register("grab", InputConstants.KEY_G, new GrabKeyMappingAction());

		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			for (Pair<KeyMapping, KeyMappingAction> pair : actions) {
				KeyMapping key = pair.getFirst();
				KeyMappingAction action = pair.getSecond();
				while (key.consumeClick()) {
					action.onPressed(client);
				}
			}
		});
	}

	private static void register(String name, int key, KeyMappingAction action) {
		KeyMapping keyMapping = KeyBindingHelper.registerKeyBinding(new KeyMapping(
				"key.portalcubed." + name, key, CATEGORY
		));
		actions.add(Pair.of(keyMapping, action));
	}
}
