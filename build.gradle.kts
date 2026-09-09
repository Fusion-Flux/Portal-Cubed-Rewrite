plugins {
	alias(libs.plugins.loom)
	alias(libs.plugins.build.logic)
	`maven-publish`
}

val isRelease: Provider<Boolean> = providers.environmentVariable("IS_RELEASE")
	.map(String::toBoolean)
	.orElse(false)
val buildNumber: Provider<String> = providers.environmentVariable("GITHUB_RUN_NUMBER")
	.filter(String::isNotEmpty)
	.map { "-build.$it" }
	.orElse("-local")
	.filter { !isRelease.get() }

val modVersion: Provider<String> = providers.gradleProperty("mod_version")
val minecraftDependency: Provider<String> = providers.gradleProperty("minecraft_dependency")

version = "${modVersion.get()}+mc${libs.versions.minecraft.get()}${buildNumber.get()}"
group = "io.github.fusionflux"

java {
	toolchain.languageVersion = JavaLanguageVersion.of(25)
	withSourcesJar()
}

dependencies {
	minecraft(libs.minecraft)
	implementation(libs.bundles.fabric)
}

tasks.processResources {
	val properties: Map<String, Any> = mapOf(
		"version" to project.version,
		"loader_version" to libs.versions.fabric.loader.get(),
		"fapi_version" to libs.versions.fabric.api.get(),
		"minecraft_dependency" to minecraftDependency.get()
	)

	filesMatching("fabric.mod.json") {
		expand(properties)
	}
}

//val gametests: Provider<SourceSet> = sourceSets.register("gametests") {
//	val main: SourceSet = sourceSets.main.get()
//	compileClasspath += main.compileClasspath
//	compileClasspath += main.output
//	runtimeClasspath += main.runtimeClasspath
//	runtimeClasspath += main.output
//}

defaultPackageInfos {
	sourceSet(sourceSets.main)
}

loom {
	accessWidenerPath = file("src/main/resources/portalcubed.accesswidener")

	runs {
//		register("gametest") {
//			server()
//			sourceSet = gametests.map(SourceSet::getName)
//			generateRunConfig = false // this is meant for CI
//			systemProperties.put("fabric-api.gametest", "true")
//			systemProperties.put("fabric-api.gametest.report-file", "${layout.buildDirectory}/junit.xml")
//			runDirectory = file("run/gametest_server")
//		}

		named("client").configure {
//			sourceSet = gametests.map(SourceSet::getName)
			displayName = "Client"
		}

		named("server").configure {
			displayName = "Server"
		}

		configureEach {
			preferGradleTask = true

			systemProperties.put("mixin.debug.export", "true")
			jvmArguments.add("-XX:+AllowEnhancedClassRedefinition")
			jvmArguments.add("-XX:+IgnoreUnrecognizedVMOptions")

			systemProperties.put("fabric.game_test.command", "true")
			systemProperties.put("fabric-tag-conventions-v2.missingTagTranslationWarning", "SILENCED")
			systemProperties.put("fabric-tag-conventions-v1.legacyTagWarning", "VERBOSE")
		}
	}
}

tasks.compileJava {
	options.compilerArgs.add("-Xmaxerrs")
	options.compilerArgs.add("10000")
}
