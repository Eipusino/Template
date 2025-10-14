package template;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;

// Component class file handler for EntityProcessor
public class FetchProcessor {
	public static void start() {
		String sourceDir = "F:/jc/fetch/from"; // Absolute path of source folder, All component class files under the "mindustry.entities.comp" package should be stored here.
		String targetDir = "F:/jc/fetch/to"; // Absolute path of target folder
		String fetchPackage = "heavyindustry.fetch"; // Package name for replacement

		try {
			processJavaFiles(sourceDir, targetDir, fetchPackage);
			System.out.println("File processing completed!");
		} catch (IOException e) {
			System.err.println("Error processing file:" + e.getMessage());
			e.printStackTrace();
		}
	}

	public static void processJavaFiles(String sourceDir, String targetDir, String fetchPackage) throws IOException {
		Path sourcePath = Paths.get(sourceDir);
		Path targetPath = Paths.get(targetDir);

		// Ensure that the target directory exists
		Files.createDirectories(targetPath);

		// Recursive traversal of directories using Files.walkFileTree
		Files.walkFileTree(sourcePath, new SimpleFileVisitor<Path>() {
			@Override
			public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
				if (file.toString().endsWith(".java")) {
					processSingleFile(file, sourcePath, targetPath, fetchPackage);
				}
				return FileVisitResult.CONTINUE;
			}

			@Override
			public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
				// Create corresponding subdirectories in the target directory
				Path relativeDir = sourcePath.relativize(dir);
				Path targetSubDir = targetPath.resolve(relativeDir);
				Files.createDirectories(targetSubDir);
				return FileVisitResult.CONTINUE;
			}
		});
	}

	private static void processSingleFile(Path sourceFile, Path sourceRoot, Path targetRoot, String fetchPackage) throws IOException {
		// Read file content
		String content = new String(Files.readAllBytes(sourceFile), "UTF-8");

		// Replace text
		String processedContent = procComp(content, fetchPackage);

		// Calculate the target file path
		Path relativePath = sourceRoot.relativize(sourceFile);
		Path targetFile = targetRoot.resolve(relativePath);

		// Write the processed content to the target file
		Files.write(targetFile, processedContent.getBytes("UTF-8"));

		System.out.println("Processed: " + sourceFile + " -> " + targetFile);
	}

	public static String procComp(String source, String fetchPackage) {
		return source
				.replace("mindustry.entities.comp", fetchPackage)
				.replace("mindustry.annotations.Annotations.*", "heavyindustry.annotations.Annotations.*")
				.replaceAll("@Component\\((base = true|.)+\\)\n*", "@EntityComponent(base = true, vanilla = true)\n")
				.replaceAll("@Component\n*", "@EntityComponent(vanilla = true)\n")
				.replaceAll("@BaseComponent\n*", "@EntityBaseComponent\n")
				.replaceAll("@CallSuper\n*", "")
				.replaceAll("@Final\n*", "")
				.replaceAll("@EntityDef\\(*.*\\)*\n*", "");
	}
}
