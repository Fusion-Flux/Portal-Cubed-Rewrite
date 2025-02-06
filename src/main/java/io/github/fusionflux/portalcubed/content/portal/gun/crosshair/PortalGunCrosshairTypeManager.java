package io.github.fusionflux.portalcubed.content.portal.gun.crosshair;

import java.util.Map;
import java.util.stream.Collectors;

import org.jetbrains.annotations.Nullable;

import io.github.fusionflux.portalcubed.PortalCubed;
import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;
import net.minecraft.resources.FileToIdConverter;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;

public final class PortalGunCrosshairTypeManager extends SimpleJsonResourceReloadListener<PortalGunCrosshairType> implements IdentifiableResourceReloadListener {
	public static final ResourceLocation ID = PortalCubed.id("portal_gun_crosshair_types");
	public static final FileToIdConverter CONVERTER = FileToIdConverter.json("portal_gun_crosshair_types");

	public static final PortalGunCrosshairTypeManager INSTANCE = new PortalGunCrosshairTypeManager();

	private Map<ResourceKey<PortalGunCrosshairType>, PortalGunCrosshairType> types = Map.of();

	private PortalGunCrosshairTypeManager() {
		super(PortalGunCrosshairType.CODEC, CONVERTER);
	}

	@Override
	public ResourceLocation getFabricId() {
		return ID;
	}

	@Override
	protected void apply(Map<ResourceLocation, PortalGunCrosshairType> types, ResourceManager manager, ProfilerFiller profiler) {
		this.types = types.entrySet().stream()
				.collect(Collectors.toUnmodifiableMap(entry -> ResourceKey.create(PortalGunCrosshairType.REGISTRY_KEY, entry.getKey()), Map.Entry::getValue));
	}

	@Nullable
	public PortalGunCrosshairType get(ResourceKey<PortalGunCrosshairType> key) {
		return this.types.get(key);
	}
}
