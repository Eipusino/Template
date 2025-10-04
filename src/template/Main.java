package template;

import arc.util.Strings;
import arc.util.Time;
import sun.misc.Unsafe;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.invoke.MethodType;
import java.lang.invoke.VarHandle;
import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Map;
import java.util.Objects;

public class Main {
	public static Unsafe unsafe;
	public static Lookup lookup;

	public static void main(String[] args) {
		try {
			/*unsafe = getUnsafe();
			lookup = getLookup();

			MethodHandle getConstructorsHandle = lookup.findVirtual(Class.class, "getDeclaredConstructors0", MethodType.methodType(Constructor[].class, boolean.class));
			System.out.println(Arrays.toString((Constructor<?>[]) getConstructorsHandle.invokeExact(Main.class, false)));*/
		} catch (Throwable e) {
			e.printStackTrace();
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
			assert unsafe != null;

			return (Lookup) unsafe.getObject(Lookup.class, unsafe.staticFieldOffset(Lookup.class.getDeclaredField("IMPL_LOOKUP")));
		} catch (NoSuchFieldException e) {
			throw new RuntimeException(e);
		}
	}
}
