package io.github.fusionflux.portalcubed.framework.util;

import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;

public interface SimpleSynchronousReloadListener extends ResourceManagerReloadListener, IdentifiableResourceReloadListener {
}
