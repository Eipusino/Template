package template;

import arc.util.Log;
import sun.misc.Unsafe;

import java.lang.reflect.Field;

public class Main {
	public static Unsafe unsafe;
	public static Object obb = new Object() {
		public final int ain = 10;
	};

	public static void main(String... arg) {
		try {
			unsafe = getUnsafe();

			System.out.println(obb.getClass().getDeclaredField("ain").getDeclaringClass() == obb.getClass());
		} catch (Throwable e) {
			Log.err(e);
		}
	}

	static Unsafe getUnsafe() {
		try {
			Field field = Unsafe.class.getDeclaredField("theUnsafe");
			field.setAccessible(true);
			return (Unsafe) field.get(null);
		} catch (NoSuchFieldException | IllegalAccessException e) {
			throw new RuntimeException(e);
		}
	}
}
