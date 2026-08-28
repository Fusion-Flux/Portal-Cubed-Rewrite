rootProject.name = "portal-cubed"

enableFeaturePreview("STABLE_CONFIGURATION_CACHE")
enableFeaturePreview("NO_IMPLICIT_LOOKUP_IN_PARENT_PROJECTS")

pluginManagement {
	repositories {
		mavenCentral()
		maven("https://maven.fabricmc.net/")
	}
}
