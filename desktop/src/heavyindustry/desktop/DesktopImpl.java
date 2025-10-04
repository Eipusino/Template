package heavyindustry.desktop;

import arc.util.Log;
import heavyindustry.core.DefaultImpl;
import heavyindustry.func.RunT;
import heavyindustry.util.PlatformImpl;

import java.lang.StackWalker.Option;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.invoke.MethodType;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Optional;

import static heavyindustry.util.Unsafer.unsafe;
import static heavyindustry.util.Unsaferf.internalUnsafe;

public class DesktopImpl implements PlatformImpl {
	static Lookup lookup;

	static MethodHandle getFieldsHandle;
	static MethodHandle getMethodsHandle;
	static MethodHandle getConstructorsHandle;

	static StackWalker walker;

	static {
		unsafe = getUnsafe();

		invoke(() -> {
			lookup = (Lookup) unsafe.getObject(Lookup.class, unsafe.staticFieldOffset(Lookup.class.getDeclaredField("IMPL_LOOKUP")));

			Demodulator.makeModuleOpen(Object.class.getModule(), "jdk.internal.misc", DesktopImpl.class.getModule());
			Demodulator.makeModuleOpen(Object.class.getModule(), "jdk.internal.misc", DefaultImpl.class.getModule());

			internalUnsafe = getInternalUnsafe();

			invoke(() -> {
				getFieldsHandle = lookup.findVirtual(Class.class, "getDeclaredFields0", MethodType.methodType(Field[].class, boolean.class));
				getMethodsHandle = lookup.findVirtual(Class.class, "getDeclaredMethods0", MethodType.methodType(Method[].class, boolean.class));
				getConstructorsHandle = lookup.findVirtual(Class.class, "getDeclaredConstructors0", MethodType.methodType(Constructor[].class, boolean.class));
			});
		});

		walker = StackWalker.getInstance(Option.RETAIN_CLASS_REFERENCE);
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
		return jdk.internal.misc.Unsafe.getUnsafe();
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
	public void setPublic(Class<?> cls) {
		// There's no need to do this
	}

	@Override
	public void setOverride(AccessibleObject override) {
		override.setAccessible(true);
	}

	@Override
	public Class<?> callerClass() {
		try {
			Optional<String> callerClassName = walker.walk(frames -> frames
					.skip(1)
					.findFirst()
					.map(StackWalker.StackFrame::getClassName));
			return callerClassName.isPresent() ? Class.forName(callerClassName.get()) : null;
		} catch (ClassNotFoundException e) {
			return null;
		}
	}

	@Override
	public Lookup lookup() {
		return lookup;
	}

	@Override
	public Field[] getFields(Class<?> cls) {
		try {
			return (Field[]) getFieldsHandle.invokeExact(cls, false);
		} catch (Throwable e) {
			return cls.getDeclaredFields();
		}
	}

	@Override
	public Method[] getMethods(Class<?> cls) {
		try {
			return (Method[]) getMethodsHandle.invokeExact(cls, false);
		} catch (Throwable e) {
			return cls.getDeclaredMethods();
		}
	}

	@Override
	public Constructor<?>[] getConstructors(Class<?> cls) {
		try {
			return (Constructor<?>[]) getConstructorsHandle.invokeExact(cls, false);
		} catch (Throwable e) {
			return cls.getDeclaredConstructors();
		}
	}
}
