package heavyindustry.android;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class Android {
	public static final Lookup lookup;

	public static Field accessFlags;

	static Method fieldsMethod;
	static Method methodsMethod;

	static {
		try {
			Method declaredMethod = Class.class.getDeclaredMethod("getDeclaredFields");
			fieldsMethod = declaredMethod;
			declaredMethod.setAccessible(true);
			Method declaredMethod2 = Class.class.getDeclaredMethod("getDeclaredMethods");
			methodsMethod = declaredMethod2;
			declaredMethod2.setAccessible(true);
		} catch (Throwable ignored) {}

		lookup = getLookup();

		try {
			Field declaredField = Class.class.getDeclaredField("accessFlags");
			declaredField.setAccessible(true);
			accessFlags = declaredField;
		} catch (NoSuchFieldException e) {
			throw new RuntimeException(e);
		}
	}

	static Lookup getLookup() {
		try {
			Field[] fields = (Field[]) fieldsMethod.invoke(Lookup.class);
			for (Field field : fields) {
				if ("IMPL_LOOKUP".equals(field.getName())) {
					field.setAccessible(true);
					return (Lookup) field.get(null);
				}
			}
		} catch (Throwable e) {
			return MethodHandles.lookup();
		}

		return MethodHandles.lookup();
	}

	static void load() {}
}
