package heavyindustry.desktop;

import heavyindustry.util.ReflectImpl;

import java.lang.StackWalker.Option;
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.reflect.AccessibleObject;
import java.util.Optional;

import static heavyindustry.util.Unsafer.unsafe;

public class DesktopImpl implements ReflectImpl {
	public static final Lookup lookup;

	static {
		try {
			lookup = (Lookup) unsafe.getObject(Lookup.class, unsafe.staticFieldOffset(Lookup.class.getDeclaredField("IMPL_LOOKUP")));

			setModule();
			Desktop.init();
		} catch (Throwable e) {
			throw new RuntimeException(e);
		}
	}

	StackWalker walker = StackWalker.getInstance(Option.RETAIN_CLASS_REFERENCE);

	static void setModule() {
		Demodulator.makeModuleOpen(Object.class.getModule(), "jdk.internal.misc", DesktopImpl.class.getModule());
	}

	public void setPublic(Class<?> cls) {}

	public void setOverride(AccessibleObject override) {
		override.setAccessible(true);
	}

	@Override
	public Class<?> getCallerClass() {
		try {
			Optional<String> callerClassName = walker.walk(frames -> frames
					.skip(1) // 跳过当前方法
					.findFirst()
					.map(StackWalker.StackFrame::getClassName));
			return Class.forName(callerClassName.orElse(null));
		} catch (ClassNotFoundException e) {
			return null;
		}
	}
}
