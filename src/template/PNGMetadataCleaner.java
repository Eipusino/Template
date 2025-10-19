package template;

import javax.imageio.ImageIO;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;

public class PNGMetadataCleaner {
	public static void start() {
		String sourceDir = "F:/jc/pngs/from";
		String targetDir = "F:/jc/pngs/to";

		try {
			cleanPNGFiles(sourceDir, targetDir);
			System.out.println("PNG文件清理完成！");
		} catch (IOException e) {
			System.err.println("处理文件时出错: " + e.getMessage());
			e.printStackTrace();
		}
	}

	public static void cleanPNGFiles(String sourceDir, String targetDir) throws IOException {
		Path sourcePath = Paths.get(sourceDir);
		Path targetPath = Paths.get(targetDir);

		// 确保目标目录存在
		Files.createDirectories(targetPath);

		Files.walkFileTree(sourcePath, new SimpleFileVisitor<Path>() {
			@Override
			public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
				if (file.toString().toLowerCase().endsWith(".png")) {
					cleanSinglePNG(file, sourcePath, targetPath);
				}
				return FileVisitResult.CONTINUE;
			}

			@Override
			public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
				Path relativeDir = sourcePath.relativize(dir);
				Path targetSubDir = targetPath.resolve(relativeDir);
				Files.createDirectories(targetSubDir);
				return FileVisitResult.CONTINUE;
			}
		});
	}

	private static void cleanSinglePNG(Path sourceFile, Path sourceRoot, Path targetRoot) throws IOException {
		// 计算目标文件路径
		Path relativePath = sourceRoot.relativize(sourceFile);
		Path targetFile = targetRoot.resolve(relativePath);

		try {
			// 方法1: 重新编码图像（最有效清除元数据）
			BufferedImage image = ImageIO.read(sourceFile.toFile());
			if (image == null) {
				System.err.println("无法读取图像: " + sourceFile);
				return;
			}

			// 创建不包含元数据的PNG
			ImageWriter writer = ImageIO.getImageWritersByFormatName("png").next();

			try (ImageOutputStream output = ImageIO.createImageOutputStream(targetFile.toFile())) {
				writer.setOutput(output);

				// 创建空的ImageWriteParam来避免写入元数据
				javax.imageio.ImageWriteParam param = writer.getDefaultWriteParam();
				writer.write(null, new javax.imageio.IIOImage(image, null, null), param);
			}
			writer.dispose();

			long originalSize = Files.size(sourceFile);
			long newSize = Files.size(targetFile);
			System.out.printf("已处理: %s | 原大小: %d KB -> 新大小: %d KB (节省: %.1f%%)%n",
					sourceFile.getFileName(),
					originalSize / 1024,
					newSize / 1024,
					(1 - (double) newSize / originalSize) * 100);

		} catch (Exception e) {
			System.err.println("处理文件失败: " + sourceFile + " - " + e.getMessage());
			// 如果处理失败，直接复制原文件
			Files.copy(sourceFile, targetFile, StandardCopyOption.REPLACE_EXISTING);
		}
	}
}
