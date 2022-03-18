package me.dessie.dessielib.resourcepack.listeners;

import com.google.common.util.concurrent.AtomicDouble;
import me.dessie.dessielib.packeteer.PacketListener;
import me.dessie.dessielib.packeteer.PacketeerHandler;
import me.dessie.dessielib.resourcepack.ResourcePack;
import me.dessie.dessielib.resourcepack.assets.BlockStateAsset;
import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.game.ClientboundBlockDestructionPacket;
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket;
import org.bukkit.Bukkit;
import org.bukkit.Effect;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.v1_18_R2.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockDamageEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class BlockListener implements Listener, PacketListener {

    private final JavaPlugin plugin;
    private final ResourcePack pack;

    private final Map<Block, BukkitTask> breaking = new HashMap<>();
    private final Map<Block, Integer> id = new HashMap<>();

    public BlockListener(JavaPlugin plugin, ResourcePack pack) {
        this.plugin = plugin;
        this.pack = pack;
    }

    public ResourcePack getPack() {return pack;}
    public JavaPlugin getPlugin() {return plugin;}

    @PacketeerHandler
    public void onBlockDestroy(ServerboundPlayerActionPacket packet, Player player) {
        //TODO Use new BlockDamageAbortEvent provided by Spigot 1.18.
        if(packet.getAction() == ServerboundPlayerActionPacket.Action.ABORT_DESTROY_BLOCK || packet.getAction() == ServerboundPlayerActionPacket.Action.STOP_DESTROY_BLOCK) {
            BlockPos pos = packet.getPos();
            Bukkit.getPluginManager().callEvent(new BlockStopDamageEvent(player, player.getWorld().getBlockAt(pos.getX(), pos.getY(), pos.getZ())));
        }
    }

    @EventHandler
    public void onStopBlockDamage(BlockStopDamageEvent event) {
        Player player = event.getPlayer();

        if(!breaking.containsKey(event.getBlock())) return;

        breaking.get(event.getBlock()).cancel();
        Block block = event.getBlock();

        ClientboundBlockDestructionPacket packet = new ClientboundBlockDestructionPacket(id.get(event.getBlock()), new BlockPos(block.getX(), block.getY(), block.getZ()), -1);
        ((CraftPlayer) event.getPlayer()).getHandle().connection.send(packet);
        player.removePotionEffect(PotionEffectType.SLOW_DIGGING);
    }

    @EventHandler
    public void onBlockDamage(BlockDamageEvent event) {
        Block block = event.getBlock();

        BlockStateAsset asset = null;
        for(BlockStateAsset blockStateAsset : this.getPack().getBuilder().getAssetsOf(BlockStateAsset.class)) {
            if(blockStateAsset.blockMatches(event.getBlock())) {
                asset = blockStateAsset;
            }
        }

        if(asset == null) return;

        double breakSpeed = asset.getBreakSpeed(event.getItemInHand(), event.getPlayer());

        //If the block never breaks, or it breaks instantly, return.
        if(breakSpeed == 0) return;
        if(breakSpeed > 1) {
            event.setInstaBreak(true);
            doBreak(event.getBlock(), event.getPlayer(), asset, event.getItemInHand());
            return;
        }

        Bukkit.getScheduler().runTaskLater(this.getPlugin(), () -> {
            event.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.SLOW_DIGGING, Integer.MAX_VALUE, -1, false, false, false));
        }, 2);

        AtomicDouble progress = new AtomicDouble(0);
        int breakId = new Random().nextInt();
        id.put(event.getBlock(), breakId);

        BlockStateAsset finalAsset = asset;
        breaking.put(event.getBlock(), new BukkitRunnable() {
            @Override
            public void run() {
                progress.getAndAdd(finalAsset.getBreakSpeed(event.getItemInHand(), event.getPlayer()));
                if(progress.get() >= 1) {

                    //Execute the break event
                    doBreak(block, event.getPlayer(), finalAsset, event.getItemInHand());

                    //Reset the destruction on the block
                    ClientboundBlockDestructionPacket packet = new ClientboundBlockDestructionPacket(breakId, new BlockPos(block.getX(), block.getY(), block.getZ()), -1);
                    ((CraftPlayer) event.getPlayer()).getHandle().connection.send(packet);

                    //Cancel the task
                    this.cancel();
                } else {
                    ClientboundBlockDestructionPacket packet = new ClientboundBlockDestructionPacket(breakId, new BlockPos(block.getX(), block.getY(), block.getZ()), ((int) (progress.get() / 0.1)));
                    ((CraftPlayer) event.getPlayer()).getHandle().connection.send(packet);
                }
            }
        }.runTaskTimer(this.getPlugin(), 1, 1));
    }

    private void doBreak(Block block, Player player, BlockStateAsset asset, ItemStack item) {
        //Fire the event
        BlockBreakEvent blockBreakEvent = new BlockBreakEvent(block, player);
        Bukkit.getPluginManager().callEvent(blockBreakEvent);

        if(blockBreakEvent.isCancelled()) return;

        //Play the break effect
        block.getWorld().playEffect(block.getLocation(), Effect.STEP_SOUND, block.getType());

        //If there's custom drops, don't drop the normal item, otherwise, drop the normal item.
        if(asset.getDrops().isEmpty()) {
            block.breakNaturally(item);
        } else {
            block.setType(Material.AIR);
        }
    }

}
