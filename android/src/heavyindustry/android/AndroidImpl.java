package heavyindustry.android;

import arc.util.Log;
import dalvik.system.VMStack;
import heavyindustry.func.RunT;
import heavyindustry.util.PlatformImpl;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Field;

import static heavyindustry.util.Unsafer.unsafe;
import static heavyindustry.util.Unsaferf.internalUnsafe;

public class AndroidImpl implements PlatformImpl {
	static Lookup lookup;

	static Field accessFlags;

	static {
		unsafe = getUnsafe();

		invoke(HiddenApi::setHiddenApiExemptions);
		invoke(() -> internalUnsafe = getInternalUnsafe());
		invoke(() -> {
			try {
				accessFlags = Class.class.getDeclaredField("accessFlags");
				accessFlags.setAccessible(true);
			} catch (NoSuchFieldException e) {
				Log.err(e);
			}
		});

		lookup = getLookup();
	}

	static sun.misc.Unsafe getUnsafe() {
		try {
			Field field = sun.misc.Unsafe.class.getDeclaredField("theUnsafe");
			field.setAccessible(true);
			return (sun.misc.Unsafe) field.get(null);
		} catch (NoSuchFieldException | IllegalAccessException e) {
			throw new RuntimeException(e);
		}
	}

	static jdk.internal.misc.Unsafe getInternalUnsafe() {
		try {
			Field field = jdk.internal.misc.Unsafe.class.getDeclaredField("theUnsafe");
			field.setAccessible(true);
			return (jdk.internal.misc.Unsafe) field.get(null);
		} catch (NoSuchFieldException | IllegalAccessException e) {
			throw new RuntimeException(e);
		}
	}

	static Lookup getLookup() {
		try {
			Field field = Lookup.class.getDeclaredField("IMPL_LOOKUP");
			field.setAccessible(true);
			return (Lookup) field.get(null);
		} catch (Throwable e) {
			Log.err("Reflection acquisition of 'IMPL_LOOKUP' encountered an exception", e);

			return MethodHandles.lookup();
		}
	}

	// It may make the code look more aesthetically pleasing, but I don't like a series of try-catch blocks.
	static void invoke(RunT<Throwable> runt) {
		try {
			runt.run();
		} catch (Throwable e) {
			Log.err(e);
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

	@Override
	public Lookup lookup() {
		return lookup;
	}
}
