package io.github.fusionflux.portalcubed.framework.util;

import java.util.Optional;
import java.util.function.Predicate;
import java.util.regex.Pattern;

import io.github.fusionflux.portalcubed.PortalCubed;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;

public class ShaderPatcher {
	public static final String CLIPPING_PLANE_UNIFORM_NAME = PortalCubed.ID + "_ClippingPlane";

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
		for (String line : src.split("\n")) {
			builder.append(line).append("\n");
			if (line.contains("#version")) {
				builder.append(CLIPPING_PLANE_UNIFORM_INJECTION);
			} else if (line.contains("gl_Position =")) {
				builder.append(CLIPPING_INJECTION.replace(PROJECTION_MATRIX_PLACEHOLDER, type.projectionMatrixName));
			}
		}
		return builder.toString();
	}

	public static void clearCache() {
		CACHE.clear();
	}

	private enum ShaderType {
		SODIUM(Pattern.compile("sodium:blocks/block_layer_opaque\\.vsh"), "u_ProjectionMatrix"),
		VANILLA(Pattern.compile("^((particle)|(rendertype_(?!gui).*))\\.vsh"), "ProjMat");

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
}
