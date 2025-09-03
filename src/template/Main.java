package template;

import java.util.Arrays;

public class Main {
	public static String gz = "al";

	public static void main(String... arg) {
		try {
			System.out.println(Main.class.getDeclaredMethod("main", String[].class));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
