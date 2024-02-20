package io.github.fusionflux.portalcubed.content;

import org.jetbrains.annotations.Nullable;

import io.github.fusionflux.portalcubed.PortalCubed;
import io.github.fusionflux.portalcubed.content.prop.P1Prop;
import io.github.fusionflux.portalcubed.framework.extension.LevelExt;
import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;

public class PortalCubedDamageSources {
	public static final ResourceKey<DamageType> PORTAL_1_PROP = ResourceKey.create(Registries.DAMAGE_TYPE, PortalCubed.id("portal_1_prop"));

	private final Holder.Reference<DamageType> portal1PropType;

	public PortalCubedDamageSources(RegistryAccess registryAccess) {
		var damageTypes = registryAccess.registryOrThrow(Registries.DAMAGE_TYPE);
		this.portal1PropType = damageTypes.getHolderOrThrow(PORTAL_1_PROP);
	}

	public static DamageSource portal1Prop(Level level, P1Prop source, @Nullable Entity attacked) {
		var damageType = ((LevelExt) level).pc$damageSources().portal1PropType;
		if (attacked instanceof LivingEntity livingEntity)
			return new DamageSource(damageType, source, livingEntity.getKillCredit());
		return new DamageSource(damageType, source);
	}
}
