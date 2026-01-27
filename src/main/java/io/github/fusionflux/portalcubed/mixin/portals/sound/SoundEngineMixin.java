package io.github.fusionflux.portalcubed.mixin.portals.sound;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;

import io.github.fusionflux.portalcubed.content.portal.Portal;
import io.github.fusionflux.portalcubed.content.portal.interaction.PortalInteractionUtils;
import io.github.fusionflux.portalcubed.content.portal.ref.PortalPath;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.client.sounds.ChannelAccess;
import net.minecraft.client.sounds.SoundEngine;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

@Mixin(SoundEngine.class)
public class SoundEngineMixin {
	@ModifyExpressionValue(
			method = "tickNonPaused",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/client/sounds/ChannelAccess$ChannelHandle;isStopped()Z"
			)
	)
	private boolean teleportSound(boolean stopped, @Local SoundInstance sound, @Local ChannelAccess.ChannelHandle handle) {
		if (stopped) {
			return true;
		}

		boolean teleported = teleportSound(sound, handle);
		if (handle.pc$teleportedLastTick() && !teleported) {
			// reset to original position
			Vec3 pos = new Vec3(sound.getX(), sound.getY(), sound.getZ());
			handle.execute(channel -> channel.setSelfPosition(pos));
		}

		handle.pc$setTeleportedLastTick(teleported);

		return false;
	}

	@Unique
	private static boolean teleportSound(SoundInstance sound, ChannelAccess.ChannelHandle handle) {
		float range = determineRange(sound);
		if (!Float.isFinite(range))
			return false;

		Minecraft mc = Minecraft.getInstance();
		if (mc.level == null)
			return false;

		Vec3 cameraPos = mc.gameRenderer.getMainCamera().getPosition();
		Vec3 soundPos = new Vec3(sound.getX(), sound.getY(), sound.getZ());
		// camera -> sound avoids getting the tail later
		PortalPath path = PortalInteractionUtils.findPathThroughPortals(mc.level, cameraPos, soundPos, range);
		if (path == null)
			return false;

		double directDistanceSqr = soundPos.distanceToSqr(cameraPos);
		double distanceThroughPortals = path.length(cameraPos, soundPos);
		if (directDistanceSqr <= Mth.square(distanceThroughPortals))
			return false;

		Portal enteredPortal = path.entries.getFirst().entered().get();
		Vec3 direction = cameraPos.vectorTo(enteredPortal.data.origin()).normalize();
		Vec3 newPos = cameraPos.add(direction.scale(distanceThroughPortals));
		handle.execute(channel -> channel.setSelfPosition(newPos));
		return true;
	}

	@Unique
	private static float determineRange(SoundInstance sound) {
		if (sound.isRelative() || sound.getAttenuation() == SoundInstance.Attenuation.NONE)
			return Float.POSITIVE_INFINITY;

		float volume = sound.getVolume();
		return Math.max(volume, 1) * sound.getSound().getAttenuationDistance();
	}
}
