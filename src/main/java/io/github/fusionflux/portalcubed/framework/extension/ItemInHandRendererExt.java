package io.github.fusionflux.portalcubed.framework.extension;

import org.quiltmc.loader.api.minecraft.ClientOnly;

import io.github.fusionflux.portalcubed.content.cannon.CannonUseResult;

@ClientOnly
public interface ItemInHandRendererExt {
	void pc$constructionCannonShoot(CannonUseResult useResult);
}
