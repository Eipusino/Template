package heavyindustry.android;

import arc.util.Log;
import dalvik.system.VMStack;
import heavyindustry.util.ReflectImpl;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Field;

public class AndroidImpl implements ReflectImpl {
	public static final Lookup lookup;

	static Field accessFlags;

	static {
		try {
			HiddenApi.setHiddenApiExemptions();
		} catch (Throwable e) {
			Log.err(e);
		}

		try {
			accessFlags = Class.class.getDeclaredField("accessFlags");
			accessFlags.setAccessible(true);
		} catch (NoSuchFieldException e) {
			Log.err(e);
		}

		lookup = getLookup();
	}

	static Lookup getLookup() {
		try {
			Field[] fields = Lookup.class.getDeclaredFields();
			for (Field field : fields) {
				if ("IMPL_LOOKUP".equals(field.getName())) {
					field.setAccessible(true);
					return (Lookup) field.get(null);
				}
			}
		} catch (Throwable e) {
			Log.err("Reflection acquisition of 'IMPL_LOOKUP' encountered an exception. " + e.getClass().getName() + ": " + e.getMessage());

			return MethodHandles.lookup();
		}

		Log.err("'IMPL_LOOKUP' field not found");

		return MethodHandles.lookup();
	}

	@Override
	public void setOverride(AccessibleObject override) {
		override.setAccessible(true);
	}

	@Override
	public void setPublic(Class<?> obj) {
		try {
			if (accessFlags != null) {
				int flags = accessFlags.getInt(obj);
				accessFlags.setInt(obj, 65535 & ((flags & 65535 & (-17) & (-3)) | 1));
			}
		} catch (IllegalAccessException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public Class<?> callerClass() {
		return VMStack.getStackClass2();
	}

	@Override
	public Lookup lookup() {
		return lookup;
	}
}
