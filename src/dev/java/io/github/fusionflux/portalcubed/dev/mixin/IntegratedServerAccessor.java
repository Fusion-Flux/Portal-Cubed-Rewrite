package io.github.fusionflux.portalcubed.dev.mixin;

import java.util.List;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.client.server.IntegratedServer;
import net.minecraft.gizmos.SimpleGizmoCollector;

@Mixin(IntegratedServer.class)
public interface IntegratedServerAccessor {
	@Accessor
	void setLatestTicksGizmos(List<SimpleGizmoCollector.GizmoInstance> gizmos);

	@Accessor
	SimpleGizmoCollector getGizmoCollector();
}
