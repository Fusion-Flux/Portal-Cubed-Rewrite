package io.github.fusionflux.portalcubed.content;

import java.util.ArrayList;
import java.util.OptionalInt;
import java.util.function.Consumer;
import java.util.function.IntSupplier;

import org.lwjgl.glfw.GLFW;
import org.quiltmc.loader.api.minecraft.ClientOnly;
import org.quiltmc.qsl.lifecycle.api.client.event.ClientTickEvents;

import io.github.fusionflux.portalcubed.content.prop.Prop;
import io.github.fusionflux.portalcubed.framework.extension.PlayerExt;
import io.github.fusionflux.portalcubed.packet.PortalCubedPackets;
import io.github.fusionflux.portalcubed.packet.serverbound.KeyPressPacket;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.phys.Vec3;

public class PortalCubedKeyBindings {
	public static final KeyBinding[] ALL = KeyBinding.values();

	@ClientOnly
	public static void init() {
		var mappings = new ArrayList<KeyMapping>();
		for (KeyBinding keyBinding : ALL) {
			var mapping = new KeyMapping("key.portalcubed." + keyBinding.name, keyBinding.keyCode.getAsInt(), "key.categories.portalcubed");
			mappings.add(mapping);
			KeyBindingHelper.registerKeyBinding(mapping);
		}

		ClientTickEvents.END.register(client -> {
			for (int i = 0; i < ALL.length; i++) {
				var keyBinding = ALL[i];
				if (client.player != null && mappings.get(i).consumeClick()) {
					keyBinding.onPress.accept(client.player);
					PortalCubedPackets.sendToServer(new KeyPressPacket(keyBinding));
				}
			}
		});
	}

	public static enum KeyBinding {
		GRAB("grab", () -> GLFW.GLFW_KEY_G, (player) -> {
			var level = player.level();
			var heldPropId = ((PlayerExt) player).pc$heldProp();
			if (heldPropId.isEmpty()) {
				if (player.isSpectator()) return;
				var playerDirection = Vec3.directionFromRotation(player.getXRot(), player.getYRot()).scale(3);
				var checkBox = player.getBoundingBox().expandTowards(playerDirection).inflate(1);

				var startPos = player.getEyePosition();
				var endPos = startPos.add(playerDirection);

				var hit = ProjectileUtil.getEntityHitResult(player, startPos, endPos, checkBox, entity -> !entity.isSpectator() && entity.isPickable(), 3 * 3);
				if (hit != null && hit.getEntity() instanceof Prop prop) {
					if (prop.hold(player))
						((PlayerExt) player).pc$heldProp(OptionalInt.of(prop.getId()));
				}
			} else {
				var heldProp = (Prop) level.getEntity(heldPropId.getAsInt());
				heldProp.drop(player);
				((PlayerExt) player).pc$heldProp(OptionalInt.empty());
			}
		});

		public final String name;
		public final IntSupplier keyCode;
		public final Consumer<Player> onPress;

		KeyBinding(String name, IntSupplier keyCode, Consumer<Player> onPress) {
			this.name = name;
			this.keyCode = keyCode;
			this.onPress = onPress;
		}

		@ClientOnly
		public static void init() {
		}
	}
}
