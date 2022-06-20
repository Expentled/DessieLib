package me.dessie.dessielib.commandapi;

import me.dessie.dessielib.core.utils.Colors;
import org.bukkit.Bukkit;
import org.bukkit.command.*;
import org.bukkit.craftbukkit.v1_19_R1.CraftServer;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.util.StringUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Main registering class for XCommand.
 */
public abstract class XCommand implements TabExecutor {

    private static final SimpleCommandMap map = ((CraftServer) Bukkit.getServer()).getCommandMap();

    private final String name;
    private final String description;
    private final List<String> aliases = new ArrayList<>();
    private String permission;
    private String usage;
    private String permissionMessage;

    private boolean registered = false;

    /**
     * @param name The name of the command.
     * @param description The description of the command.
     */
    public XCommand(String name, String description) {
        this(name, description, null);
    }

    /**
     * @param name The name of the command.
     * @param description The description of the command.
     * @param permission The default permission of the command.
     */
    public XCommand(String name, String description, @Nullable String permission) {
        this.name = name;
        this.description = description;
        this.permission = permission;
    }

    /**
     * Overridable method that is called when this command is executed by a {@link Player}.
     * @param player The Player that ran the command.
     * @param args The arguments that were passed with the command.
     */
    protected void execute(Player player, String[] args) {}

    /**
     * Overridable method that is called when this command is executed by a {@link ConsoleCommandSender}.
     * @param console The console sender that ran the command.
     * @param args The arguments that were passed with the command.
     */
    protected void execute(ConsoleCommandSender console, String[] args) {}

    /**
     * Overridable method that is called when this command is executed by any sender.
     *
     * @see XCommand#execute(Player, String[]) if you just want a {@link Player} to run this command.
     * @see XCommand#execute(ConsoleCommandSender, String[]) if you just want {@link ConsoleCommandSender} to run this command.
     *
     * @param sender The {@link CommandSender} that ran the command.
     * @param args The arguments that were passed with the command.
     */
    protected void execute(CommandSender sender, String[] args) {}

    /**
     * Overridable method that is called when an argument is tab completed on this command by any sender.
     *
     * The options returned will automatically be matched partially to what they have currently written.
     *
     * @param sender The {@link CommandSender} that tab completed.
     * @param args The current arguments of the command line.
     * @return A list of the tab completion options.
     */
    protected List<String> executeTab(CommandSender sender, String[] args) {
        return new ArrayList<>();
    }

    /**
     * @return The name of the command
     */
    public String getName() {return name;}

    /**
     * @return The description of the command
     */
    public String getDescription() {return description;}

    /**
     * @return The default permission for this command
     */
    public String getPermission() {return permission;}

    /**
     * @return All aliases that function as this command.
     */
    public List<String> getAliases() {return aliases;}

    /**
     * @return The permission message that is sent when a player uses this command without permission.
     */
    public String getPermissionMessage() {return permissionMessage;}

    /**
     * @return The usage message when a user incorrectly uses the command.
     */
    public String getUsage() {return usage;}

    /**
     * @return If this command has been registered
     */
    public boolean isRegistered() {
        return registered;
    }

    /**
     * Adds command aliases for this command.
     *
     * @param aliases The aliases to add
     * @return The XCommand instance
     */
    public XCommand addAliases(String... aliases) {
        this.aliases.addAll(Arrays.asList(aliases));
        return this;
    }

    /**
     * Sets the usage message for this command
     *
     * @param usage The usage message
     * @return The XCommand instance
     */
    public XCommand setUsage(String usage) {
        this.usage = usage;
        return this;
    }

    /**
     * Sets the required permission to run this command.
     *
     * @param permission The permission that is required.
     * @return The XCommand instance
     */
    public XCommand setPermission(String permission) {
        this.permission = permission;
        return this;
    }

    /**
     * Sets the permission message for this command
     *
     * @param permissionMessage The permission message
     * @return The XCommand instance
     */
    public XCommand setPermissionMessage(String permissionMessage) {
        this.permissionMessage = Colors.color(permissionMessage);
        return this;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        XCommand xCommand = CommandAPI.findCommand(command.getName());
        if(xCommand == null) return false;

        if (sender instanceof Player player) {
            xCommand.execute(player, args);
        } else if (sender instanceof ConsoleCommandSender console) {
            xCommand.execute(console, args);
        }

        xCommand.execute(sender, args);

        return true;
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        XCommand xCommand = CommandAPI.findCommand(command.getName());

        if(xCommand == null) return null;
        List<String> completions = xCommand.executeTab(sender, args);
        return completions == null ? null : StringUtil.copyPartialMatches(args[args.length - 1], completions, new ArrayList<>());
    }

    void register() {
        //Don't register a command that's already been registered.
        if(isRegistered()) return;

        CommandAPI.getCommands().add(this);

        //Instantiate the protected PluginCommand constructor.
        try {
            Constructor<PluginCommand> constructor = PluginCommand.class.getDeclaredConstructor(String.class, Plugin.class);;
            constructor.setAccessible(true);

            //Create the PluginCommand
            PluginCommand command = constructor.newInstance(this.getName(), CommandAPI.getPlugin());
            command.setExecutor(this);
            command.setTabCompleter(this);
            command.setPermission(this.getPermission());
            command.setDescription(this.getDescription());
            command.setAliases(this.getAliases().stream().map(String::toLowerCase).toList());
            command.setPermissionMessage(this.getPermissionMessage());
            command.setUsage(this.getUsage());

            map.register(CommandAPI.getPlugin().getName(), command);

        }  catch (InvocationTargetException | InstantiationException | IllegalAccessException | NoSuchMethodException e) {
            e.printStackTrace();
        }

        this.registered = true;
    }

}
