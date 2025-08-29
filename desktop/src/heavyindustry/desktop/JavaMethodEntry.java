package heavyindustry.desktop;

import dynamilize.Function;
import dynamilize.FunctionType;
import dynamilize.IFunctionEntry;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Describe the behavior of a sub instance's function in the dynamic class of the method description entry
 * in {@code DynamicClass.visitClass(Class, JavaHandleHelper) Behavior Template}.
 * <p>The operation of method entry is actually a reference to the template method, so it is necessary to ensure
 * that the class where the template method is located is always valid. The method entry will generate the
 * entry function of this method and provide it to the dynamic object for use.
 *
 * @author EBwilson
 */
@SuppressWarnings("unchecked")
public class JavaMethodEntry implements IFunctionEntry {
	private final String name;
	private final FunctionType type;
	private final Function<?, ?> defFunc;

	/**
	 * Create a method entry directly through the target method and generate a handle to the method
	 * reference to provide to the anonymous function to describe its behavior
	 *
	 * @param invokeMethod Sample method
	 */
	public JavaMethodEntry(Method invokeMethod) {
		this.name = invokeMethod.getName();
		this.type = FunctionType.inst(invokeMethod);

		defFunc = (self, args) -> {
			try {
				return invokeMethod.invoke(self.objSelf(), args.args());
			} catch (InvocationTargetException | IllegalAccessException e) {
				throw new RuntimeException(e);
			}
		};
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public <S, R> Function<S, R> getFunction() {
		return (Function<S, R>) defFunc;
	}

	@Override
	public FunctionType getType() {
		return type;
	}
}
