package template;

import arc.util.Log;
import arc.util.Time;
import sun.misc.Unsafe;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.invoke.MethodType;
import java.lang.invoke.VarHandle;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

public class Main {
	static Unsafe unsafe;
	static Lookup lookup;

	static int test;

	public static void main(String... arg) {
		try {
			Set<Float> treeSet = new TreeSet<>();
			treeSet.add(Float.NaN);
		} catch (Throwable e) {
			e.printStackTrace();
		}
	}

	public static void test() {
		test++;
	}

	public static void ensureFieldOpen() throws Throwable {
		Class<?> c = Class.forName("jdk.internal.reflect.Reflection");

		Map<?, ?> fieldFilterMap = (Map<?, ?>) lookup.findStaticGetter(c, "fieldFilterMap", Map.class).invokeExact();
		if (fieldFilterMap != null) {
			fieldFilterMap.clear();
		}

		Map<?, ?> methodFilterMap = (Map<?, ?>) lookup.findStaticGetter(c, "methodFilterMap", Map.class).invokeExact();
		if (methodFilterMap != null) {
			methodFilterMap.clear();
		}
	}

	public static Unsafe getUnsafe() {
		try {
			Field field = Unsafe.class.getDeclaredField("theUnsafe");
			field.setAccessible(true);
			return (Unsafe) field.get(null);
		} catch (NoSuchFieldException | IllegalAccessException e) {
			throw new RuntimeException(e);
		}
	}

	public static Lookup getLookup() {
		try {
			return (Lookup) unsafe.getObject(Lookup.class, unsafe.staticFieldOffset(Lookup.class.getDeclaredField("IMPL_LOOKUP")));
		} catch (NoSuchFieldException e) {
			throw new RuntimeException(e);
		}
	}
}
