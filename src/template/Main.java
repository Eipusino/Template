package template;

import java.lang.reflect.Field;
import java.util.Arrays;

public class Main implements Cloneable {
	public static int var0 = 10;
	public static Integer var1 = 10;
	public static Float var2 = 10f;
	public static Object var3 = new Object() {
		public int var00 = 118;
	};

	public static void main(String... arg) {
		System.out.println("oh no");

		loadc();
	}

	public static void loadc() {
		try {
			Thread thread = Thread.currentThread();
			StackTraceElement[] trace = thread.getStackTrace();
			for (StackTraceElement traces : trace) {
				System.out.println(Class.forName(traces.getClassName()));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public Object copy() {
		try {
			return super.clone();
		} catch (CloneNotSupportedException e) {
			throw new RuntimeException(e);
		}
	}
}
