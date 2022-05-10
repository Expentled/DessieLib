package me.dessie.dessielib.resourcepack.webhost;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import me.dessie.dessielib.core.utils.Colors;
import me.dessie.dessielib.resourcepack.ResourcePack;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerResourcePackStatusEvent;
import org.bukkit.event.server.PluginDisableEvent;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;

/**
 * Creates an HTTP web-host on the server to serve the ResourcePack to players that join the server.
 */
public class ResourcePackServer implements HttpHandler, Listener {

    private HttpServer server;
    private ResourcePack resourcePack;
    private boolean required;
    private String address;
    private int port;

    private String kickMessage = Colors.color("&cYou are required to accept the Server Resource Pack to join this server!\nMake sure Server Resource Packs are enabled in &6Edit -> Server Resource Packs &cfor this server!");

    //Contains a list of all Players who have accepted the ResourcePack.
    private final List<UUID> acceptedPack = new ArrayList<>();

    /*
    The way Minecraft hashing works, is that it stores the Server Resources as the Download link in SHA-1 format.
    This means to update the texture pack, we need to update the URL itself.

    This can be done, by simply attaching /<zip hash> at the end of the URL when it's changed.
    This means an example URL would look like http://localhost:8080/resourcepack/c8695ca42a9c90a6187e0f1e01a0f935b4b4e0f6
    Where `c8695ca42a9c90a6187e0f1e01a0f935b4b4e0f6` is the SHA-1 Hash of the generated .zip file.
    */
    private String packUrl;

    /**
     * @param address The IP address to host the web server on.
     * @param port The port to host the web server on.
     * @param required If players are required to accept the resource pack.
     *                 If true, players who deny the pack will be kicked.
     */
    public ResourcePackServer(String address, int port, boolean required) {
        this(address, port, required, null);
    }

    /**
     * @param address The IP address to host the web server on.
     * @param port The port to host the web server on.
     * @param required If players are required to accept the resource pack.
     *                 If true, players who deny the pack will be kicked.
     * @param pack A {@link ResourcePack} to serve the players who join the server.
     */
    public ResourcePackServer(String address, int port, boolean required, ResourcePack pack) {
        try {
            this.server = HttpServer.create(new InetSocketAddress(address, port), 0);
            this.required = required;
            this.address = address;
            this.port = port;

            //Delay starting until the resource pack is set.
            if (this.resourcePack != null) {
                this.setResourcePack(pack);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * @return The port that the server is hosted on.
     */
    public int getPort() { return port; }

    /**
     * @return The IP address that the server is hosted on.
     */
    public String getAddress() {return address;}

    /**
     * @return The {@link HttpServer} that is being hosted.
     */
    public HttpServer getServer() {
        return server;
    }

    /**
     * @return The {@link ResourcePack} that is being served to Players.
     */
    public ResourcePack getResourcePack() {return resourcePack;}

    /**
     * @return If the resource pack is required to join the server.
     */
    public boolean isRequired() {return required;}

    /**
     * @return The URL to directly download the resource pack.
     */
    public String getPackUrl() {return packUrl;}

    /**
     * @return The kick message if the user declines a required resource pack.
     */
    public String getKickMessage() {
        return kickMessage;
    }

    /**
     * Sets the ResourcePack to send to Players when they join the server.
     *
     * If the server failed to start, this method will not do anything, and the pack will not be sent.
     * Note: The resource pack URL will change after this is called.
     *
     * @param pack The new ResourcePack to server players.
     */
    public void setResourcePack(ResourcePack pack) {
        this.resourcePack = pack;

        if(this.getServer() == null) {
            Bukkit.getLogger().log(Level.WARNING, "Unable to start ResourcePackServer!");
            return;
        }

        //Setup the webserver context
        String urlPath = "/resourcepack/" + pack.getBuilder().getHash();
        this.getServer().createContext(urlPath, this);
        this.packUrl = "http://" + this.getAddress() + ":" + this.getPort() + urlPath;

        //Register the EventHandler
        ResourcePack.getPlugin().getServer().getPluginManager().registerEvents(this, ResourcePack.getPlugin());
        this.getServer().start();
    }

    /**
     * Sets the kick message for when a player declines a required resource pack.
     *
     * @param kickMessage The kick message to send.
     * @return The ResourcePackServer instance.
     */
    public ResourcePackServer setKickMessage(String kickMessage) {
        this.kickMessage = kickMessage;
        return this;
    }

    /**
     * Checks if a Player accepted or declined the resource pack.
     *
     * Note: This method may return incorrect values if a /reload is ran!
     *
     * @param player The Player to check
     * @return If the provided Player accepted or declined the resource pack.
     */
    public boolean isLoadedBy(Player player) {
        return acceptedPack.contains(player.getUniqueId());
    }

    /**
     * Tells the ResourcePack that this Player has successfully loaded the ResourcePack.
     * {@link ResourcePack#isLoadedBy(Player)} will now return true.
     *
     * @param player The {@link Player} that loaded the ResourcePack.
     */
    private void addLoadedPlayer(Player player) {
        this.acceptedPack.add(player.getUniqueId());
    }

    /**
     * Tells the ResourcePack that this Player no longer has the ResourcePack loaded.
     * {@link ResourcePack#isLoadedBy(Player)} will now return false.
     *
     * Is automatically called when a Player logs out of the server.
     *
     * @param player The {@link Player} that unloaded the ResourcePack.
     */
    private void removeLoadedPlayer(Player player) {
        this.acceptedPack.remove(player.getUniqueId());
    }

    @Override
    public void handle(HttpExchange exchange) {
        try {
            exchange.getResponseHeaders().set("Content-type", "application/zip");
            exchange.sendResponseHeaders(200, this.getResourcePack().getResourcePack().length());
            OutputStream outputStream = exchange.getResponseBody();
            Files.copy(this.getResourcePack().getResourcePack().toPath(), outputStream);
            outputStream.close();
        } catch (IOException exception) {
            exception.printStackTrace();
        }
    }

    @EventHandler
    private void onResourceStatus(PlayerResourcePackStatusEvent event) {
        if(event.getStatus() == PlayerResourcePackStatusEvent.Status.SUCCESSFULLY_LOADED) {
            this.addLoadedPlayer(event.getPlayer());
        }

        if(!this.isRequired()) return;

        if(event.getStatus() == PlayerResourcePackStatusEvent.Status.DECLINED) {
            event.getPlayer().kickPlayer(this.getKickMessage());
        } else if(event.getStatus() == PlayerResourcePackStatusEvent.Status.FAILED_DOWNLOAD) {
            event.getPlayer().kickPlayer(Colors.color("&cSomething went wrong while downloading the resource pack!"));
        }
    }

    @EventHandler
    private void onDisable(PluginDisableEvent event) {
        //Shutdown the WebServer when the plugin disables.
        if(event.getPlugin() == ResourcePack.getPlugin()) {
            this.getResourcePack().getBuilder().getResourcePackServer().getServer().stop(0);
        }
    }

    @EventHandler
    private void onJoin(PlayerJoinEvent event) {
        Bukkit.getScheduler().runTask(ResourcePack.getPlugin(), () -> {
            //When the player joins, send them the resource pack.
            event.getPlayer().setResourcePack(this.getPackUrl(), this.getResourcePack().getBuilder().getHashBytes());
        });
    }

    @EventHandler
    private void onQuit(PlayerQuitEvent event) {
        this.removeLoadedPlayer(event.getPlayer());
    }
}
