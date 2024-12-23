package io.github.fusionflux.portalcubed.framework.extension;

import java.util.Collection;

import org.jetbrains.annotations.Nullable;

public interface ClientSuggestionProviderExt {
	@Nullable
	String pc$getTargetedPortal();

	Collection<String> pc$getAllPortalKeys();
}
