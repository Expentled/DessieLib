package me.dessie.dessielib.core.utils;

import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class ClassUtil {

    /**
     * Retrieves all Classes in the provided plugin.
     *
     * @param plugin The plugin to get the classes for.
     * @return All classes within the plugin.
     */
    public static List<Class<?>> getClasses(JavaPlugin plugin) {
        return getClasses(plugin, null);
    }

    /**
     * Retrieves all Classes in the provided plugin, contained within a specific package.
     *
     * @param plugin The plugin to get the classes for.
     * @param packageName The package to look in.
     * @return All classes in the provided package within the plugin.
     */
    public static List<Class<?>> getClasses(JavaPlugin plugin, String packageName) {
        JarFile file;
        List<Class<?>> classes = new ArrayList<>();

        try {
            Method method = JavaPlugin.class.getDeclaredMethod("getFile");
            method.setAccessible(true);
            file = new JarFile((File) method.invoke(plugin));

            Enumeration<JarEntry> entries = file.entries();
            while (entries.hasMoreElements()) {
                JarEntry entry = entries.nextElement();

                //Rename the JarEntry to use . instead of /
                String name = entry.getName().replace("/", ".");

                //If it's not a class, skip it.
                if(!name.endsWith(".class")) continue;

                //Only register if the class exists within the package, or a package wasn't provided.
                if (packageName != null && !packageName.equals("") && !name.startsWith(packageName)) continue;

                classes.add(Class.forName(name.substring(0, name.length() - 6)));
            }
        } catch (IOException | InvocationTargetException | NoSuchMethodException | IllegalAccessException | ClassNotFoundException e) {
            e.printStackTrace();
        }

        return classes;
    }

    /**
     * Retrieves all typed Classes in the provided plugin, contained within a specific package.
     *
     * @param type The type of Class to get
     * @param plugin The plugin to get the classes for.
     * @param packageName The package to look in.
     * @param <T> The type of class to get
     * @return All typed classes in the provided package within the plugin.
     */
    @SuppressWarnings("unchecked")
    public static <T> List<Class<T>> getClasses(Class<T> type, JavaPlugin plugin, String packageName) {
        JarFile file;
        List<Class<T>> classes = new ArrayList<>();

        try {
            Method method = JavaPlugin.class.getDeclaredMethod("getFile");
            method.setAccessible(true);
            file = new JarFile((File) method.invoke(plugin));

            Enumeration<JarEntry> entries = file.entries();
            while (entries.hasMoreElements()) {
                JarEntry entry = entries.nextElement();

                //Rename the JarEntry to use . instead of /
                String name = entry.getName().replace("/", ".");

                //If it's not a class, skip it.
                if(!name.endsWith(".class")) continue;

                //Only register if the class exists within the package, or a package wasn't provided.
                if (packageName != null && !packageName.equals("") && !name.startsWith(packageName)) continue;

                Class<?> clazz = Class.forName(name.substring(0, name.length() - 6));
                if(!type.isAssignableFrom(clazz)) continue;
                try {
                    classes.add((Class<T>) clazz);
                } catch (ClassCastException ignored) {}
            }
        } catch (IOException | InvocationTargetException | NoSuchMethodException | IllegalAccessException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        return classes;
    }
}
