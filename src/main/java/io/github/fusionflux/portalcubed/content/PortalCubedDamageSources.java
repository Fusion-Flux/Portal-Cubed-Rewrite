package io.github.fusionflux.portalcubed.content;

import io.github.fusionflux.portalcubed.PortalCubed;
import io.github.fusionflux.portalcubed.framework.extension.LevelExt;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.level.Level;

public class PortalCubedDamageSources {
	public static final ResourceKey<DamageType> PORTAL_1_PROP = ResourceKey.create(Registries.DAMAGE_TYPE, PortalCubed.id("portal_1_prop"));

	private final DamageSource portal1Prop;

	public PortalCubedDamageSources(RegistryAccess registryAccess) {
		var damageTypes = registryAccess.registryOrThrow(Registries.DAMAGE_TYPE);
		this.portal1Prop = new DamageSource(damageTypes.getHolderOrThrow(PORTAL_1_PROP));
	}

	public static DamageSource portal1Prop(Level level) {
		return ((LevelExt) level).pc$damageSources().portal1Prop;
	}
}
