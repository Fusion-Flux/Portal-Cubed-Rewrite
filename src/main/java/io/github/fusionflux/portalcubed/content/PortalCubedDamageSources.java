package io.github.fusionflux.portalcubed.content;

import java.util.Objects;

import io.github.fusionflux.portalcubed.framework.extension.PlayerExt;

import net.minecraft.core.Holder.Reference;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import io.github.fusionflux.portalcubed.PortalCubed;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class PortalCubedDamageSources {
	public static final ResourceKey<DamageType> LANDING_DAMAGE = ResourceKey.create(Registries.DAMAGE_TYPE, PortalCubed.id("landing_damage"));
	public static final ResourceKey<DamageType> LEMONADE = ResourceKey.create(Registries.DAMAGE_TYPE, PortalCubed.id("lemonade"));
	public static final ResourceKey<DamageType> LEMONADE_PLAYER = ResourceKey.create(Registries.DAMAGE_TYPE, PortalCubed.id("lemonade_player"));
	public static final ResourceKey<DamageType> TOXIC_GOO = ResourceKey.create(Registries.DAMAGE_TYPE, PortalCubed.id("toxic_goo"));
	public static final ResourceKey<DamageType> DISINTEGRATION = ResourceKey.create(Registries.DAMAGE_TYPE, PortalCubed.id("disintegration"));
	public static final ResourceKey<DamageType> SUBMERGED_THE_OPERATIONAL_END_OF_THE_DEVICE = ResourceKey.create(Registries.DAMAGE_TYPE, PortalCubed.id("submerged_the_operational_end_of_the_device"));

	private final Holder.Reference<DamageType> landingDamageType;
	private final Holder.Reference<DamageType> lemonadeDamageType;
	private final Holder.Reference<DamageType> lemonadePlayerDamageType;
	private final Holder.Reference<DamageType> disintegrationDamageType;
	private final Holder.Reference<DamageType> submergedTheOperationalEndOfTheDeviceDamageType;
	private final DamageSource toxicGooDamage;

	public PortalCubedDamageSources(RegistryAccess registries) {
		Registry<DamageType> damageTypes = registries.lookupOrThrow(Registries.DAMAGE_TYPE);
		this.landingDamageType = damageTypes.getOrThrow(LANDING_DAMAGE);
		this.lemonadeDamageType = damageTypes.getOrThrow(LEMONADE);
		this.lemonadePlayerDamageType = damageTypes.getOrThrow(LEMONADE_PLAYER);
		this.disintegrationDamageType = damageTypes.getOrThrow(DISINTEGRATION);
		this.submergedTheOperationalEndOfTheDeviceDamageType = damageTypes.getOrThrow(SUBMERGED_THE_OPERATIONAL_END_OF_THE_DEVICE);
		this.toxicGooDamage = new DamageSource(damageTypes.getOrThrow(TOXIC_GOO));
	}

	public static DamageSource landingDamage(Level level, @Nullable Entity source, @Nullable Entity attacked) {
		Holder.Reference<DamageType> damageType = level.pc$damageSources().landingDamageType;
		if (attacked instanceof LivingEntity livingEntity)
			return new LandingDamageSource(damageType, source, livingEntity.getKillCredit());
		return new LandingDamageSource(damageType, source, source);
	}

	public static DamageSource lemonade(Level level, @Nullable Entity source, @Nullable Entity attacked) {
		PortalCubedDamageSources damageSources = level.pc$damageSources();
		return new DamageSource(attacked != null && source != null ? damageSources.lemonadePlayerDamageType : damageSources.lemonadeDamageType, source, attacked);
	}

	public static DamageSource toxicGoo(Level level) {
		return level.pc$damageSources().toxicGooDamage;
	}

	public static DamageSource disintegration(Level level, Entity attacked) {
		Reference<DamageType> type = getDisintegrationDamageType(attacked, level.pc$damageSources());
		if (attacked instanceof LivingEntity livingEntity)
			return new DamageSource(type, null, livingEntity.getKillCredit());
		return new DamageSource(type, null, null);
	}

	private static Holder.Reference<DamageType> getDisintegrationDamageType(Entity damaged, PortalCubedDamageSources sources) {
		if (damaged instanceof PlayerExt player && player.pc$hasSubmergedTheOperationalEndOfTheDevice()) {
			player.pc$setHasSubmergedTheOperationalEndOfTheDevice(false);
			return sources.submergedTheOperationalEndOfTheDeviceDamageType;
		} else {
			return sources.disintegrationDamageType;
		}
	}

	public static class LandingDamageSource extends DamageSource {
		LandingDamageSource(Holder<DamageType> type, Entity source, Entity attacker) {
			super(type, source, attacker);
		}

		@NotNull
		@Override
		public Component getLocalizedDeathMessage(LivingEntity attacked) {
			String id = "death.attack." + this.type().msgId();
			Component sourceName = Objects.requireNonNull(this.getDirectEntity()).getDisplayName();
			if (!(this.getEntity() instanceof LivingEntity attacker))
				return Component.translatable(id, attacked.getDisplayName(), sourceName);
			Component causeName = this.getEntity().getDisplayName();
			ItemStack attackerHeldStack = attacker.getMainHandItem();
			return !attackerHeldStack.isEmpty() && attackerHeldStack.getCustomName() != null
				? Component.translatable(id + ".item", attacked.getDisplayName(), sourceName, causeName, attackerHeldStack.getDisplayName())
				: Component.translatable(id + ".player", attacked.getDisplayName(), sourceName, causeName);
		}
	}
}
