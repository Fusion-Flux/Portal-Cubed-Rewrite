package io.github.fusionflux.portalcubed.content;

import java.util.ArrayList;
import java.util.function.Consumer;
import java.util.function.IntSupplier;

import org.lwjgl.glfw.GLFW;
import org.quiltmc.loader.api.minecraft.ClientOnly;
import org.quiltmc.qsl.lifecycle.api.client.event.ClientTickEvents;

import io.github.fusionflux.portalcubed.content.prop.entity.Prop;
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
				if (client.player != null && mappings.get(i).consumeClick())
					PortalCubedPackets.sendToServer(new KeyPressPacket(ALL[i]));
			}
		});
	}

	public static enum KeyBinding {
		GRAB("grab", () -> GLFW.GLFW_KEY_G, (player) -> {
			var level = player.level();
			var heldPropId = ((PlayerExt) player).pc$heldProp();
			boolean isHoldingPortalGun = player.getMainHandItem().is(PortalCubedItems.PORTAL_GUN);
			if (heldPropId.isEmpty()) {
				if (player.isSpectator()) return;
				var playerDirection = Vec3.directionFromRotation(player.getXRot(), player.getYRot()).scale(3);
				var checkBox = player.getBoundingBox().expandTowards(playerDirection).inflate(1);

				var startPos = player.getEyePosition();
				var endPos = startPos.add(playerDirection);

				var hit = ProjectileUtil.getEntityHitResult(player, startPos, endPos, checkBox, entity -> !entity.isSpectator() && entity.isPickable(), 3 * 3);
				if (hit != null && hit.getEntity() instanceof Prop prop) {
					if (prop.hold(player))
						return;
				}
				if (isHoldingPortalGun) player.playSound(PortalCubedSounds.PORTAL_GUN_CANNOT_GRAB);
			} else {
				if (level.getEntity(heldPropId.getAsInt()) instanceof Prop heldProp)
					heldProp.drop(player);
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
