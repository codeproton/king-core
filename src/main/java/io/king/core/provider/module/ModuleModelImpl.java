package io.king.core.provider.module;

import io.king.core.api.cycle.CycleLoader;
import io.king.core.api.cycle.LifeContext;
import io.king.core.api.cycle.LifeCycle;
import io.king.core.api.module.Module;
import io.king.core.api.module.ModuleManager;
import io.king.core.api.module.ModuleModel;
import io.king.core.provider.Util;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
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
    private static final String FILE_MODULE_IDENTIFICATION = "module";
    private final static Class<Module> MODULE_CLASS = Module.class;
    private static final Util UTIL = Util.getInstance();

    private final JavaPlugin plugin;
    private final File moduleFolder;
    private final CycleLoader cycleLoader;

    public ModuleModelImpl(JavaPlugin plugin, CycleLoader loader, LifeContext context) {
        this(plugin, loader, context, "/modules");
    }

    public ModuleModelImpl(JavaPlugin plugin, CycleLoader cycleLoader, LifeContext context, String folder) {
        this.moduleFolder = new File(plugin.getDataFolder(), folder);
        this.cycleLoader = cycleLoader;
        this.plugin = plugin;
    }

    public ModuleManager load() throws Exception {
        if (!moduleFolder.exists()) moduleFolder.mkdir();

        File[] moduleFiles = moduleFolder.listFiles();
        if (moduleFiles == null) throw new NoSuchElementException();

        final List<ModuleObject> moduleObjects = new LinkedList<>();
        for (File file : moduleFiles) {
            if (!file.isFile()) continue;

            final ModuleObject moduleObject = loadFile(file);
            moduleObjects.add(moduleObject);
        }

        return new ModuleManagerImpl(plugin, moduleObjects, cycleLoader);
    }

    public ModuleObject loadFile(File file) throws IOException, ClassNotFoundException {
        final URL url = file.toURI().toURL();
        final URLClassLoader urlClassLoader = URLClassLoader.newInstance(
                new URL[]{url}, LIFE_CYCLE_CLASS_LOADER
        );

        final String mainClass = loadJarFile(file);
        final Class<?> clazz = urlClassLoader.loadClass(mainClass);

        final Module moduleAnnotation = clazz.getAnnotation(MODULE_CLASS);
        if (moduleAnnotation == null) throw new NoSuchElementException("Sub-module should be annoted with @Module");

        urlClassLoader.close();
        return new ModuleObject(mainClass, clazz, moduleAnnotation);
    }

    public String loadJarFile(File file) throws IOException {
        final JarFile jarFile = new JarFile(file);

        final Enumeration<JarEntry> entries = jarFile.entries();
        while (entries.hasMoreElements()) {
            final JarEntry entry = entries.nextElement();
            if (entry.isDirectory() || !entry.getName().equalsIgnoreCase(FILE_MODULE_IDENTIFICATION)) continue;

            final InputStream inputStream = jarFile.getInputStream(entry);
            final String trim = UTIL.loadBytes(inputStream).trim();

            /*
             * Close the streams
             */
            inputStream.close();
            jarFile.close();

            return trim;
        }

        /*
         * Close the stream
         */
        jarFile.close();
        return null;
    }
}