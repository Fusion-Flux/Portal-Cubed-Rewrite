package io.github.fusionflux.portalcubed.content;

import net.minecraft.core.Registry;

import net.minecraft.world.item.ItemStack;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import io.github.fusionflux.portalcubed.PortalCubed;
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

import java.util.Objects;

public class PortalCubedDamageSources {
	public static final ResourceKey<DamageType> LANDING_DAMAGE = ResourceKey.create(Registries.DAMAGE_TYPE, PortalCubed.id("landing_damage"));
	public static final ResourceKey<DamageType> LEMONADE = ResourceKey.create(Registries.DAMAGE_TYPE, PortalCubed.id("lemonade"));
	public static final ResourceKey<DamageType> LEMONADE_PLAYER = ResourceKey.create(Registries.DAMAGE_TYPE, PortalCubed.id("lemonade_player"));

	private final Holder.Reference<DamageType> landingDamageType;
	private final Holder.Reference<DamageType> lemonadeDamageType;
	private final Holder.Reference<DamageType> lemonadePlayerDamageType;

	public PortalCubedDamageSources(RegistryAccess registryAccess) {
		Registry<DamageType> damageTypes = registryAccess.registryOrThrow(Registries.DAMAGE_TYPE);
		this.landingDamageType = damageTypes.getHolderOrThrow(LANDING_DAMAGE);
		this.lemonadeDamageType = damageTypes.getHolderOrThrow(LEMONADE);
		this.lemonadePlayerDamageType = damageTypes.getHolderOrThrow(LEMONADE_PLAYER);
	}

	public static DamageSource landingDamage(Level level, @Nullable Entity source, @Nullable Entity attacked) {
		Holder.Reference<DamageType> damageType = ((LevelExt) level).pc$damageSources().landingDamageType;
		if (attacked instanceof LivingEntity livingEntity)
			return new LandingDamageSource(damageType, source, livingEntity.getKillCredit());
		return new LandingDamageSource(damageType, source, source);
	}

	public static DamageSource lemonade(Level level, @Nullable Entity source, @Nullable Entity attacked) {
		PortalCubedDamageSources damageSources = ((LevelExt) level).pc$damageSources();
		return new DamageSource(attacked != null && source != null ? damageSources.lemonadePlayerDamageType : damageSources.lemonadeDamageType, source, attacked);
	}

	public static class LandingDamageSource extends DamageSource {
		LandingDamageSource(Holder<DamageType> type, Entity source, Entity attacker) {
			super(type, source, attacker);
		}

		@NotNull
		@Override
		public Component getLocalizedDeathMessage(LivingEntity attacked) {
			String id = "death.attack." + type().msgId();
			Component sourceName = Objects.requireNonNull(getDirectEntity()).getDisplayName();
			if (!(getEntity() instanceof LivingEntity attacker))
				return Component.translatable(id, attacked.getDisplayName(), sourceName);
			Component causeName = getEntity().getDisplayName();
			ItemStack attackerHeldStack = attacker.getMainHandItem();
			return !attackerHeldStack.isEmpty() && attackerHeldStack.hasCustomHoverName()
				? Component.translatable(id + ".item", attacked.getDisplayName(), sourceName, causeName, attackerHeldStack.getDisplayName())
				: Component.translatable(id + ".player", attacked.getDisplayName(), sourceName, causeName);
		}
	}
}
