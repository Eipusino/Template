package heavyindustry.android;

import dalvik.system.VMStack;
import heavyindustry.util.ReflectImpl;

import java.lang.reflect.AccessibleObject;

public class AndroidImpl implements ReflectImpl {
	public AndroidImpl() {
		HiddenApi.load();
		Android.load();
	}

	public void setOverride(AccessibleObject override) throws Throwable {
		override.setAccessible(true);
	}

	public void setPublic(Class<?> obj) throws Exception {
		int flags = Android.accessFlags.getInt(obj);
		Android.accessFlags.setInt(obj, 65535 & ((flags & 65535 & (-17) & (-3)) | 1));
	}

	@Override
	public Class<?> getCallerClass() {
		return VMStack.getStackClass2();
	}
}
