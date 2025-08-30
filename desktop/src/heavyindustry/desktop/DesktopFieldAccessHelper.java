package heavyindustry.desktop;

import arc.struct.ObjectMap;
import arc.struct.ObjectSet;
import arc.util.Log;
import heavyindustry.util.FieldAccessHelper;
import heavyindustry.util.Unsafer;

import java.lang.invoke.MethodHandle;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

import static heavyindustry.desktop.DesktopImpl.lookup;

public class DesktopFieldAccessHelper implements FieldAccessHelper {
	private static final ObjectMap<Class<?>, ObjectMap<String, Field>> fieldMap = new ObjectMap<>();
	private static final ObjectMap<String, Field> EMP = new ObjectMap<>();

	private static final ObjectSet<Field> finalFields = new ObjectSet<>();

	private static final ObjectMap<Field, MethodHandle> getters = new ObjectMap<>();
	private static final ObjectMap<Field, MethodHandle> setters = new ObjectMap<>();

	private static final boolean useUnsafe;

	static {
		boolean tmp;

		try {
			Log.infoTag("Unsafe", "getUnsafe: " + Unsafer.unsafe);
			tmp = true;
		} catch (Throwable e) {
			Log.err(e);

			tmp = false;
		}
		useUnsafe = tmp;
	}

	public Field getField(Class<?> clazz, String field, boolean isStatic) throws NoSuchFieldException {
		Field res = fieldMap.get(clazz, EMP).get(field);
		if (res != null) return res;

		try {
			if (isStatic) {
				return getField0(clazz, field);
			} else {
				Class<?> curr = clazz;
				while (curr != null) {
					try {
						return getField0(curr, field);
					} catch (NoSuchFieldException ignored) {
					}

					curr = curr.getSuperclass();
				}
			}
		} catch (Throwable e) {
			throw new RuntimeException(e);
		}

		throw new NoSuchFieldException();
	}

	protected Field getField0(Class<?> clazz, String field) throws NoSuchFieldException {
		Field res = clazz.getDeclaredField(field);
		Demodulator.makeModuleOpen(
				clazz.getModule(),
				clazz.getPackage(),
				DesktopFieldAccessHelper.class.getModule()
		);
		res.setAccessible(true);

		if ((res.getModifiers() & Modifier.FINAL) != 0) {
			try {
				Field modifiers = Field.class.getDeclaredField("modifiers");
				Demodulator.makeModuleOpen(
						Field.class.getModule(),
						Field.class.getPackage(),
						DesktopFieldAccessHelper.class.getModule()
				);
				modifiers.setAccessible(true);
				modifiers.set(res, res.getModifiers() & ~Modifier.FINAL);
			} catch (Throwable e) {
				throw new RuntimeException(e);
			}
		}

		return res;
	}

	protected void initField(Field field) {
		getters.get(field, () -> {
			try {
				return lookup.unreflectGetter(field);
			} catch (IllegalAccessException e) {
				throw new RuntimeException(e);
			}
		});
		setters.get(field, () -> {
			try {
				return lookup.unreflectSetter(field);
			} catch (IllegalAccessException e) {
				throw new RuntimeException(e);
			}
		});
	}

