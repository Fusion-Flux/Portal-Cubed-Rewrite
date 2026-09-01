package io.github.fusionflux.portalcubed.content.portal.gun.skin;

import java.util.Map;
import java.util.stream.Collectors;

import org.jetbrains.annotations.Nullable;

import com.mojang.serialization.JsonOps;

import io.github.fusionflux.portalcubed.PortalCubed;
import net.minecraft.client.multiplayer.ClientRegistryLayer;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.FileToIdConverter;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.PlaceholderLookupProvider;
import net.minecraft.util.profiling.ProfilerFiller;

public final class PortalGunSkinManager extends SimpleJsonResourceReloadListener<PortalGunSkin> {
	public static final Identifier ID = PortalCubed.id("portal_gun_skins");
	public static final FileToIdConverter ASSET_LISTER = FileToIdConverter.registry(PortalGunSkin.REGISTRY_KEY);

	public static final PortalGunSkinManager INSTANCE = new PortalGunSkinManager();

	private Map<ResourceKey<PortalGunSkin>, PortalGunSkin> skins = Map.of();

	private PortalGunSkinManager() {
		// Registry access for sound events, ClientItemInfoLoader uses this too
		RegistryAccess.Frozen staticRegistries = ClientRegistryLayer.createRegistryAccess().compositeAccess();
		PlaceholderLookupProvider lookup = new PlaceholderLookupProvider(staticRegistries);
		super(lookup.createSerializationContext(JsonOps.INSTANCE), PortalGunSkin.CODEC, ASSET_LISTER);
	}

	@Override
	protected void apply(Map<Identifier, PortalGunSkin> skins, ResourceManager manager, ProfilerFiller profiler) {
		this.skins = skins.entrySet().stream()
				.collect(Collectors.toUnmodifiableMap(entry -> ResourceKey.create(PortalGunSkin.REGISTRY_KEY, entry.getKey()), Map.Entry::getValue));
	}

	@Nullable
	public PortalGunSkin get(ResourceKey<PortalGunSkin> key) {
		return this.skins.get(key);
	}
}
