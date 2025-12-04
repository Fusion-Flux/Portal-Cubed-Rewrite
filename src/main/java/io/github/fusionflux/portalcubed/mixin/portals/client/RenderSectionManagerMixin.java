package io.github.fusionflux.portalcubed.mixin.portals.client;

import java.util.ArrayDeque;
import java.util.Map;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;

import io.github.fusionflux.portalcubed.content.portal.graphics.render.PortalRenderer;
import io.github.fusionflux.portalcubed.content.portal.graphics.render.RecursionAttachedResource;
import net.caffeinemc.mods.sodium.client.render.chunk.ChunkUpdateType;
import net.caffeinemc.mods.sodium.client.render.chunk.RenderSection;
import net.caffeinemc.mods.sodium.client.render.chunk.RenderSectionManager;
import net.caffeinemc.mods.sodium.client.render.chunk.lists.SortedRenderLists;
import net.caffeinemc.mods.sodium.client.render.chunk.translucent_sorting.trigger.CameraMovement;
import net.minecraft.Util;

@Mixin(value = RenderSectionManager.class, remap = false)
public class RenderSectionManagerMixin {
	@Unique
	private RecursionAttachedResource<SortedRenderLists> portalRenderLists;
	@Unique
	private RecursionAttachedResource<Map<ChunkUpdateType, ArrayDeque<RenderSection>>> portalTaskLists;

	@Inject(method = "<init>", at = @At("TAIL"))
	private void init(CallbackInfo ci) {
		this.portalRenderLists = RecursionAttachedResource.create(SortedRenderLists::empty);
		this.portalTaskLists = RecursionAttachedResource.create(() -> Util.makeEnumMap(ChunkUpdateType.class, $ -> new ArrayDeque<>()));
	}

	@WrapOperation(
			method = "update",
			at = @At(
					value = "FIELD",
					target = "Lnet/caffeinemc/mods/sodium/client/render/chunk/RenderSectionManager;needsGraphUpdate:Z"
			)
	)
	private void graphStillNeedsUpdating(RenderSectionManager instance, boolean value, Operation<Void> original) {
		original.call(instance, PortalRenderer.isRenderingView());
	}

	@WrapOperation(
			method = {"resetRenderLists", "createTerrainRenderList"},
			at = @At(
					value = "FIELD",
					target = "Lnet/caffeinemc/mods/sodium/client/render/chunk/RenderSectionManager;renderLists:Lnet/caffeinemc/mods/sodium/client/render/chunk/lists/SortedRenderLists;"
			)
	)
	private void setPortalRenderLists(RenderSectionManager instance, SortedRenderLists value, Operation<Void> original) {
		if (PortalRenderer.isRenderingView()) {
			this.portalRenderLists.set(value);
		} else {
			original.call(instance, value);
		}
	}

	@ModifyExpressionValue(
			method = {"getRenderLists", "renderLayer", "getVisibleChunkCount"},
			at = @At(
					value = "FIELD",
					target = "Lnet/caffeinemc/mods/sodium/client/render/chunk/RenderSectionManager;renderLists:Lnet/caffeinemc/mods/sodium/client/render/chunk/lists/SortedRenderLists;"
			)
	)
	private SortedRenderLists usePortalRenderLists(SortedRenderLists original) {
		return PortalRenderer.isRenderingView() ? this.portalRenderLists.get() : original;
	}

	@WrapOperation(
			method = "createTerrainRenderList",
			at = @At(
					value = "FIELD",
					target = "Lnet/caffeinemc/mods/sodium/client/render/chunk/RenderSectionManager;taskLists:Ljava/util/Map;"
			)
	)
	private void setPortalTaskLists(RenderSectionManager instance, Map<ChunkUpdateType, ArrayDeque<RenderSection>> value, Operation<Void> original) {
		if (PortalRenderer.isRenderingView()) {
			this.portalTaskLists.set(value);
		} else {
			original.call(instance, value);
		}
	}

	@ModifyExpressionValue(
			method = {
					"resetRenderLists",
					"submitSectionTasks(Lnet/caffeinemc/mods/sodium/client/render/chunk/compile/executor/ChunkJobCollector;Lnet/caffeinemc/mods/sodium/client/render/chunk/ChunkUpdateType;Z)V"
			},
			at = @At(
					value = "FIELD",
					target = "Lnet/caffeinemc/mods/sodium/client/render/chunk/RenderSectionManager;taskLists:Ljava/util/Map;"
			)
	)
	private Map<ChunkUpdateType, ArrayDeque<RenderSection>> usePortalTaskLists(Map<ChunkUpdateType, ArrayDeque<RenderSection>> original) {
		return PortalRenderer.isRenderingView() ? this.portalTaskLists.get() : original;
	}

	@WrapMethod(method = "processGFNIMovement")
	private void dontProcessGFNIMovementForPortalCamera(CameraMovement movement, Operation<Void> original) {
		if (!PortalRenderer.isRenderingView())
			original.call(movement);
	}
}
