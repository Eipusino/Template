package heavyindustry.desktop;

import heavyindustry.util.ReflectImpl;

import java.lang.StackWalker.Option;
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.reflect.AccessibleObject;
import java.util.Optional;

import static heavyindustry.util.Unsafer.unsafe;

@SuppressWarnings("unused")
public class DesktopImpl implements ReflectImpl {
	public static final Lookup lookup;

	public static final StackWalker walker;

	static {
		try {
			lookup = (Lookup) unsafe.getObject(Lookup.class, unsafe.staticFieldOffset(Lookup.class.getDeclaredField("IMPL_LOOKUP")));

			Demodulator.makeModuleOpen(Object.class.getModule(), "jdk.internal.misc", DesktopImpl.class.getModule());
		} catch (Throwable e) {
			throw new RuntimeException(e);
		}

		walker = StackWalker.getInstance(Option.RETAIN_CLASS_REFERENCE);
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
