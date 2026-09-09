package io.github.fusionflux.portalcubed.build_logic;

import java.io.File;

import javax.inject.Inject;

import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.provider.Provider;
import org.gradle.api.tasks.Delete;
import org.gradle.api.tasks.SourceSet;
import org.gradle.api.tasks.TaskContainer;

public abstract class PackageInfosExtension {
	@Inject
	protected abstract Project getProject();

	public void sourceSets(Iterable<Provider<SourceSet>> sourceSetProviders) {
		sourceSetProviders.forEach(this::sourceSet);
	}

	public void sourceSet(Provider<SourceSet> sourceSetProvider) {
		SourceSet sourceSet = sourceSetProvider.get();
		String name = sourceSet.getName();

		Project project = this.getProject();
		TaskContainer tasks = project.getTasks();

		String generateTaskName = sourceSet.getTaskName("generate", "PackageInfos");
		File source = project.file("src/" + name + "/java");
		File output = project.file("build/generatedPackageInfos/" + name);

		Provider<? extends Task> generateTask = tasks.register(generateTaskName, GeneratePackageInfosTask.class, task -> {
			task.setGroup(PortalCubedGradlePlugin.TASK_GROUP);
			task.setDescription("Generates package-info files for the " + name + " SourceSet.");

			task.getSourceRoot().set(source);
			task.getOutputDirectory().set(output);
		});

		sourceSet.getJava().srcDir(output);
		project.getTasks().named("ideaSyncTask", task -> task.finalizedBy(generateTask));

		String cleanTaskName = sourceSet.getTaskName("clean", "PackageInfos");
		Provider<? extends Task> cleanTask = tasks.register(cleanTaskName, Delete.class, task -> {
			task.setGroup(PortalCubedGradlePlugin.TASK_GROUP);
			task.setDescription("Cleans generated package-info files for the " + name + " SourceSet.");
			task.delete(output);
		});

		tasks.named("clean", task -> task.dependsOn(cleanTask));
	}
}
