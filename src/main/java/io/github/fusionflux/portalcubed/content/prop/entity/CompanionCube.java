package io.github.fusionflux.portalcubed.content.prop.entity;

import org.quiltmc.loader.api.minecraft.ClientOnly;

import io.github.fusionflux.portalcubed.content.PortalCubedSounds;
import io.github.fusionflux.portalcubed.content.prop.PropSoundInstance;
import io.github.fusionflux.portalcubed.content.prop.PropType;
import io.github.fusionflux.portalcubed.framework.extension.AmbientSoundEmitter;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;

public class CompanionCube extends ButtonActivatedProp implements AmbientSoundEmitter {
	public CompanionCube(PropType type, EntityType<?> entityType, Level level) {
		super(type, entityType, level);
	}

	@ClientOnly
	@Override
	public void playAmbientSound() {
		Minecraft.getInstance().getSoundManager().play(new PropSoundInstance(PortalCubedSounds.COMPANION_CUBE_AMBIANCE, this));
	}
}
