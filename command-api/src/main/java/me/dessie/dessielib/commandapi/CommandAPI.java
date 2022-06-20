package me.dessie.dessielib.commandapi;

import me.dessie.dessielib.core.utils.ClassUtil;
import org.bukkit.plugin.java.JavaPlugin;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Can register commands without them being in the plugin.yml
 */
public class CommandAPI {

    private static JavaPlugin plugin;
    private static boolean registered = false;

    private static final List<XCommand> commands = new ArrayList<>();

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
        for(Class<XCommand> clazz : ClassUtil.getClasses(XCommand.class, CommandAPI.getPlugin(), packageName)) {
            for (Constructor<?> constructor : clazz.getConstructors()) {
                if (constructor.getParameterCount() != 0) continue;
                try {
                    XCommand command = (XCommand) constructor.newInstance();
                    command.register();
                } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
                    e.printStackTrace();
                }
            }
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
            throw new IllegalStateException("CommandAPI already registered to " + getPlugin().getName());
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

    /**
     * Returns all the registered {@link XCommand}s.
     * @return All registered XCommands.
     */
    public static List<XCommand> getCommands() {
        return commands;
    }

    /**
     * Finds a registered {@link XCommand} that has the provided name or alias.
     *
     * @param name The name or alias to find.
     * @return The XCommand that has the provided name or alias, or null if it does not exist.
     */
    public static XCommand findCommand(String name) {
        return getCommands().stream().filter(command -> command.getName().equalsIgnoreCase(name)
                || command.getAliases().contains(name.toLowerCase(Locale.ROOT))).findFirst().orElse(null);
    }
}
