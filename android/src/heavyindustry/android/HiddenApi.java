package heavyindustry.android;

import android.os.Build.VERSION;
import arc.util.Log;
import arc.util.OS;
import dalvik.system.VMRuntime;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import static heavyindustry.util.Unsafer.unsafe;

/** Only For Android */
public class HiddenApi {
	public static final VMRuntime runtime = VMRuntime.getRuntime();

	public static final long intBytes = Integer.BYTES;
	/**
	 * <a href="https://cs.android.com/android/platform/superproject/main/+/main:art/runtime/mirror/executable.h;bpv=1;bpt=1;l=73?q=executable&ss=android&gsn=art_method_&gs=KYTHE%3A%2F%2Fkythe%3A%2F%2Fandroid.googlesource.com%2Fplatform%2Fsuperproject%2Fmain%2F%2Fmain%3Flang%3Dc%252B%252B%3Fpath%3Dart%2Fruntime%2Fmirror%2Fexecutable.h%23GLbGh3aGsjxEudfgKrvQvNcLL3KUjmUaJTc4nCOKuVY">
	 * uint64_t Executable::art_method_</a>
	 */
	public static final int artMethodOffset = 24;

	public static void setHiddenApiExemptions() {
		if (VERSION.SDK_INT < 28 && trySetHiddenApiExemptions()) return;
		// In higher versions, the setHiddenApiExertions method cannot be directly reflected to obtain it, so the artMethod needs to be modified
		// Sdk_version>28 (exact number unknown)
		Method setHiddenApiExemptions = findMethod();

		try {
			if (setHiddenApiExemptions == null) {
				throw new InternalError("setHiddenApiExemptions not found.");
			}

			invoke(setHiddenApiExemptions);
		} catch (Exception e) {
			Log.err(e);
		}
	}

	/** @return true if successful. */
	private static boolean trySetHiddenApiExemptions() {
		try {
			// MAYBE: sdk_version < 28
			runtime.setHiddenApiExemptions(new String[]{"L"});

			return true;
		} catch (Throwable e) {
			Log.err(e);
		}

		try {
			// Obtaining method through reflection
			Method method = Class.class.getDeclaredMethod("getDeclaredMethod", String.class, Class[].class);
			method.setAccessible(true);
			Method setHiddenApiExemptions = (Method) method.invoke(VMRuntime.class, "setHiddenApiExemptions", new Class[]{String[].class});
			invoke(setHiddenApiExemptions);

			return true;
		} catch (Throwable e) {
			Log.err(e);
		}

		return false;
	}

	private static void invoke(Method method) throws IllegalAccessException, InvocationTargetException {
		method.setAccessible(true);
		method.invoke(runtime, (Object) new String[]{"L"});
	}

	private static Method findMethod() {
		Method[] methods = VMRuntime.class.getDeclaredMethods();
		if (methods[0].getName().equals("setHiddenApiExemptions")) {
			return methods[0];
		}
		int length = methods.length;
		Method[] array = (Method[]) runtime.newNonMovableArray(Method.class, length);
		System.arraycopy(methods, 0, array, 0, length);

		long address = addressOf(array);
		long min = Long.MAX_VALUE, minSecond = Long.MAX_VALUE, max = Long.MIN_VALUE;
		/* Find artMethod  */
		for (int k = 0; k < length; ++k) {
			final long addressKBs = address + k * intBytes;
			final long addressMethod = unsafe.getInt(addressKBs);
			final long addressArtMethod = unsafe.getLong(addressMethod + artMethodOffset);
			if (min >= addressArtMethod) {
				min = addressArtMethod;
			} else if (minSecond >= addressArtMethod) {
				minSecond = addressArtMethod;
			}
			if (max <= addressArtMethod) {
				max = addressArtMethod;
			}
		}

		// The difference between two artMethods (due to continuity)
		final long sizeArtMethod = minSecond - min;

		Log.debug("sizeArtMethod: " + sizeArtMethod);

		if (sizeArtMethod > 0 && sizeArtMethod < 100) {
			for (long artMethod = minSecond; artMethod < max; artMethod += sizeArtMethod) {
				// This obtains the * Method of array [0], with a size of 32 bits
				final long addressMethod = unsafe.getInt(address);
				// Modify the artMethod of the first method
				unsafe.putLong(addressMethod + artMethodOffset, artMethod);
				// Android's getName is a native implementation, and by modifying artMethod, the name will naturally change
				if ("setHiddenApiExemptions".equals(array[0].getName())) {
					Log.debug("Got: " + array[0]);

					return array[0];
				}
			}
		}
		return null;
	}

	public static long addressOf(Object[] array) {
		try {
			if (VERSION.SDK_INT < 21) return runtime.addressOf(array);
		} catch (Throwable e) {
			Log.err(e);
		}
		return uaddressOf(array);
	}

	public static long uaddressOf(Object obj) {
		return vaddressOf(obj) + offset;
	}

	// ---------Address/Memory Operation---------
	static long vaddressOf(Object o) {
		if (o == null) throw new IllegalArgumentException("o is null.");
		oneArray[0] = o;
		long baseOffset = unsafe.arrayBaseOffset(Object[].class);
		return switch (unsafe.arrayIndexScale(Object[].class)) {
			case 4 -> (unsafe.getInt(oneArray, baseOffset) & 0xFFFFFFFFL) * (OS.is64Bit ? 8 : 1);
			case 8 -> unsafe.getLong(oneArray, baseOffset);
			default -> throw new UnsupportedOperationException("Unsupported address size: " + unsafe.arrayIndexScale(Object[].class));
		};
	}

	static Object[] oneArray;
	static int[] intArray;

	static long offset;

	static {
		oneArray = (Object[]) runtime.newNonMovableArray(Object.class, 1);
		intArray = (int[]) runtime.newNonMovableArray(int.class, 0);
		offset = runtime.addressOf(intArray) - vaddressOf(intArray);
	}

	private static void replaceMethod(Method dest, Method src) {
		long addressDest = unsafe.getLong(dest, artMethodOffset);
		long addressSrc = unsafe.getLong(src, artMethodOffset);

		unsafe.copyMemory(addressSrc + 4, addressDest + 4, 24);
	}
}
