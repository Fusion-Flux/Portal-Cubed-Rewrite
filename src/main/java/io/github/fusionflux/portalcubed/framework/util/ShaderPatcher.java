package io.github.fusionflux.portalcubed.framework.util;

import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.regex.Pattern;

import org.joml.Vector4f;

import io.github.fusionflux.portalcubed.PortalCubed;
import it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenHashMap;
import net.minecraft.Util;
import net.minecraft.client.renderer.ShaderProgramConfig;

public class ShaderPatcher {
	public static final Vector4f[] CLIPPING_PLANES = {new Vector4f(), new Vector4f()};
	public static final ShaderProgramConfig.Uniform[] CLIPPING_PLANE_UNIFORMS = Util.make(new ShaderProgramConfig.Uniform[CLIPPING_PLANES.length], uniforms -> {
		for (int i = 0; i < uniforms.length; i++) {
			//noinspection StringConcatenationMissingWhitespace
			uniforms[i] = new ShaderProgramConfig.Uniform(PortalCubed.ID + "_ClippingPlane" + i, "float", 4, List.of(0f, 0f, 0f, 1f));
		}
	});

	private static final String VIEWSPACE_VERTEX_VARIABLE = "portalcubed_viewspace_vertex_pos";
	private static final String VIEWSPACE_VERTEX_INJECTION = String.format("    vec3 %s = (inverse(%%s) * gl_Position).xyz;\n", VIEWSPACE_VERTEX_VARIABLE);
	private static final String CLIPPING_INJECTION = String.format("    gl_ClipDistance[%%s] = dot(%%s.xyz, %s) + %%2$2s.w;\n", VIEWSPACE_VERTEX_VARIABLE);

	public static final ShaderProgramConfig.Uniform DISINTEGRATION_COLOR_MODIFIER_UNIFORM = new ShaderProgramConfig.Uniform(
			PortalCubed.ID + "_DisintegrationColorModifier", "float", 4, List.of(1f, 1f, 1f, 1f)
	);
	private static final String DISINTEGRATION_COLOR_MODIFIER_INJECTION = String.format("    color *= %s;\n", DISINTEGRATION_COLOR_MODIFIER_UNIFORM.name());

	private static final int MAX_CACHE_SIZE = 50;
	private static final Object2ObjectLinkedOpenHashMap<CacheKey, Optional<String>> CACHE = new Object2ObjectLinkedOpenHashMap<>(MAX_CACHE_SIZE, 0.75f);

	public static void injectUniforms(ShaderProgramConfig config, Consumer<ShaderProgramConfig.Uniform> adder) {
		Set<ShaderType> matchedTypes = EnumSet.noneOf(ShaderType.class);
		ShaderType.matches(config.vertex() + ".vsh").ifPresent(matchedTypes::add);
		ShaderType.matches(config.fragment() + ".fsh").ifPresent(matchedTypes::add);
		matchedTypes.forEach(type -> type.uniforms.forEach(adder));
	}

	public static Optional<String> tryPatch(String src, String name) {
		if (CACHE.size() == MAX_CACHE_SIZE)
			CACHE.removeFirst();
		return CACHE.computeIfAbsent(
				new CacheKey(name, src),
				$ -> ShaderType.matches(name)
						.map(type -> patch(type, src))
		);
	}

	private static String patch(ShaderType type, String src) {
		StringBuilder builder = new StringBuilder();
		boolean insertedUniforms = false;
		boolean foundMain = false;
		for (String line : src.split("\n")) {
			if (type == ShaderType.DISINTEGRATION_COLOR_MODIFIER) {
				if (line.contains("fragColor =") && foundMain)
					builder.append(DISINTEGRATION_COLOR_MODIFIER_INJECTION);
			}

			builder.append(line).append("\n");
			if (line.contains("uniform ") && !insertedUniforms) {
				type.uniforms.forEach(uniform -> builder.append(String.format("uniform vec4 %s;\n", uniform.name())));
				insertedUniforms = true;
			} else if (line.contains("void main() {")) {
				foundMain = true;
			}

			if (type == ShaderType.VANILLA_CLIPPING_PLANE || type == ShaderType.SODIUM_CLIPPING_PLANE) {
				if (line.contains("gl_Position =") && foundMain) {
					//noinspection OptionalGetWithoutIsPresent
					builder.append(String.format(VIEWSPACE_VERTEX_INJECTION, type.projectionMatrixName.get()));
					for (int i = 0; i < ShaderPatcher.CLIPPING_PLANE_UNIFORMS.length; i++) {
						builder.append(String.format(CLIPPING_INJECTION, i, ShaderPatcher.CLIPPING_PLANE_UNIFORMS[i].name()));
					}
				}
			}
		}
		return builder.toString();
	}

	private enum ShaderType {
		SODIUM_CLIPPING_PLANE(
				Pattern.compile("sodium:blocks/block_layer_opaque\\.vsh"),
				"u_ProjectionMatrix",
				CLIPPING_PLANE_UNIFORMS
		),
		VANILLA_CLIPPING_PLANE(
				Pattern.compile("^(minecraft:core/(?!gui|lightmap|blit_screen).*)\\.vsh"),
				"ProjMat",
				CLIPPING_PLANE_UNIFORMS
		),
		DISINTEGRATION_COLOR_MODIFIER(
				Pattern.compile("^(minecraft:core/(?!position|gui|lightmap|blit_screen|rendertype_(leash|end_portal|lightning|clouds|water_mask)).*)\\.fsh"),
				DISINTEGRATION_COLOR_MODIFIER_UNIFORM
		);

		public final Predicate<String> matcher;
		public final List<ShaderProgramConfig.Uniform> uniforms;
		public final Optional<String> projectionMatrixName;

		ShaderType(Pattern pattern, ShaderProgramConfig.Uniform... uniforms) {
			this(pattern, Optional.empty(), uniforms);
		}

		ShaderType(Pattern pattern, String projectionMatrixName, ShaderProgramConfig.Uniform... uniforms) {
			this(pattern, Optional.of(projectionMatrixName), uniforms);
		}

		ShaderType(Pattern pattern, Optional<String> projectionMatrixName, ShaderProgramConfig.Uniform... uniforms) {
			this.matcher = pattern.asMatchPredicate();
			this.uniforms = List.of(uniforms);
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

	private record CacheKey(String name, String src) {
	}
}
