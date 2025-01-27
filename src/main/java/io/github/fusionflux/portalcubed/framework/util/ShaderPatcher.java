package io.github.fusionflux.portalcubed.framework.util;

import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.regex.Pattern;

import io.github.fusionflux.portalcubed.PortalCubed;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.client.renderer.ShaderProgramConfig;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;

public class ShaderPatcher {
	public static final String CLIPPING_PLANE_UNIFORM_NAME = PortalCubed.ID + "_ClippingPlane";
	public static final ShaderProgramConfig.Uniform CLIPPING_PLANE_UNIFORM = new ShaderProgramConfig.Uniform(CLIPPING_PLANE_UNIFORM_NAME, "float", 4, List.of(0f, 0f, 0f, 1f));
	public static final ShaderProgramConfig.Uniform DISINTEGRATION_COLOR_MODIFIER_UNIFORM = new ShaderProgramConfig.Uniform(
			PortalCubed.ID + "_DisintegrationColorModifier", "float", 4, List.of(1f, 1f, 1f, 1f)
	);

	private static final String PROJECTION_MATRIX_PLACEHOLDER = "{projectionMatrix}";
	private static final String CLIPPING_INJECTION = String.format("    gl_ClipDistance[0] = dot(%s.xyz, (inverse(%s) * gl_Position).xyz) + %1$1s.w;\n", CLIPPING_PLANE_UNIFORM_NAME, PROJECTION_MATRIX_PLACEHOLDER);
	private static final String DISINTEGRATION_COLOR_MODIFIER_INJECTION = String.format("    color *= %s;\n", DISINTEGRATION_COLOR_MODIFIER_UNIFORM.name());

	private static final Object2ObjectOpenHashMap<CacheKey, String> CACHE = new Object2ObjectOpenHashMap<>();

	public static void injectUniforms(ShaderProgramConfig config, Consumer<ShaderProgramConfig.Uniform> adder) {
		Set<ShaderType> matchedTypes = EnumSet.noneOf(ShaderType.class);
		ShaderType.matches(config.vertex() + ".vsh").ifPresent(matchedTypes::add);
		ShaderType.matches(config.fragment() + ".fsh").ifPresent(matchedTypes::add);
		matchedTypes.forEach(type -> type.uniforms.forEach(adder));
	}

	public static Optional<String> tryPatch(String src, String name) {
		return ShaderType
				.matches(name)
				.map(type -> CACHE.computeIfAbsent(new CacheKey(name, src), $ -> patch(type, src)));
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
				if (line.contains("gl_Position =") && foundMain)
					builder.append(CLIPPING_INJECTION.replace(PROJECTION_MATRIX_PLACEHOLDER, type.projectionMatrixName.get()));
			}
		}
		return builder.toString();
	}

	private enum ShaderType {
		SODIUM_CLIPPING_PLANE(
				Pattern.compile("sodium:blocks/block_layer_opaque\\.vsh"),
				"u_ProjectionMatrix",
				CLIPPING_PLANE_UNIFORM
		),
		VANILLA_CLIPPING_PLANE(
				Pattern.compile("^(minecraft:core/(?!gui|lightmap|blit_screen).*)\\.vsh"),
				"ProjMat",
				CLIPPING_PLANE_UNIFORM
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

	private record CacheKey(String name, String src) {}

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
