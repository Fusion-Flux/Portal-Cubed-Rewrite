package io.github.fusionflux.portalcubed.content.portal.renderer;

import java.lang.reflect.Field;

import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.minecraft.Util;
import net.minecraft.client.renderer.LevelRenderer;
import sun.misc.Unsafe;

public interface WorldRenderContextHook {
	WorldRenderContextHook INSTANCE = Util.make(() -> {
		try {
			for (Field field : LevelRenderer.class.getDeclaredFields()) {
				if (WorldRenderContext.class.isAssignableFrom(field.getType())) {
					// unsafe needs to be used here because it's a private final field in a mixin, I wish there was a better way
					Field unsafeField = Unsafe.class.getDeclaredField("theUnsafe");
					unsafeField.setAccessible(true);
					Unsafe unsafe = (Unsafe) unsafeField.get(null);

					@SuppressWarnings("deprecation")
					long fieldOffset = unsafe.objectFieldOffset(field);
					return new WorldRenderContextHook() {
						@Override
						public void set(LevelRenderer levelRenderer, WorldRenderContext newContext) {
							unsafe.putObject(levelRenderer, fieldOffset, newContext);
						}

						@Override
						public WorldRenderContext get(LevelRenderer levelRenderer) {
							return (WorldRenderContext) unsafe.getObject(levelRenderer, fieldOffset);
						}
					};
				}
			}

			throw new IllegalStateException("Failed to find WorldRenderContext.. how?");
		} catch (Throwable t) {
			throw new RuntimeException("[PortalCubed] Error while trying to retrieve hook to WRC!", t);
		}
	});

	void set(LevelRenderer levelRenderer, WorldRenderContext newContext);
	WorldRenderContext get(LevelRenderer levelRenderer);
}
