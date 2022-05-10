package me.dessie.dessielib.scoreboardapi;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scoreboard.Team;

/**
 * Listen for players leaving the server to unregister their Scoreboard.
 */
public class ScoreboardListener implements Listener {
    void createListeners() {
        ScoreboardAPI.getPlugin().getServer().getPluginManager().registerEvents(this, ScoreboardAPI.getPlugin());
    }

    @EventHandler
    private void onLeave(PlayerQuitEvent event) {
        ScoreboardAPI board = ScoreboardAPI.getBoard(event.getPlayer());
        if(board != null) {
            board.stopTitleAnimation();
            board.stopAllScoreAnimation();
        }

        ScoreboardAPI.boards.remove(event.getPlayer());

        //Remove that player from all the entries.
        for(Player player : ScoreboardAPI.boards.keySet()) {
            ScoreboardAPI scoreboard = ScoreboardAPI.getBoard(player);
            for(Team team : scoreboard.getScoreboard().getTeams()) {
                team.removeEntry(event.getPlayer().getName());
            }
        }
    }
}
