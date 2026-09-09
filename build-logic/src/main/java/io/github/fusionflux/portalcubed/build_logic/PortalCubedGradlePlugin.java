package io.github.fusionflux.portalcubed.build_logic;

import org.gradle.api.Plugin;
import org.gradle.api.Project;

public final class PortalCubedGradlePlugin implements Plugin<Project> {
	public static final String TASK_GROUP = "portalcubed";

	@Override
	public void apply(Project project) {
		project.getExtensions().create("defaultPackageInfos", PackageInfosExtension.class);
	}
}
