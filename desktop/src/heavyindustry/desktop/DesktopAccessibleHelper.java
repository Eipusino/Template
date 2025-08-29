package heavyindustry.desktop;

import dynamilize.DynamicMaker;
import heavyindustry.util.AccessibleHelper;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class DesktopAccessibleHelper implements AccessibleHelper {
	@Override
	public void makeAccessible(AccessibleObject object) {
		if (object instanceof Method method) {
			Demodulator.makeModuleOpen(method.getReturnType().getModule(), method.getReturnType(), DynamicMaker.class.getModule());

			for (Class<?> type : method.getParameterTypes()) {
				Demodulator.makeModuleOpen(type.getModule(), type, DynamicMaker.class.getModule());
			}

			method.setAccessible(true);
		} else if (object instanceof Field field) {
			Demodulator.makeModuleOpen(field.getType().getModule(), field.getType(), DynamicMaker.class.getModule());

			field.setAccessible(true);
		}
	}

	@Override
	public void makeClassAccessible(Class<?> clazz) {
		//no action
	}
}
