package io.github.fusionflux.portalcubed.framework.registration;

import net.minecraft.client.renderer.RenderType;

import org.quiltmc.loader.api.minecraft.ClientOnly;

import java.util.function.Supplier;

public enum RenderTypes {
	SOLID(() -> () -> RenderType::solid),
	CUTOUT(() -> () -> RenderType::cutout),
	TRANSLUCENT(() -> () -> RenderType::translucent);

	private final Supplier<Supplier<Supplier<RenderType>>> supplier;

	RenderTypes(Supplier<Supplier<Supplier<RenderType>>> supplier) {
		this.supplier = supplier;
	}

	@ClientOnly
	public RenderType vanilla() {
		return supplier.get().get().get();
	}
}
