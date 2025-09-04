package heavyindustry.desktop;

import arc.util.Log;
import heavyindustry.core.DefaultImpl;
import heavyindustry.func.RunT;
import heavyindustry.util.ReflectImpl;

import java.lang.StackWalker.Option;
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Field;
import java.util.Optional;

import static heavyindustry.util.InteUnsafer.internalUnsafe;
import static heavyindustry.util.Unsafer.unsafe;

public class DesktopImpl implements ReflectImpl {
	static Lookup lookup;

	static StackWalker walker;

	static {
		unsafe = getUnsafe();

		invoke(() -> {
			lookup = (Lookup) unsafe.getObject(Lookup.class, unsafe.staticFieldOffset(Lookup.class.getDeclaredField("IMPL_LOOKUP")));

			Demodulator.makeModuleOpen(Object.class.getModule(), "jdk.internal.misc", DesktopImpl.class.getModule());
			Demodulator.makeModuleOpen(Object.class.getModule(), "jdk.internal.misc", DefaultImpl.class.getModule());

			internalUnsafe = getInternalUnsafe();
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
}
