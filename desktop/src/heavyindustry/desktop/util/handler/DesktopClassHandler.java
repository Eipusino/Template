package heavyindustry.desktop.util.handler;

import dynamilize.DynamicMaker;
import dynamilize.PackageAccHandler;
import dynamilize.classmaker.ASMGenerator;
import dynamilize.classmaker.AbstractClassGenerator;
import dynamilize.classmaker.ByteClassLoader;
import dynamilize.classmaker.ClassInfo;
import heavyindustry.desktop.util.DesktopDynamicClassLoader;
import heavyindustry.desktop.util.DesktopGeneratedClassLoader;
import heavyindustry.mod.ModGetter;
import heavyindustry.mod.ModInfo;
import heavyindustry.util.AbstractDynamicClassLoader;
import heavyindustry.util.AbstractFileClassLoader;
import heavyindustry.util.AbstractGeneratedClassLoader;
import heavyindustry.util.DefaultHandleHelper;
import heavyindustry.util.JarList;
import heavyindustry.util.handler.ClassHandler;
import mindustry.Vars;
import mindustry.mod.Mod;
import org.objectweb.asm.Opcodes;

public class DesktopClassHandler implements ClassHandler {
	protected final ModInfo mod;
	protected AbstractDynamicClassLoader dynamicLoader;
	protected AbstractGeneratedClassLoader generatedLoader;
	protected ASMGenerator generator;
	protected PackageAccHandler accHandler;
	protected DynamicMaker maker;

	private boolean generateFinished;

	public DesktopClassHandler(ModInfo mod) {
		this.mod = mod;
		initLoaders();
	}

	protected void initLoaders() {
		generatedLoader = new DesktopGeneratedClassLoader(mod, Vars.mods.mainLoader());
		dynamicLoader = new DesktopDynamicClassLoader(generatedLoader);
	}

	@Override
	@SuppressWarnings("unchecked")
	public ClassHandler newInstance(Class<?> modMain) {
		if (!Mod.class.isAssignableFrom(modMain))
			throw new IllegalArgumentException("require class is child of Mod");

		ModInfo mod = ModGetter.getModWithClass((Class<? extends Mod>) modMain);
		if (mod == null)
			throw new IllegalArgumentException("mod that inputted main class was not found");

		return new DesktopClassHandler(mod);
	}

	@Override
	public AbstractClassGenerator getGenerator() {
		return generator != null ? generator : (generator = new ASMGenerator(new ByteClassLoader() {
			@Override
			public void declareClass(String name, byte[] byteCode) {
				currLoader().declareClass(name, byteCode);
			}

			@Override
			public Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
				return currLoader().loadClass(name, resolve);
			}
		}, Opcodes.V1_8) {
			@Override
			@SuppressWarnings("unchecked")
			protected <T> Class<T> generateClass(ClassInfo<T> classInfo) throws ClassNotFoundException {
				try {
					return (Class<T>) currLoader().loadClass(classInfo.name());
				} catch (ClassNotFoundException ignored) {
					return super.generateClass(classInfo);
				}
			}
		});
	}

	public PackageAccHandler getAccHandler() {
		return accHandler != null ? accHandler : (accHandler = new PackageAccHandler() {
			@SuppressWarnings("unchecked")
			@Override
			protected <T> Class<? extends T> loadClass(ClassInfo<?> clazz, Class<T> baseClass) {
				try {
					return (Class<? extends T>) new ASMGenerator(new ByteClassLoader() {
						@Override
						public void declareClass(String s, byte[] bytes) {
							currLoader().declareClass(s, bytes);
						}

						@Override
						public Class<?> loadClass(String s, boolean b) throws ClassNotFoundException {
							return currLoader().loadClass(s, baseClass, b);
						}
					}, Opcodes.V1_8) {
						@Override
						protected <Ty> Class<Ty> generateClass(ClassInfo<Ty> classInfo) throws ClassNotFoundException {
							try {
								return (Class<Ty>) currLoader().loadClass(classInfo.name(), baseClass, false);
							} catch (ClassNotFoundException ignored) {
								return super.generateClass(classInfo);
							}
						}
					}.generateClass(clazz);
				} catch (ClassNotFoundException e) {
					throw new RuntimeException(e);
				}
			}
		});
	}

	@Override
	public DynamicMaker getDynamicMaker() {
		return maker != null ? maker : (maker = new DynamicMaker(new DefaultHandleHelper()) {
			@Override
			@SuppressWarnings("unchecked")
			protected <T> Class<? extends T> generateClass(Class<T> baseClass, Class<?>[] interfaces, Class<?>[] aspects) {
				String name = getDynamicName(baseClass, interfaces);

				try {
					return (Class<? extends T>) currLoader().loadClass(name);
				} catch (ClassNotFoundException ignored) {
					return makeClassInfo(baseClass, interfaces, aspects).generate(getGenerator());
				}
			}

			@Override
			protected <T> Class<? extends T> handleBaseClass(Class<T> baseClass) {
				return getAccHandler().handle(baseClass);
			}
		});
	}

	@Override
	public AbstractFileClassLoader currLoader() {
		return generateFinished ? dynamicLoader : generatedLoader;
	}

	@Override
	public void finishGenerate() {
		generateFinished = true;
		JarList.inst().update(mod);
	}
}
