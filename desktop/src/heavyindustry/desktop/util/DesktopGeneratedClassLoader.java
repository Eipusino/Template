package heavyindustry.desktop.util;

import arc.Core;
import arc.files.Fi;
import arc.files.ZipFi;
import arc.util.Log;
import heavyindustry.desktop.Demodulator;
import heavyindustry.mod.ModInfo;
import heavyindustry.util.AbstractGeneratedClassLoader;
import heavyindustry.util.JarList;

import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.security.ProtectionDomain;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

public class DesktopGeneratedClassLoader extends AbstractGeneratedClassLoader {
	private static final Fi jarFileCache = Core.settings.getDataDirectory().child("heavyindustry").child("cache");

	public static final Fi tmpFile = jarFileCache.child("temp-file.jar");

	private static final Object unsafe;
	private static final Method defineClass;

	static {
		try {
			Demodulator.makeModuleOpen(Object.class.getModule(), "jdk.internal.misc", DesktopGeneratedClassLoader.class.getModule());

			Class<?> clazz = Class.forName("jdk.internal.misc.Unsafe");

			defineClass = clazz.getDeclaredMethod("defineClass", String.class, byte[].class, int.class, int.class, ClassLoader.class, ProtectionDomain.class);

			Constructor<?> cstr = clazz.getDeclaredConstructor();
			cstr.setAccessible(true);
			unsafe = cstr.newInstance();
		} catch (NoSuchMethodException | InstantiationException | IllegalAccessException | InvocationTargetException |
		         ClassNotFoundException e) {
			throw new RuntimeException(e);
		}
	}

	private final HashMap<String, Class<?>> classMap = new HashMap<>();

	protected ModInfo mod;

	private ZipFi zip;

	public DesktopGeneratedClassLoader(ModInfo mod, ClassLoader parent) {
		super(JarList.inst().loadCacheFile(mod).file(), parent);
		this.mod = mod;
	}

	protected Class<?> defineClass(String name, byte[] bytes, Class<?> accessor) {
		try {
			return (Class<?>) defineClass.invoke(unsafe, name, bytes, 0, bytes.length, accessor.getClassLoader(), null);
		} catch (IllegalAccessException | InvocationTargetException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void declareClass(String name, byte[] byteCode) {
		if (classMap.containsKey(name)) return;

		boolean existed;
		try {
			existed = !file.createNewFile();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}

		if (existed) {
			zip = new ZipFi(new Fi(file));
			new Fi(file).copyTo(tmpFile);
		}

		try (ZipOutputStream outputStream = new ZipOutputStream(new FileOutputStream(file, false))) {
			String entryName = name.replace(".", "/") + ".class";

			ZipEntry declaringEntry = new ZipEntry(entryName);

			if (existed) {
				try {
					ZipFile tempZipped = new ZipFile(tmpFile.file());

					Enumeration<? extends ZipEntry> entries = tempZipped.entries();
					while (entries.hasMoreElements()) {
						ZipEntry entry = entries.nextElement();
						if (entry.isDirectory()) continue;

						outputStream.putNextEntry(new ZipEntry(entry));
						try (InputStream inputStream = tempZipped.getInputStream(entry)) {
							for (int l = inputStream.read(); l > -1; l = inputStream.read()) {
								outputStream.write(l);
							}
							outputStream.closeEntry();
							outputStream.flush();
						}
					}

					tempZipped.close();
				} catch (ZipException e) {
					Log.warn("[GeneratedClassLoader] cache zip format error or it was an empty zip, direct write byte code");
				} finally {
					tmpFile.delete();
				}
			}

			outputStream.putNextEntry(declaringEntry);
			outputStream.write(byteCode);
			outputStream.closeEntry();
			outputStream.finish();
			outputStream.flush();

			zip = new ZipFi(new Fi(file));
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	protected Class<?> findClass(String name) throws ClassNotFoundException {
		return findClass(name, (Class<?>) null);
	}

	protected Class<?> findClass(String name, Class<?> accessor) throws ClassNotFoundException {
		try {
			return super.findClass(name);
		} catch (ClassNotFoundException ne) {
			Class<?> res = classMap.computeIfAbsent(name, n -> {
				String[] classPath = n.split("\\.");
				classPath[classPath.length - 1] = classPath[classPath.length - 1] + ".class";

				if (zip == null) {
					if (!file.exists())
						return null;

					zip = new ZipFi(new Fi(file));
				}

				Fi f = zip;
				for (String path : classPath) {
					f = f.child(path);
				}

				try (InputStream in = f.read()) {
					ByteArrayOutputStream w = new ByteArrayOutputStream();
					int i;
					while ((i = in.read()) != -1) {
						w.write(i);
					}
					byte[] byteCode = w.toByteArray();

					return accessor != null ? defineClass(name, byteCode, accessor)
							: defineClass(n, byteCode, 0, byteCode.length);
				} catch (IOException | RuntimeException ignored1) {
					return null;
				}
			});

			if (res == null) {
				throw new ClassNotFoundException("no such class: " + name + ", try relaunch");
			}

			return res;
		}
	}

	@Override
	public Class<?> loadClass(String name, Class<?> accessor, boolean resolve) throws ClassNotFoundException {
		Class<?> res;
		try {
			res = getParent().loadClass(name);
		} catch (ClassNotFoundException ignored) {
			res = findClass(name, accessor);
		}

		if (resolve) resolveClass(res);

		return res;
	}
}
