package io.github.fusionflux.portalcubed.content;

import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.OptionalInt;
import java.util.function.Consumer;
import java.util.function.IntSupplier;

import com.mojang.datafixers.util.Pair;

import io.github.fusionflux.portalcubed.framework.key.GrabKeyMappingAction;
import io.github.fusionflux.portalcubed.framework.key.KeyMappingAction;

import org.lwjgl.glfw.GLFW;
import org.quiltmc.loader.api.minecraft.ClientOnly;
import org.quiltmc.qsl.lifecycle.api.client.event.ClientTickEvents;

import io.github.fusionflux.portalcubed.content.prop.entity.Prop;
import io.github.fusionflux.portalcubed.framework.extension.PlayerExt;
import io.github.fusionflux.portalcubed.packet.PortalCubedPackets;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;

import com.mojang.blaze3d.platform.InputConstants;

import net.minecraft.client.KeyMapping;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;

public class PortalCubedKeyMappings {
	public static final String CATEGORY = "key.categories.portalcubed";

	private static final List<Pair<KeyMapping, KeyMappingAction>> actions = new ArrayList<>();

	public static void init() {
		register("grab", InputConstants.KEY_G, new GrabKeyMappingAction());

		ClientTickEvents.END.register(client -> {
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
