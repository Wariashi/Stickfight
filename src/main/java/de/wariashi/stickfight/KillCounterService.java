package de.wariashi.stickfight;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scoreboard.Criteria;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;

/**
 * The {@link KillCounterService} adds a kill counter to the game.
 */
public class KillCounterService implements Listener {
	private static final String KILLS_OBJECTIVE = "stickfight_kill_counter";

	private Objective killsObjective;
	private boolean running = false;
	private Scoreboard scoreboard;
	private final Stickfight stickfight;

	/**
	 * Creates a new {@link KillCounterService}.
	 *
	 * @param stickfight the stickfight plugin that this service is associated with
	 */
	public KillCounterService(Stickfight stickfight) {
		this.stickfight = stickfight;
	}

	/**
	 * Adds new {@link Player players} to the kill counter.
	 *
	 * @param event the {@link Event} that is sent when a {@link Player} joins the server
	 */
	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent event) {
		var player = event.getPlayer();
		var name = player.getName();
		var score = killsObjective.getScore(name);
		score.setScore(0);
	}

	/**
	 * Removes {@link Player players} from the {@link Scoreboard} when they leave the server.
	 *
	 * @param event the {@link Event} that is sent when a {@link Player} leaves the server
	 */
	@EventHandler
	public void onPlayerQuitEvent(PlayerQuitEvent event) {
		var player = event.getPlayer();
		var name = player.getName();
		scoreboard.resetScores(name);
	}

	/**
	 * Starts the service if it has not already been started.
	 */
	public synchronized void start() {
		if (!running) {
			var pluginManager = Bukkit.getPluginManager();
			pluginManager.registerEvents(this, stickfight);
			initScoreboard();
			running = true;
		}
	}

	/**
	 * Stops the service by unregistering it as a {@link Listener} and removing the kill counter.
	 */
	public void stop() {
		HandlerList.unregisterAll(this);
		killsObjective.unregister();
		running = false;
	}

	/**
	 * Initializes the {@link Scoreboard} with an {@link Objective} to count kills.
	 */
	private void initScoreboard() {
		var scoreboardManager = Bukkit.getScoreboardManager();
		scoreboard = scoreboardManager.getMainScoreboard();

		// add objective
		killsObjective = scoreboard.getObjective(KILLS_OBJECTIVE);
		if (killsObjective == null) {
			killsObjective = scoreboard.registerNewObjective(KILLS_OBJECTIVE, Criteria.PLAYER_KILL_COUNT, "Kills");
		}
		killsObjective.setDisplaySlot(DisplaySlot.SIDEBAR);

		// set initial scores for online players
		var onlinePlayers = Bukkit.getOnlinePlayers().toArray(new Player[0]);
		for (var onlinePlayer : onlinePlayers) {
			var name = onlinePlayer.getName();
			var score = killsObjective.getScore(name);
			score.setScore(0);
		}
	}
}
