package io.github.fusionflux.portalcubed.content;

import org.jetbrains.annotations.Nullable;

import io.github.fusionflux.portalcubed.PortalCubed;
import io.github.fusionflux.portalcubed.content.prop.entity.Prop;
import io.github.fusionflux.portalcubed.framework.extension.LevelExt;
import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;

public class PortalCubedDamageSources {
	public static final ResourceKey<DamageType> LANDING_DAMAGE = ResourceKey.create(Registries.DAMAGE_TYPE, PortalCubed.id("landing_damage"));

	private final Holder.Reference<DamageType> landingDamageType;

	public PortalCubedDamageSources(RegistryAccess registryAccess) {
		var damageTypes = registryAccess.registryOrThrow(Registries.DAMAGE_TYPE);
		this.landingDamageType = damageTypes.getHolderOrThrow(LANDING_DAMAGE);
	}

	public static DamageSource landingDamage(Level level, Prop source, @Nullable Entity attacked) {
		var damageType = ((LevelExt) level).pc$damageSources().landingDamageType;
		if (attacked instanceof LivingEntity livingEntity)
			return new LandingDamageSource(damageType, source, livingEntity.getKillCredit());
		return new LandingDamageSource(damageType, source, source);
	}

	public static class LandingDamageSource extends DamageSource {
		LandingDamageSource(Holder<DamageType> type, Entity source, Entity attacker) {
			super(type, source, attacker);
		}

		@Override
		public Component getLocalizedDeathMessage(LivingEntity attacked) {
			var id = "death.attack." + type().msgId();
			var sourceName = getDirectEntity().getDisplayName();
			if (!(getEntity() instanceof LivingEntity attacker))
				return Component.translatable(id, attacked.getDisplayName(), sourceName);
			var causeName = getEntity().getDisplayName();
			var attackerHeldStack = attacker.getMainHandItem();
			return !attackerHeldStack.isEmpty() && attackerHeldStack.hasCustomHoverName()
				? Component.translatable(id + ".item", attacked.getDisplayName(), sourceName, causeName, attackerHeldStack.getDisplayName())
				: Component.translatable(id + ".player", attacked.getDisplayName(), sourceName, causeName);
		}
	}
}
