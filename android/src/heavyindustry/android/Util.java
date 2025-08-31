package heavyindustry.android;

import arc.util.OS;
import dalvik.system.VMRuntime;

import static heavyindustry.util.Unsafer.unsafe;

public class Util {
	static long getLong(Object obj, long offset) {
		return unsafe.getLong(obj, offset);
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

	static Object[] oneArray = (Object[]) VMRuntime.getRuntime().newNonMovableArray(Object.class, 1);

	static void putLong(long address, long x) {
		unsafe.putLong(address, x);
	}

	static int getInt(long address) {
		return unsafe.getInt(address);
	}

	static long getLong(long address) {
		return unsafe.getLong(address);
	}

	static void copyMemory(long src, long dest, int bytes) {
		unsafe.copyMemory(src, dest, bytes);
	}
}
