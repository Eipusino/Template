package template;

import jdk.internal.misc.Unsafe;

public class Util {
	public static Unsafe unsafe;

	static String num = "0";

	@SuppressWarnings("unchecked")
	public static <T, E extends Throwable> T thrower(Throwable err) throws E {
		throw (E) err;
	}
}
