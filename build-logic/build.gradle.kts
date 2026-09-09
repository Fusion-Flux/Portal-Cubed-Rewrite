plugins {
	`java-gradle-plugin`
}

repositories {
	mavenCentral()
}

gradlePlugin {
	plugins {
		create("build-logic") {
			id = "io.github.fusionflux.portalcubed.build-logic"
			implementationClass = "io.github.fusionflux.portalcubed.build_logic.PortalCubedGradlePlugin"
		}
	}
}
