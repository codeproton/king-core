package io.king.core.provider.module;

import io.king.core.api.cycle.CycleLoader;
import io.king.core.api.cycle.LifeCycle;
import io.king.core.api.module.Module;
import io.king.core.api.module.ModuleManager;
import io.king.core.api.module.ModuleModel;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public final class ModuleModelImpl implements ModuleModel {

    private static final ClassLoader LIFE_CYCLE_CLASS_LOADER = LifeCycle.class.getClassLoader();
    private final static Class<Module> MODULE_CLASS = Module.class;

    private final CycleLoader cycleLoader;
    private final JavaPlugin plugin;
    private final File moduleFolder;

    public ModuleModelImpl(
      JavaPlugin plugin,
      CycleLoader loader
    ) {
        this(plugin, loader, "/modules");
    }

    public ModuleModelImpl(
      JavaPlugin plugin,
      CycleLoader cycleLoader,
      String folder
    ) {
        this.moduleFolder = new File(plugin.getDataFolder(), folder);
        this.cycleLoader = cycleLoader;
        this.plugin = plugin;

        cycleLoader.setModuleDirectory(moduleFolder);
    }

    public ModuleManager load() throws Exception {
        if (!moduleFolder.exists()) moduleFolder.mkdir();

        File[] moduleFiles = moduleFolder.listFiles();
        if (moduleFiles == null)
            throw new NoSuchElementException("Can´t read files of folder.");

        final List<ModuleObject> moduleObjects = new LinkedList<>();
        final URLClassLoader urlClassLoader = resolveClasses(moduleFiles);

        for (File file : moduleFiles) {
            if (!file.isFile()) continue;

            final ModuleObject moduleObject = loadFile(file, urlClassLoader);
            moduleObjects.add(moduleObject);
        }

        urlClassLoader.close();
        return new ModuleManagerImpl(
          moduleObjects,
          cycleLoader,
          plugin
        );
    }

    @Override
    public URLClassLoader resolveClasses(File[] moduleFiles) throws MalformedURLException {
        URL[] urls = new URL[moduleFiles.length];

        for (int i = 0; i < urls.length; i++) {
            urls[i] = moduleFiles[i].toURI().toURL();
        }

        return URLClassLoader.newInstance(
          urls,
          LIFE_CYCLE_CLASS_LOADER
        );
    }

    @Override
    public ModuleObject loadFile(File file, ClassLoader classLoader) throws Exception {
        final long oldMsTime = System.currentTimeMillis();

        final ModuleProps moduleProps = loadJarFile(file, classLoader);
        final Class<?> clazz = Class.forName(
          moduleProps.getMainClass().getName(),
          true,
          classLoader
        );

        final Module moduleAnnotation = clazz.getAnnotation(MODULE_CLASS);
        if (moduleAnnotation == null) throw new NoSuchElementException(
          "Module should be annotated with @Module"
        );

        final long delayedLoad = System.currentTimeMillis() - oldMsTime;

        return new ModuleObject(
          moduleProps,
          clazz,
          delayedLoad,
          moduleAnnotation,
          file
        );
    }

    @Override
    public ModuleProps loadJarFile(File file, ClassLoader classLoader) throws Exception {
        final JarFile jarFile = new JarFile(file);

        final Enumeration<JarEntry> entries = jarFile.entries();
        Class<?> mainClass = null;

        while (entries.hasMoreElements()) {
            final JarEntry entry = entries.nextElement();
            if (entry.isDirectory()) continue;

            /*
             * Replace me/author/Main.class to me.author.Main
             */
            String name = entry.getName().replace("/", ".");
            if (name.endsWith(".class")) {
                name = name.substring(0, name.length() - 6);
                final Class<?> loadedClass = Class.forName(name, true, classLoader);

                final boolean isPresent = loadedClass.isAnnotationPresent(MODULE_CLASS);
                if (!isPresent) continue;

                mainClass = loadedClass;
            }
        }

        final String jarFileName = jarFile.getName();
        jarFile.close();

        if (mainClass == null) throw new NoSuchElementException(
          "No classes were annotated with @Module in this file."
        );

        final long loadedAtTime = System.currentTimeMillis();
        return new ModuleProps(
          mainClass,
          jarFileName,
          loadedAtTime
        );
    }
}
