package io.github.fusionflux.portalcubed.content.prop.entity;

import io.github.fusionflux.portalcubed.content.PortalCubedSounds;
import io.github.fusionflux.portalcubed.content.prop.PropSoundInstance;
import io.github.fusionflux.portalcubed.content.prop.PropType;
import io.github.fusionflux.portalcubed.framework.extension.AmbientSoundEmitter;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;

public class CompanionCube extends ButtonActivatedProp implements AmbientSoundEmitter {
	public CompanionCube(PropType type, EntityType<?> entityType, Level level) {
		super(type, entityType, level);
	}

	@Environment(EnvType.CLIENT)
	@Override
	public void playAmbientSound() {
		PropSoundInstance soundInstance = new PropSoundInstance(PortalCubedSounds.COMPANION_CUBE_AMBIANCE, this);
		soundInstance.setLooping(true);
		Minecraft.getInstance().getSoundManager().play(soundInstance);
	}
}
