package io.github.fusionflux.portalcubed.mixin.client;

import it.unimi.dsi.fastutil.longs.Long2ReferenceMap;
import me.jellysquid.mods.sodium.client.render.chunk.RenderSection;
import me.jellysquid.mods.sodium.client.render.chunk.RenderSectionManager;

import me.jellysquid.mods.sodium.client.render.chunk.lists.SortedRenderLists;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(RenderSectionManager.class)
public interface RenderSectionManagerAccessor {
	@Accessor
	void setRenderLists(SortedRenderLists renderLists);

	@Accessor
	Long2ReferenceMap<RenderSection> getSectionByPosition();

	@Accessor
	int getLastUpdatedFrame();
}
