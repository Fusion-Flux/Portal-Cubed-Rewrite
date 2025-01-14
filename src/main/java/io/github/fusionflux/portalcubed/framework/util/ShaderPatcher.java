package io.github.fusionflux.portalcubed.framework.util;

import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.regex.Pattern;

import io.github.fusionflux.portalcubed.PortalCubed;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.client.renderer.ShaderProgramConfig;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;

public class ShaderPatcher {
	public static final String CLIPPING_PLANE_UNIFORM_NAME = PortalCubed.ID + "_ClippingPlane";
	public static final ShaderProgramConfig.Uniform CLIPPING_PLANE_UNIFORM_CONFIG = new ShaderProgramConfig.Uniform(CLIPPING_PLANE_UNIFORM_NAME, "float", 4, List.of(0f, 0f, 0f, 1f));

	private static final String CLIPPING_PLANE_UNIFORM_INJECTION = String.format("uniform vec4 %s;\n", CLIPPING_PLANE_UNIFORM_NAME);
	private static final String PROJECTION_MATRIX_PLACEHOLDER = "{projectionMatrix}";
	private static final String CLIPPING_INJECTION = String.format("gl_ClipDistance[0] = dot(%s.xyz, (inverse(%s) * gl_Position).xyz) + %1$1s.w;\n", CLIPPING_PLANE_UNIFORM_NAME, PROJECTION_MATRIX_PLACEHOLDER);

	private static final Object2ObjectOpenHashMap<String, String> CACHE = new Object2ObjectOpenHashMap<>();

	public static boolean shouldPatch(String name) {
		return ShaderType.matches(name).isPresent();
	}

	public static Optional<String> tryPatch(String src, String name) {
		return ShaderType
				.matches(name)
				.map(type -> CACHE.computeIfAbsent(name, $ -> patch(type, src)));
	}

	private static String patch(ShaderType type, String src) {
		StringBuilder builder = new StringBuilder();
		boolean insertedUniform = false;
		boolean foundMain = false;
		for (String line : src.split("\n")) {
			builder.append(line).append("\n");
			if (line.contains("#version") && !insertedUniform) {
				builder.append(CLIPPING_PLANE_UNIFORM_INJECTION);
				insertedUniform = true;
			} else if (line.contains("void main() {")) {
				foundMain = true;
			} else if (line.contains("gl_Position =") && foundMain) {
				builder.append(CLIPPING_INJECTION.replace(PROJECTION_MATRIX_PLACEHOLDER, type.projectionMatrixName));
			}
		}
		return builder.toString();
	}

	private enum ShaderType {
		SODIUM(Pattern.compile("sodium:blocks/block_layer_opaque\\.vsh"), "u_ProjectionMatrix"),
		VANILLA(Pattern.compile("^(minecraft:core/(?!gui|lightmap|blit_screen).*)\\.vsh"), "ProjMat");

		private final Predicate<String> matcher;
		public final String projectionMatrixName;

		ShaderType(Pattern pattern, String projectionMatrixName) {
			this.matcher = pattern.asMatchPredicate();
			this.projectionMatrixName = projectionMatrixName;
		}

		public static Optional<ShaderType> matches(String name) {
			for (ShaderType type : values()) {
				if (type.matcher.test(name))
					return Optional.of(type);
			}
			return Optional.empty();
		}
	}

	public enum ReloadListener implements SimpleSynchronousReloadListener {
		INSTANCE;

		public static final ResourceLocation ID = PortalCubed.id("shader_patcher");

		@Override
		public ResourceLocation getFabricId() {
			return ID;
		}

		@Override
		public void onResourceManagerReload(ResourceManager manager) {
			CACHE.clear();
		}
	}
}
