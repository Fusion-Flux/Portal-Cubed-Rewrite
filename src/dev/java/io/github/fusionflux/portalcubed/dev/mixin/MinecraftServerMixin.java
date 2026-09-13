package io.github.fusionflux.portalcubed.dev.mixin;

import java.util.List;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;

import net.minecraft.client.server.IntegratedServer;
import net.minecraft.gizmos.Gizmos;
import net.minecraft.gizmos.SimpleGizmoCollector;
import net.minecraft.server.MinecraftServer;

@Mixin(MinecraftServer.class)
public class MinecraftServerMixin {
	@WrapOperation(
			method = "runServer",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/server/MinecraftServer;waitUntilNextTick()V"
			)
	)
	private void allowGizmosWhenRunningTasks(MinecraftServer self, Operation<Void> original) {
		if (!(self instanceof IntegratedServer server)) {
			original.call(self);
			return;
		}

		IntegratedServerAccessor accessor = (IntegratedServerAccessor) server;
		SimpleGizmoCollector collector = accessor.getGizmoCollector();

		try (Gizmos.TemporaryCollection ignored = Gizmos.withCollector(collector)) {
			original.call(self);
		}

		if (self.tickRateManager().runsNormally()) {
			List<SimpleGizmoCollector.GizmoInstance> drained = collector.drainGizmos();
			drained.addAll(server.getPerTickGizmos());
			accessor.setLatestTicksGizmos(drained);
		}
	}
}
