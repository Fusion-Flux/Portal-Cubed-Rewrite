package io.github.fusionflux.portalcubed.content.portal.gun.skin;

import java.util.Map;
import java.util.stream.Collectors;

import org.jetbrains.annotations.Nullable;

import io.github.fusionflux.portalcubed.PortalCubed;
import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;

public final class PortalGunSkinManager extends SimpleJsonResourceReloadListener<PortalGunSkin> implements IdentifiableResourceReloadListener {
	public static final ResourceLocation ID = PortalCubed.id("portal_gun_skins");

	public static final PortalGunSkinManager INSTANCE = new PortalGunSkinManager();

	private Map<ResourceKey<PortalGunSkin>, PortalGunSkin> skins = Map.of();

	private PortalGunSkinManager() {
		// sneak in registry access for sound events
		super(
				RegistryAccess.fromRegistryOfRegistries(BuiltInRegistries.REGISTRY),
				PortalGunSkin.CODEC,
				ResourceKey.createRegistryKey(ResourceLocation.withDefaultNamespace("portal_gun_skins"))
		);
	}

	@Override
	public ResourceLocation getFabricId() {
		return ID;
	}

	@Override
	protected void apply(Map<ResourceLocation, PortalGunSkin> skins, ResourceManager manager, ProfilerFiller profiler) {
		this.skins = skins.entrySet().stream()
				.collect(Collectors.toUnmodifiableMap(entry -> ResourceKey.create(PortalGunSkin.REGISTRY_KEY, entry.getKey()), Map.Entry::getValue));
	}

	@Nullable
	public PortalGunSkin get(ResourceKey<PortalGunSkin> key) {
		return this.skins.get(key);
	}
}
