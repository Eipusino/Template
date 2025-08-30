package heavyindustry.desktop;

import dynamilize.IllegalHandleException;
import heavyindustry.HVars;
import heavyindustry.desktop.util.handler.DesktopClassHandler;
import heavyindustry.mod.ModGetter;
import heavyindustry.mod.ModInfo;
import heavyindustry.util.ReflectImpl;
import mindustry.mod.Mod;

import java.lang.StackWalker.Option;
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.InvocationTargetException;
import java.util.Optional;

import static heavyindustry.util.Unsafer.unsafe;

public class DesktopImpl implements ReflectImpl {
	public static final Lookup lookup;

	static final StackWalker walker;

	static {
		try {
			lookup = (Lookup) unsafe.getObject(Lookup.class, unsafe.staticFieldOffset(Lookup.class.getDeclaredField("IMPL_LOOKUP")));

			Demodulator.makeModuleOpen(Object.class.getModule(), "jdk.internal.misc", DesktopImpl.class.getModule());
		} catch (Throwable e) {
			throw new RuntimeException(e);
		}

		walker = StackWalker.getInstance(Option.RETAIN_CLASS_REFERENCE);
	}

	@SuppressWarnings("unchecked")
	public DesktopImpl() {
		HVars.accessibleHelper = new DesktopAccessibleHelper();
		HVars.fieldAccessHelper = new DesktopFieldAccessHelper();
		HVars.methodInvokeHelper = new DesktopMethodInvokeHelper();

		HVars.classesFactory = main -> {
			try {
				if (!Mod.class.isAssignableFrom(main))
					throw new IllegalHandleException("class was not a mod main class");

				ModInfo mod = ModGetter.getModWithClass((Class<? extends Mod>) main);
				if (mod == null)
					throw new IllegalHandleException("mod with main class " + main + " was not found");

				ModGetter.checkModFormat(mod.file);

				return DesktopClassHandler.class.getConstructor(ModInfo.class).newInstance(mod);
			} catch (IllegalHandleException | InvocationTargetException | InstantiationException |
			         IllegalAccessException | NoSuchMethodException e) {
				throw new RuntimeException(e);
			}
		};
	}

	@Override
	public void setPublic(Class<?> cls) {}

	@Override
	public void setOverride(AccessibleObject override) {
		override.setAccessible(true);
	}

	@Override
	public Class<?> getCallerClass() {
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
}
