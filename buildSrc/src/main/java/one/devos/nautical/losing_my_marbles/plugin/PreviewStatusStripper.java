package one.devos.nautical.losing_my_marbles.plugin;

import org.glavo.classfile.ClassBuilder;
import org.glavo.classfile.ClassElement;
import org.glavo.classfile.ClassFile;
import org.glavo.classfile.ClassFileVersion;
import org.glavo.classfile.ClassModel;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;

public final class PreviewStatusStripper {
	public static void run(File dir) throws IOException {
		Files.walkFileTree(dir.toPath(), new SimpleFileVisitor<>() {
			@NotNull
			@Override
			public FileVisitResult visitFile(@NotNull Path file, @NotNull BasicFileAttributes attrs) throws IOException {
				if (!file.getFileName().toString().endsWith(".class"))
					return FileVisitResult.CONTINUE;

				ClassFile cf = ClassFile.of();
				ClassModel model = cf.parse(file);
				byte[] transformed = cf.transform(model, PreviewStatusStripper::transform);
				Files.write(file, transformed);
				return FileVisitResult.CONTINUE;
			}
		});
	}

	private static void transform(ClassBuilder builder, ClassElement element) {
		if (element instanceof ClassFileVersion version && version.minorVersion() != 0) {
			builder.with(ClassFileVersion.of(version.majorVersion(), 0));
			return;
		}

		builder.with(element);
	}
}
