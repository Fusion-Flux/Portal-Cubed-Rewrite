package io.github.fusionflux.portalcubed.build_logic;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.stream.Stream;

import org.gradle.api.DefaultTask;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.tasks.InputDirectory;
import org.gradle.api.tasks.OutputDirectory;
import org.gradle.api.tasks.SkipWhenEmpty;
import org.gradle.api.tasks.TaskAction;
import org.jspecify.annotations.Nullable;

// this system is based on the one used by Ponder, and therefore Flywheel and Fabric API as well.
// https://github.com/Creators-of-Create/Ponder/blob/mc26.1/dev/build-logic/src/main/kotlin/net/createmod/pondergradle/nullability/GeneratePackageInfosTask.kt
public abstract class GeneratePackageInfosTask extends DefaultTask {
	@SkipWhenEmpty
	@InputDirectory
	public abstract DirectoryProperty getSourceRoot();

	@OutputDirectory
	public abstract DirectoryProperty getOutputDirectory();

	@TaskAction
	public void run() throws IOException {
		Path root = this.getSourceRoot().get().getAsFile().toPath();
		Path output = this.getOutputDirectory().get().getAsFile().toPath();

		// clear the output
		deleteRecursively(output);

		PackageInfoGenerator generator = new PackageInfoGenerator(root, output);
		generator.run();

		this.getLogger().quiet("Generated {} package-infos.", generator.generated);
	}

	private static boolean containsJavaFiles(Path dir) throws IOException {
		try (Stream<Path> stream = Files.list(dir)) {
			return stream.anyMatch(GeneratePackageInfosTask::isJavaFile);
		}
	}

	private static boolean isJavaFile(Path file) {
		return Files.isRegularFile(file) && file.getFileName().toString().endsWith(".java");
	}

	private static void deleteRecursively(Path dir) throws IOException {
		Files.walkFileTree(dir, new SimpleFileVisitor<>() {
			@Override
			public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
				Files.delete(file);
				return FileVisitResult.CONTINUE;
			}

			@Override
			public FileVisitResult postVisitDirectory(Path dir, @Nullable IOException exc) throws IOException {
				Files.delete(dir);
				return FileVisitResult.CONTINUE;
			}
		});
	}

	private static final class PackageInfoGenerator {
		private static final String PACKAGE_INFO = "package-info.java";
		private static final String PACKAGE_PLACEHOLDER = "%PACKAGE%";
		private static final String TEMPLATE = """
			@NullMarked
			package %PACKAGE%;

			import org.jspecify.annotations.NullMarked;
			""";

		private final Path root;
		private final Path output;

		private int generated;

		private PackageInfoGenerator(Path root, Path output) {
			this.root = root;
			this.output = output;
		}

		public void run() throws IOException {
			Files.walkFileTree(this.root, new SimpleFileVisitor<>() {
				@Override
				public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
					PackageInfoGenerator.this.visit(dir);
					return FileVisitResult.CONTINUE;
				}
			});
		}

		private void visit(Path dir) throws IOException {
			if (!containsJavaFiles(dir))
				return;

			Path packageInfo = dir.resolve(PACKAGE_INFO);
			if (Files.exists(packageInfo))
				return;

			Path relative = this.root.relativize(dir);
			Path targetParent = this.output.resolve(relative);
			Path target = targetParent.resolve(PACKAGE_INFO);
			String separator = relative.getFileSystem().getSeparator();
			String packageName = relative.toString().replace(separator, ".");
			String content = TEMPLATE.replace(PACKAGE_PLACEHOLDER, packageName);

			Files.createDirectories(targetParent);
			Files.writeString(target, content);

			this.generated++;
		}
	}
}
