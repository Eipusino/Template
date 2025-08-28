package heavyindustry.desktop;

import jdk.internal.misc.Unsafe;

public class Desktop {
	public static Unsafe unsafe;

	public static void init() {
		unsafe = Unsafe.getUnsafe();
	}
}