	@Override
	public void set(Object object, String field, byte value) {
		try {
			Field f = getField(object.getClass(), field, false);
			if (useUnsafe) {
				Unsafer.setByte(f, object, value);
			} else {
				if (finalFields.contains(f)) {
					f.set(object, value);
					return;
				}

				initField(f);
				setters.get(f).invoke(object, value);
			}
		} catch (Throwable e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void setStatic(Class<?> clazz, String field, byte value) {
		try {
			Field f = getField(clazz, field, false);
			if (useUnsafe) {
				Unsafer.setByte(f, null, value);
			} else {
				if (finalFields.contains(f)) {
					f.set(null, value);
					return;
				}

				initField(f);
				setters.get(f).invoke(value);
			}
		} catch (Throwable e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public byte getByte(Object object, String field) {
		try {
			Field f = getField(object.getClass(), field, false);
			if (useUnsafe) {
				return Unsafer.getByte(f, object);
			} else {
				initField(f);
				return (byte) getters.get(f).invoke(object);
			}
		} catch (Throwable e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public byte getByteStatic(Class<?> clazz, String field) {
		try {
			Field f = getField(clazz, field, true);
			if (useUnsafe) {
				return Unsafer.getByte(f, null);
			} else {
				initField(f);
				return (byte) getters.get(f).invoke();
			}
		} catch (Throwable e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void set(Object object, String field, short value) {
		try {
			Field f = getField(object.getClass(), field, false);
			if (useUnsafe) {
				Unsafer.setShort(f, object, value);
			} else {

				if (finalFields.contains(f)) {
					f.set(object, value);
					return;
				}

				initField(f);
				setters.get(f).invoke(object, value);
			}
		} catch (Throwable e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void setStatic(Class<?> clazz, String field, short value) {
		try {
			Field f = getField(clazz, field, false);
			if (useUnsafe) {
				Unsafer.setShort(f, null, value);
			} else {

				if (finalFields.contains(f)) {
					f.set(null, value);
					return;
				}

				initField(f);
				setters.get(f).invoke(value);
			}
		} catch (Throwable e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public short getShort(Object object, String field) {
		try {
			Field f = getField(object.getClass(), field, false);
			if (useUnsafe) {
				return Unsafer.getShort(f, object);
			} else {
				initField(f);
				return (short) getters.get(f).invoke(object);
			}
		} catch (Throwable e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public short getShortStatic(Class<?> clazz, String field) {
		try {
			Field f = getField(clazz, field, true);
			if (useUnsafe) {
				return Unsafer.getShort(f, null);
			} else {
				initField(f);
				return (short) getters.get(f).invoke();
			}
		} catch (Throwable e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void set(Object object, String field, int value) {
		try {
			Field f = getField(object.getClass(), field, false);
			if (useUnsafe) {
				Unsafer.setInt(f, object, value);
			} else {

				if (finalFields.contains(f)) {
					f.set(object, value);
					return;
				}

				initField(f);
				setters.get(f).invoke(object, value);
			}
		} catch (Throwable e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void setStatic(Class<?> clazz, String field, int value) {
		try {
			Field f = getField(clazz, field, false);
			if (useUnsafe) {
				Unsafer.setInt(f, null, value);
			} else {

				if (finalFields.contains(f)) {
					f.set(null, value);
					return;
				}

				initField(f);
				setters.get(f).invoke(value);
			}
		} catch (Throwable e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public int getInt(Object object, String field) {
		try {
			Field f = getField(object.getClass(), field, false);
			if (useUnsafe) {
				return Unsafer.getInt(f, object);
			} else {
				initField(f);
				return (int) getters.get(f).invoke(object);
			}
		} catch (Throwable e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public int getIntStatic(Class<?> clazz, String field) {
		try {
			Field f = getField(clazz, field, true);
			if (useUnsafe) {
				return Unsafer.getInt(f, null);
			} else {
				initField(f);
				return (int) getters.get(f).invoke();
			}
		} catch (Throwable e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void set(Object object, String field, long value) {
		try {
			Field f = getField(object.getClass(), field, false);
			if (useUnsafe) {
				Unsafer.setLong(f, object, value);
			} else {
				if (finalFields.contains(f)) {
					f.set(object, value);
					return;
				}

				initField(f);
				setters.get(f).invoke(object, value);
			}
		} catch (Throwable e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void setStatic(Class<?> clazz, String field, long value) {
		try {
			Field f = getField(clazz, field, false);
			if (useUnsafe) {
				Unsafer.setLong(f, null, value);
			} else {
				if (finalFields.contains(f)) {
					f.set(null, value);
					return;
				}

				initField(f);
				setters.get(f).invoke(value);
			}
		} catch (Throwable e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public long getLong(Object object, String field) {
		try {
			Field f = getField(object.getClass(), field, false);
			if (useUnsafe) {
				return Unsafer.getLong(f, object);
			} else {
				initField(f);
				return (long) getters.get(f).invoke(object);
			}
		} catch (Throwable e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public long getLongStatic(Class<?> clazz, String field) {
		try {
			Field f = getField(clazz, field, true);
			if (useUnsafe) {
				return Unsafer.getLong(f, null);
			} else {
				initField(f);
				return (long) getters.get(f).invoke();
			}
		} catch (Throwable e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void set(Object object, String field, float value) {
		try {
			Field f = getField(object.getClass(), field, false);
			if (useUnsafe) {
				Unsafer.setFloat(f, object, value);
			} else {
				if (finalFields.contains(f)) {
					f.set(object, value);
					return;
				}

				initField(f);
				setters.get(f).invoke(object, value);
			}
		} catch (Throwable e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void setStatic(Class<?> clazz, String field, float value) {
		try {
			Field f = getField(clazz, field, false);

			if (useUnsafe) {
				Unsafer.setFloat(f, null, value);
			} else {
				if (finalFields.contains(f)) {
					f.set(null, value);
					return;
				}

				initField(f);
				setters.get(f).invoke(value);
			}
		} catch (Throwable e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public float getFloat(Object object, String field) {
		try {
			Field f = getField(object.getClass(), field, false);
			if (useUnsafe) {
				return Unsafer.getFloat(f, object);
			} else {
				initField(f);
				return (float) getters.get(f).invoke(object);
			}
		} catch (Throwable e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public float getFloatStatic(Class<?> clazz, String field) {
		try {
			Field f = getField(clazz, field, true);
			if (useUnsafe) {
				return Unsafer.getFloat(f, null);
			} else {
				initField(f);
				return (float) getters.get(f).invoke();
			}
		} catch (Throwable e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void set(Object object, String field, double value) {
		try {
			Field f = getField(object.getClass(), field, false);

			if (useUnsafe) {
				Unsafer.setDouble(f, object, value);
			} else {
				if (finalFields.contains(f)) {
					f.set(object, value);
					return;
				}

				initField(f);
				setters.get(f).invoke(object, value);
			}
		} catch (Throwable e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void setStatic(Class<?> clazz, String field, double value) {
		try {
			Field f = getField(clazz, field, false);
			if (useUnsafe) {
				Unsafer.setDouble(f, null, value);
			} else {
				if (finalFields.contains(f)) {
					f.set(null, value);
					return;
				}

				initField(f);
				setters.get(f).invoke(value);
			}
		} catch (Throwable e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public double getDouble(Object object, String field) {
		try {
			Field f = getField(object.getClass(), field, false);
			if (useUnsafe) {
				return Unsafer.getDouble(f, object);
			} else {
				initField(f);
				return (double) getters.get(f).invoke(object);
			}
		} catch (Throwable e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public double getDoubleStatic(Class<?> clazz, String field) {
		try {
			Field f = getField(clazz, field, true);
			if (useUnsafe) {
				return Unsafer.getDouble(f, null);
			} else {
				initField(f);
				return (double) getters.get(f).invoke();
			}
		} catch (Throwable e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void set(Object object, String field, boolean value) {
		try {
			Field f = getField(object.getClass(), field, false);

			if (useUnsafe) {
				Unsafer.setBool(f, object, value);
			} else {
				if (finalFields.contains(f)) {
					f.set(object, value);
					return;
				}

				initField(f);
				setters.get(f).invoke(object, value);
			}
		} catch (Throwable e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void setStatic(Class<?> clazz, String field, boolean value) {
		try {
			Field f = getField(clazz, field, false);

			if (useUnsafe) {
				Unsafer.setBool(f, null, value);
			} else {
				if (finalFields.contains(f)) {
					f.set(null, value);
					return;
				}

				initField(f);
				setters.get(f).invoke(value);
			}
		} catch (Throwable e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public boolean getBoolean(Object object, String field) {
		try {
			Field f = getField(object.getClass(), field, false);
			if (useUnsafe) {
				return Unsafer.getBool(f, object);
			} else {
				initField(f);
				return (boolean) getters.get(f).invoke(object);
			}
		} catch (Throwable e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public boolean getBooleanStatic(Class<?> clazz, String field) {
		try {
			Field f = getField(clazz, field, true);
			if (useUnsafe) {
				return Unsafer.getBool(f, null);
			} else {
				initField(f);
				return (boolean) getters.get(f).invoke();
			}
		} catch (Throwable e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void set(Object object, String field, Object value) {
		try {
			Field f = getField(object.getClass(), field, false);

			if (useUnsafe) {
				Unsafer.set(f, object, value);
			} else {
				if (finalFields.contains(f)) {
					f.set(object, value);
					return;
				}

				initField(f);
				setters.get(f).invoke(object, value);
			}
		} catch (Throwable e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void setStatic(Class<?> clazz, String field, Object value) {
		try {
			Field f = getField(clazz, field, false);

			if (useUnsafe) {
				Unsafer.set(f, null, value);
			} else {
				if (finalFields.contains(f)) {
					f.set(null, value);
					return;
				}

				initField(f);
				setters.get(f).invoke(value);
			}
		} catch (Throwable e) {
			throw new RuntimeException(e);
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T get(Object object, String field) {
		try {
			Field f = getField(object.getClass(), field, false);
			if (useUnsafe) {
				return (T) Unsafer.get(f, object);
			} else {
				initField(f);
				return (T) getters.get(f).invoke(object);
			}
		} catch (Throwable e) {
			throw new RuntimeException(e);
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T getStatic(Class<?> clazz, String field) {
		try {
			Field f = getField(clazz, field, true);
			if (useUnsafe) {
				return (T) Unsafer.get(f, null);
			} else {
				initField(f);
				return (T) getters.get(f).invoke();
			}
		} catch (Throwable e) {
			throw new RuntimeException(e);
		}
	}
}
