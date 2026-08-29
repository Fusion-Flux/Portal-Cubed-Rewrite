package io.github.fusionflux.portalcubed.mixin.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import it.unimi.dsi.fastutil.longs.Long2ReferenceMap;
import net.caffeinemc.mods.sodium.client.render.chunk.RenderSection;
import net.caffeinemc.mods.sodium.client.render.chunk.RenderSectionManager;
import net.caffeinemc.mods.sodium.client.render.chunk.lists.SortedRenderLists;

@Mixin(value = RenderSectionManager.class, remap = false)
public interface RenderSectionManagerAccessor {
	@Accessor
	void setRenderLists(SortedRenderLists renderLists);

	@Accessor
	Long2ReferenceMap<RenderSection> getSectionByPosition();

	@Accessor
	int getLastUpdatedFrame();
}
