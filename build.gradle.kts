// versions
val minecraftVersion = "1.21.4"
val minecraftDep = "=1.21.4"
// https://parchmentmc.org/docs/getting-started
val parchmentVersion = "2025.02.16"
// https://fabricmc.net/develop
val loaderVersion = "0.16.10"
val fapiVersion = "0.117.0+1.21.4"
// https://modrinth.com/mod/sodium/versions?l=fabric
val sodiumVersion = "0.6.9"
// https://github.com/TerraformersMC/Terraform/releases
val woodApiVersion = "13.0.0-alpha.2"
// https://github.com/LlamaLad7/MixinExtras/releases
val mixinExtrasVersion = "0.5.0-beta.5"

// dev env mods
// https://modrinth.com/mod/modmenu/versions
val modmenuVersion = "13.0.2"

// buildscript
plugins {
    id("fabric-loom") version "1.10.+"
    id("maven-publish")
}

base.archivesName = "portalcubed"
group = "io.github.fusionflux"

val isRelease = providers.environmentVariable("IS_RELEASE")
    .map(String::toBoolean)
    .getOrElse(false)
val buildNum = providers.environmentVariable("GITHUB_RUN_NUMBER")
    .filter(String::isNotEmpty)
    .map { "-build.$it" }
    .orElse("-local")
    .filter { !isRelease }
    .getOrElse("")

version = "3.0.0-alpha.2+mc$minecraftVersion$buildNum"

repositories {
    maven("https://maven.parchmentmc.org")
    maven("https://api.modrinth.com/maven")
    maven("https://maven.terraformersmc.com/releases/")
}

dependencies {
    // dev environment
    minecraft("com.mojang:minecraft:$minecraftVersion")
    mappings(loom.layered {
        officialMojangMappings { nameSyntheticMembers = false }
        parchment("org.parchmentmc.data:parchment-$minecraftVersion:$parchmentVersion@zip")
    })
    modImplementation("net.fabricmc:fabric-loader:$loaderVersion")

    // dependencies
    modImplementation("net.fabricmc.fabric-api:fabric-api:$fapiVersion")
    modImplementation("com.terraformersmc.terraform-api:terraform-wood-api-v1:$woodApiVersion")
    implementation(annotationProcessor("io.github.llamalad7:mixinextras-fabric:$mixinExtrasVersion")!!)
    include("io.github.llamalad7:mixinextras-fabric:$mixinExtrasVersion:slim")
    modImplementation("maven.modrinth:sodium:mc$minecraftVersion-$sodiumVersion-fabric")

    // dev env
    modLocalRuntime("maven.modrinth:modmenu:$modmenuVersion")
}

tasks.withType(ProcessResources::class) {
    inputs.properties(
        "version" to version,
        "loader_version" to loaderVersion,
        "fapi_version" to fapiVersion,
        "minecraft_dependency" to minecraftDep,
        "wood_api_version" to woodApiVersion,
        "sodium_version" to "$sodiumVersion+$minecraftVersion"
    )

    filesMatching("fabric.mod.json") {
        expand(inputs.properties)
    }
}

val gametests: SourceSet by sourceSets.creating {
    val main: SourceSet = sourceSets["main"]
    compileClasspath += main.compileClasspath
    compileClasspath += main.output
    runtimeClasspath += main.runtimeClasspath
    runtimeClasspath += main.output
}

loom {
    accessWidenerPath = file("src/main/resources/portalcubed.accesswidener")

    runs {
        register("gametest") {
            server()
            source(gametests)
            ideConfigGenerated(false) // this is meant for CI
            property("fabric-api.gametest")
            property("fabric-api.gametest.report-file", "${layout.buildDirectory}/junit.xml")
            runDir("run/gametest_server")
        }

        named("client").configure {
            source(gametests)
            configName = "Client"
        }

        named("server").configure {
            configName = "Server"
        }

        configureEach {
            property("mixin.debug.export", "true")
            vmArg("-XX:+AllowEnhancedClassRedefinition")
            vmArg("-XX:+IgnoreUnrecognizedVMOptions")

            property("fabric.game_test.command", "true")
            property("fabric-tag-conventions-v2.missingTagTranslationWarning", "SILENCED")
            property("fabric-tag-conventions-v1.legacyTagWarning", "VERBOSE")
        }
    }
}

tasks.named<JavaCompile>("compileJava") {
    options.compilerArgs.add("-Xmaxerrs")
    options.compilerArgs.add("10000")
}
