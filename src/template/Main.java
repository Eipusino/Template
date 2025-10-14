package template;

import sun.misc.Unsafe;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.invoke.MethodType;

public class Main {
	public static void main(String... arg) {
		try {
			FetchProcessor.start();
		} catch (Throwable e) {
			e.printStackTrace();
		}
	}

	/*public static Unsafe getUnsafe() {
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
	}*/
}
