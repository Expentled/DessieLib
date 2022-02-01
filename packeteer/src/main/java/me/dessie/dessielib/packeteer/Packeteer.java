package me.dessie.dessielib.packeteer;

import io.netty.channel.*;
import io.netty.util.concurrent.Promise;
import net.minecraft.network.protocol.Packet;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_18_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.server.PluginEnableEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

/**
 * Event handlers for incoming and outgoing packets.
 *
 * Packeteer handlers should have a Packet and a Player as parameters.
 */
public class Packeteer implements Listener {

    private static final List<PacketListener> HANDLERS = new ArrayList<>();

    private static boolean registered;
    private static final List<UUID> injectedPlayers = new ArrayList<>();
    private static JavaPlugin plugin;

    private Packeteer() {
        getPlugin().getServer().getPluginManager().registerEvents(this, getPlugin());
    }

    /**
     * Enables Packeteer to start listening for packets.
     * @param yourPlugin Your plugin instance
     * @return A {@link Packeteer} instance for usage.
     */
    public static Packeteer register(JavaPlugin yourPlugin) {
        if(isRegistered()) {
            throw new IllegalStateException("Cannot register Packeteer in " + yourPlugin.getName() + ". Already registered by " + getPlugin().getName());
        }

        registered = true;
        plugin = yourPlugin;

        return new Packeteer();
    }

    /**
     * @return If Packeteer is currently injecting players into a packet pipeline.
     */
    public static boolean isRegistered() {
        return registered;
    }

    /**
     * @return All Players that are currently injected into Packeteer's Pipeline
     */
    public static List<UUID> getInjectedPlayers() {return injectedPlayers;}
    public static JavaPlugin getPlugin() {return plugin;}

    /**
     * Adds a Listener class into Packeteer.
     * All Listener methods should have the {@link PacketeerHandler} Annotation
     *
     * @param listener Your PacketListener instance
     */
    public void addListener(PacketListener listener) {
        HANDLERS.add(listener);
    }

    private void handle(Player player, Packet<?> packet) {
        for(PacketListener listener : HANDLERS) {
            for(Method method : listener.getClass().getDeclaredMethods()) {
                if(!method.isAnnotationPresent(PacketeerHandler.class)) continue;
                if(method.getParameterCount() == 1) {
                    if(!Arrays.stream(method.getParameterTypes()).allMatch(param -> param.isAssignableFrom(packet.getClass()))) continue;

                    method.setAccessible(true);
                    Bukkit.getScheduler().runTask(getPlugin(), () -> {
                        try {
                            method.invoke(listener, packet, player);
                        } catch (IllegalAccessException | InvocationTargetException e) {
                            e.printStackTrace();
                        }
                    });

                } else if(method.getParameterCount() == 2) {
                    if(method.getParameterTypes()[0] != packet.getClass() || method.getParameterTypes()[1] != Player.class) continue;

                    method.setAccessible(true);
                    Bukkit.getScheduler().runTask(getPlugin(), () -> {
                        try {
                            method.invoke(listener, packet, player);
                        } catch (IllegalAccessException | InvocationTargetException e) {
                            e.printStackTrace();
                        }
                    });
                }
            }
        }
    }

    @EventHandler
    private void onJoin(PlayerJoinEvent event) {
        if(isRegistered()) {
            inject(event.getPlayer());
        }
    }

    @EventHandler
    private void onQuit(PlayerQuitEvent event) {
        if(getInjectedPlayers().contains(event.getPlayer().getUniqueId())) {
            unInject(event.getPlayer());
            getInjectedPlayers().remove(event.getPlayer().getUniqueId());
        }
    }

    @EventHandler
    private void onEnable(PluginEnableEvent event) {
        if(event.getPlugin() != getPlugin()) return;
        for (Player player : Bukkit.getOnlinePlayers()) {
            inject(player);
        }
    }

    private Promise<?> unInject(Player player) {
        Channel channel = ((CraftPlayer) player).getHandle().connection.connection.channel;
        return (Promise<?>) channel.eventLoop().submit(() -> {
            channel.pipeline().remove(player.getName());
        });
    }

    private void inject(Player player) {
        unInject(player).addListener(future -> {
            ChannelDuplexHandler handler = new ChannelDuplexHandler() {
                @Override
                public void write(ChannelHandlerContext ctx, Object obj, ChannelPromise promise) throws Exception {
                    if(obj instanceof Packet packet) {
                        handle(player, packet);
                    }
                    super.write(ctx, obj, promise);
                }

                @Override
                public void channelRead(ChannelHandlerContext ctx, Object obj) throws Exception {
                    if (obj instanceof Packet packet) {
                        handle(player, packet);
                    }
                    super.channelRead(ctx, obj);
                }
            };

            ChannelPipeline pipeline = ((CraftPlayer) player.getPlayer()).getHandle().connection.connection.channel.pipeline();
            pipeline.addBefore("packet_handler", player.getName(), handler);

            injectedPlayers.add(player.getUniqueId());
        });
    }
}
