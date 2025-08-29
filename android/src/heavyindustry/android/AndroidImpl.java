package heavyindustry.android;

import arc.util.Log;
import dalvik.system.VMStack;
import dynamilize.IllegalHandleException;
import heavyindustry.HVars;
import heavyindustry.android.util.handler.AndroidClassHandler;
import heavyindustry.mod.ModGetter;
import heavyindustry.mod.ModInfo;
import heavyindustry.util.AccessibleHelper;
import heavyindustry.util.ReflectImpl;
import mindustry.mod.Mod;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class AndroidImpl implements ReflectImpl {
	static Lookup lookup;

	static Field accessFlags;

	static Method fieldsMethod;
	static Method methodsMethod;

	static {
		try {
			fieldsMethod = Class.class.getDeclaredMethod("getDeclaredFields");
			fieldsMethod.setAccessible(true);
			methodsMethod = Class.class.getDeclaredMethod("getDeclaredMethods");
			methodsMethod.setAccessible(true);
			accessFlags = Class.class.getDeclaredField("accessFlags");
			accessFlags.setAccessible(true);
		} catch (NoSuchMethodException | NoSuchFieldException e) {
			Log.err(e);
		}

		lookup = getLookup();
	}

	@SuppressWarnings("unchecked")
	public AndroidImpl() {
		HVars.accessibleHelper = new AccessibleHelper() {
			@Override
			public void makeAccessible(AccessibleObject object) {
				object.setAccessible(true);
			}

			@Override
			public void makeClassAccessible(Class<?> clazz) {
				//no action
			}
		};
		HVars.fieldAccessHelper = new AndroidFieldAccessHelper();
		HVars.methodInvokeHelper = new AndroidMethodInvokeHelper();

		HVars.classesFactory = main -> {
			try {
				if (!Mod.class.isAssignableFrom(main))
					throw new IllegalHandleException("class was not a mod main class");

				ModInfo mod = ModGetter.getModWithClass((Class<? extends Mod>) main);
				if (mod == null)
					throw new IllegalHandleException("mod with main class " + main + " was not found");

				ModGetter.checkModFormat(mod.file);
				return new AndroidClassHandler(mod);
			} catch (IllegalHandleException e) {
				throw new RuntimeException(e);
			}
		};
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
			Log.err(e);
		}

		return MethodHandles.lookup();
	}

	@Override
	public void setOverride(AccessibleObject override) {
		override.setAccessible(true);
	}

	@Override
	public void setPublic(Class<?> obj) throws Exception {
		if (accessFlags != null) {
			int flags = accessFlags.getInt(obj);
			accessFlags.setInt(obj, 65535 & ((flags & 65535 & (-17) & (-3)) | 1));
		}
	}

	@Override
	public Class<?> getCallerClass() {
		return VMStack.getStackClass2();
	}
}
