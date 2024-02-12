package io.github.fusionflux.portalcubed.content.prop;

import org.quiltmc.loader.api.minecraft.ClientOnly;
import org.quiltmc.loader.api.minecraft.MinecraftQuiltLoader;

import io.github.fusionflux.portalcubed.content.PortalCubedSounds;
import net.fabricmc.api.EnvType;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;

public class CompanionCube extends ButtonActivatedProp {
	private boolean playingSound = false;

	public CompanionCube(PropType type, EntityType<?> entityType, Level level) {
		super(type, entityType, level);
	}

	// best entrypoint for when a entity is created
	@Override
	public void setId(int id) {
		super.setId(id);
		if (MinecraftQuiltLoader.getEnvironmentType() == EnvType.CLIENT && !playingSound) {
			startPlayingSound();
			playingSound = true;
		}
	}

	@ClientOnly
	private void startPlayingSound() {
		Minecraft.getInstance().getSoundManager().play(new PropSoundInstance(PortalCubedSounds.COMPANION_CUBE_AMBIANCE, this));
	}
}
