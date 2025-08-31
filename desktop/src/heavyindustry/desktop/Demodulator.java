package heavyindustry.desktop;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import static heavyindustry.util.Unsafer.unsafe;

/**
 * The anti modularity tool only provides one main method {@link Demodulator#makeModuleOpen(Module, Package, Module)}
 * to force software packages that open modules to the required modules.
 * <p>This class behavior may completely break the modular access protection and is inherently insecure. If it is
 * not necessary, please try to avoid using this class.
 * <p><strong>This class is only available after Java 9 to avoid referencing methods of this class in earlier versions,
 * and it is only available on the desktop platform. Any behavior of this class is not allowed on the
 * Android platform.</strong>
 *
 * @author EBwilson
 */
public final class Demodulator {
	private static final long fieldFilterOffset = 112l;

	private static final Field opensField;
	private static final Field exportField;

	private static final Method exportNative;

	static {
		try {
			ensureFieldOpen();

			opensField = Module.class.getDeclaredField("openPackages");
			exportField = Module.class.getDeclaredField("exportedPackages");

			makeModuleOpen(Module.class.getModule(), "java.lang", Demodulator.class.getModule());

			exportNative = Module.class.getDeclaredMethod("addExports0", Module.class, String.class, Module.class);
			exportNative.setAccessible(true);
			exportNative.invoke(null, Module.class.getModule(), "java.lang", Demodulator.class.getModule());
		} catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException |
		         NoSuchFieldException e) {
			throw new RuntimeException(e);
		}
	}

	private Demodulator() {}

	public static void makeModuleOpen(Module from, Class<?> clazz, Module to) {
		if (clazz.isArray()) {
			makeModuleOpen(from, clazz.getComponentType(), to);
		} else {
			makeModuleOpen(from, clazz.getPackage(), to);
		}
	}

	public static void makeModuleOpen(Module from, Package pac, Module to) {
		if (checkModuleOpen(from, pac, to)) return;

		makeModuleOpen(from, pac.getName(), to);
	}

	@SuppressWarnings("unchecked")
	public static void makeModuleOpen(Module from, String pac, Module to) {
		try {
			if (exportNative != null) exportNative.invoke(null, from, pac, to);
		} catch (IllegalAccessException | InvocationTargetException e) {
			throw new RuntimeException(e);
		}

		Map<String, Set<Module>> opensMap = (Map<String, Set<Module>>) unsafe.getObjectVolatile(from, unsafe.objectFieldOffset(opensField));
		if (opensMap == null) {
			opensMap = new HashMap<>();
			unsafe.putObjectVolatile(from, unsafe.objectFieldOffset(opensField), opensMap);
		}

		Map<String, Set<Module>> exportsMap = (Map<String, Set<Module>>) unsafe.getObjectVolatile(from, unsafe.objectFieldOffset(exportField));
		if (exportsMap == null) {
			exportsMap = new HashMap<>();
			unsafe.putObjectVolatile(from, unsafe.objectFieldOffset(exportField), exportsMap);
		}

		Set<Module> opens = opensMap.computeIfAbsent(pac, e -> new HashSet<>());
		Set<Module> exports = exportsMap.computeIfAbsent(pac, e -> new HashSet<>());

		try {
			opens.add(to);
		} catch (UnsupportedOperationException e) {
			ArrayList<Module> lis = new ArrayList<>(opens);
			lis.add(to);
			opensMap.put(pac, new HashSet<>(lis));
		}

		try {
			exports.add(to);
		} catch (UnsupportedOperationException e) {
			ArrayList<Module> lis = new ArrayList<>(exports);
			lis.add(to);
			exportsMap.put(pac, new HashSet<>(lis));
		}
	}

	public static boolean checkModuleOpen(Module from, Package pac, Module to) {
		Objects.requireNonNull(from);
		Objects.requireNonNull(to);

		if (pac == null) return true;

		return from.isOpen(pac.getName(), to);
	}

	@SuppressWarnings("unchecked")
	public static void ensureFieldOpen() {
		try {
			Class<?> clazz = Class.forName("jdk.internal.reflect.Reflection");
			Map<Class<?>, Set<String>> map = (Map<Class<?>, Set<String>>) unsafe.getObject(clazz, fieldFilterOffset);
			map.clear();
		} catch (ClassNotFoundException e) {
			throw new RuntimeException(e);
		}
	}
}
