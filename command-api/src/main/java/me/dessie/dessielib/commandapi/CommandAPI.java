package me.dessie.dessielib.commandapi;

import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * Can register commands without them being in the plugin.yml
 */
public class CommandAPI {

    private static JavaPlugin plugin;
    private static boolean registered = false;

    /**
     * @param registerAllCommands If all commands should be registered by default.
     *                            This will automatically call {@link CommandAPI#registerAllCommands(String)} if true.
     */
    private CommandAPI(boolean registerAllCommands) {
        if(registerAllCommands) {
            this.registerAllCommands(null);
        }
    }

    /**
     * Registers a {@link XCommand} to the server.
     *
     * @param command The command to register.
     */
    public void registerCommand(XCommand command) {
        command.register();
    }

    /**
     * Registers all commands within a package of the plugin.
     * To register all commands within the plugin, pass an empty string or null.
     *
     * Note: If your command does not have a default constructor, it will not be registered with this method.
     * Commands that require any arguments will still need to be registered manually using {@link CommandAPI#registerCommand(XCommand)}
     *
     * @param packageName The package to search for commands in.
     */
    public void registerAllCommands(String packageName) {
        JarFile file;
        try {
            Method method = JavaPlugin.class.getDeclaredMethod("getFile");
            method.setAccessible(true);
            file = new JarFile((File) method.invoke(CommandAPI.getPlugin()));

            Enumeration<JarEntry> entries = file.entries();
            while(entries.hasMoreElements()) {
                JarEntry entry = entries.nextElement();

                //Rename the JarEntry to use . instead of /
                String name = entry.getName().replace("/", ".");

                //If it's not a class, skip it.
                if(!name.endsWith(".class")) continue;

                //Only register if the class exists within the package, or a package wasn't provided.
                if (packageName != null && !packageName.equals("") && !name.startsWith(packageName)) continue;

                Class<?> clazz = Class.forName(name.substring(0, name.length() - 6));

                //Register the command with the default constructor.
                if (!XCommand.class.isAssignableFrom(clazz)) continue;

                for (Constructor<?> constructor : clazz.getConstructors()) {
                    if (constructor.getParameterCount() != 0) continue;

                    XCommand command = (XCommand) constructor.newInstance();
                    command.register();
                }
            }
        } catch (IOException | ClassNotFoundException | InvocationTargetException | NoSuchMethodException | IllegalAccessException | InstantiationException e) {
            e.printStackTrace();
        }
    }

    /**
     * Register the API to use your plugin.
     * @param yourPlugin Your plugin instance.
     * @return The CommandAPI instance that was registered.
     */
    public static CommandAPI register(JavaPlugin yourPlugin) {
        return register(yourPlugin, false);
    }

    /**
     * Register the API to use your plugin.
     * @param yourPlugin Your plugin instance.
     * @param registerAllCommands If all commands should be registered by default.
     * @return A CommandAPI instance.
     */
    public static CommandAPI register(JavaPlugin yourPlugin, boolean registerAllCommands) {
        if(isRegistered()) {
            throw new IllegalStateException("ResourcePack already registered to " + getPlugin().getName());
        }
        plugin = yourPlugin;
        registered = true;

        return new CommandAPI(registerAllCommands);
    }

    /**
     * @return The plugin that registered the CommandAPI.
     */
    public static JavaPlugin getPlugin() {
        return plugin;
    }

    /**
     * @return If CommandAPI is registered.
     */
    public static boolean isRegistered() {
        return registered;
    }
}
