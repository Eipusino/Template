package heavyindustry.android;

import arc.util.Log;
import dalvik.system.VMStack;
import heavyindustry.HVars;
import heavyindustry.util.PlatformImpl;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Field;

import static heavyindustry.util.InternalUtils.internalUnsafe;
import static heavyindustry.util.InvokeUtils.lookup;
import static heavyindustry.util.ObjectUtils.run;
import static heavyindustry.util.UnsafeUtils.unsafe;

public class AndroidImpl implements PlatformImpl {
	static Field accessFlags;

	static {
		init();
	}

	static void init() {
		try {
			Log.info("Use @", Class.forName("sun.misc.Unsafe"));

			Field field = sun.misc.Unsafe.class.getDeclaredField("theUnsafe");
			field.setAccessible(true);
			unsafe = (sun.misc.Unsafe) field.get(null);

			HVars.hasUnsafe = true;
		} catch (Throwable e) {
			Log.err(e);

			return;
		}

		run(HiddenApi::setHiddenApiExemptions);
		run(() -> {
			Log.info("Use @", Class.forName("jdk.internal.misc.Unsafe"));

			Field field = jdk.internal.misc.Unsafe.class.getDeclaredField("theUnsafe");
			field.setAccessible(true);
			internalUnsafe = (jdk.internal.misc.Unsafe) field.get(null);

			HVars.hasJDKUnsafe = true;
		});
		run(() -> {
			try {
				accessFlags = Class.class.getDeclaredField("accessFlags");
				accessFlags.setAccessible(true);
			} catch (NoSuchFieldException e) {
				Log.err(e);
			}
		});

		try {
			Field field = Lookup.class.getDeclaredField("IMPL_LOOKUP");
			field.setAccessible(true);
			lookup = (Lookup) field.get(null);

			HVars.hasImplLookup = true;
		} catch (Throwable e) {
			Log.err("Reflection acquisition of 'IMPL_LOOKUP' encountered an exception", e);
		}
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
}
