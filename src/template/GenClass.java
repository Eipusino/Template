package template;

import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class GenClass {
	/*public static void generateFieldUtilsClass(String outputPath) throws IOException {
		Files.createDirectories(Paths.get(outputPath).getParent());

		ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES);

		cw.visit(Opcodes.V1_8,
				Opcodes.ACC_PUBLIC + Opcodes.ACC_FINAL,
				"heavyindustry/android/field/FieldUtils",
				null,
				"java/lang/Object",
				null);

		cw.visitSource("FieldUtils.java", null);

		generatePrivateConstructor(cw);
		generateGetFieldOffsetMethod(cw);
		generateGetDeclaredFieldsUncheckedMethod(cw);
		generateGetDeclaredMethodsUncheckedMethod(cw);

		cw.visitEnd();

		// write to file
		byte[] bytecode = cw.toByteArray();
		try (FileOutputStream fos = new FileOutputStream(outputPath)) {
			fos.write(bytecode);
		}

		System.out.println("The FieldUtils class has been generated to: " + outputPath);
	}

	private static void generatePrivateConstructor(ClassWriter cw) {
		MethodVisitor mv = cw.visitMethod(Opcodes.ACC_PRIVATE, "<init>", "()V", null, null);
		mv.visitCode();
		mv.visitVarInsn(Opcodes.ALOAD, 0);
		mv.visitMethodInsn(Opcodes.INVOKESPECIAL, "java/lang/Object", "<init>", "()V", false);
		mv.visitInsn(Opcodes.RETURN);
		mv.visitMaxs(1, 1);
		mv.visitEnd();
	}

	private static void generateGetFieldOffsetMethod(ClassWriter cw) {
		MethodVisitor mv = cw.visitMethod(
				Opcodes.ACC_PUBLIC + Opcodes.ACC_STATIC,
				"getFieldOffset",
				"(Ljava/lang/reflect/Field;)I",
				null,
				null
		);

		mv.visitCode();
		mv.visitVarInsn(Opcodes.ALOAD, 0);
		mv.visitMethodInsn(
				Opcodes.INVOKEVIRTUAL,
				"java/lang/reflect/Field",
				"getOffset",
				"()I",
				false
		);
		mv.visitInsn(Opcodes.IRETURN);
		mv.visitMaxs(1, 1);
		mv.visitEnd();
	}

	private static void generateGetDeclaredFieldsUncheckedMethod(ClassWriter cw) {
		MethodVisitor mv = cw.visitMethod(
				Opcodes.ACC_PUBLIC + Opcodes.ACC_STATIC,
				"getDeclaredFieldsUnchecked",
				"(Ljava/lang/Class;)[Ljava/lang/reflect/Field;",
				null,
				null
		);

		mv.visitCode();
		mv.visitVarInsn(Opcodes.ALOAD, 0);
		mv.visitInsn(Opcodes.ICONST_0);
		mv.visitMethodInsn(
				Opcodes.INVOKEVIRTUAL,
				"java/lang/Class",
				"getDeclaredFieldsUnchecked",
				"(Z)[Ljava/lang/reflect/Field;",
				false
		);
		mv.visitInsn(Opcodes.ARETURN);
		mv.visitMaxs(2, 1);
		mv.visitEnd();
	}

	private static void generateGetDeclaredMethodsUncheckedMethod(ClassWriter cw) {
		MethodVisitor mv = cw.visitMethod(
				Opcodes.ACC_PUBLIC + Opcodes.ACC_STATIC,
				"getDeclaredMethodsUnchecked",
				"(Ljava/lang/Class;)[Ljava/lang/reflect/Method;",
				null,
				null
		);

		mv.visitCode();
		mv.visitVarInsn(Opcodes.ALOAD, 0);
		mv.visitInsn(Opcodes.ICONST_0);
		mv.visitMethodInsn(
				Opcodes.INVOKEVIRTUAL,
				"java/lang/Class",
				"getDeclaredMethodsUnchecked",
				"(Z)[Ljava/lang/reflect/Method;",
				false
		);
		mv.visitInsn(Opcodes.ARETURN);
		mv.visitMaxs(2, 1);
		mv.visitEnd();
	}

	public static void main(String[] args) {
		try {
			String outputPath = "D:/V9_9/FieldUtils.class";

			generateFieldUtilsClass(outputPath);
			System.out.println("Successfully generated!");

			File classFile = new File(outputPath);
			if (classFile.exists()) {
				System.out.println("File size: " + classFile.length() + " byte");
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}*/
}
