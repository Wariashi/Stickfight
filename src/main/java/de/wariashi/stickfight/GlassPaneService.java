package de.wariashi.stickfight;

import java.util.TimerTask;

import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scoreboard.Criteria;
import org.bukkit.scoreboard.Objective;

public class GlassPaneService implements Listener {
	private static final String PLACEHOLDER_TAG = "stickfight_placeholder";
	private static final int RESPAWN_TIME = 60 * 20; // seconds * ticks per second
	private static final String TIMER_OBJECTIVE = "stickfight_timer";

	private final JavaPlugin plugin;
	private boolean running = false;
	private Objective timerObjective;
	private TimerTask timerTask;
	private final World world;

	public GlassPaneService(JavaPlugin plugin, World world) {
		this.plugin = plugin;
		this.world = world;
		initScoreboard();
	}

	@EventHandler
	public void onPlayerMove(PlayerMoveEvent event) {
		var player = event.getPlayer();
		var gamemode = player.getGameMode();
		if (gamemode != GameMode.ADVENTURE) {
			return;
		}

		var location = event.getTo();
		if (location != null) {
			var block = world.getBlockAt(location);
			breakIfGlassPane(block);

			block = block.getRelative(BlockFace.UP);
			breakIfGlassPane(block);

			block = block.getRelative(BlockFace.UP);
			breakIfGlassPane(block);
		}
	}

	public synchronized void start() {
		if (!running) {
			// register events
			var pluginManager = Bukkit.getPluginManager();
			pluginManager.registerEvents(this, plugin);

			// create timer task
			timerTask = new TimerTask() {
				@Override
				public void run() {
					tick();
				}
			};

			// start timer
			var scheduler = Bukkit.getScheduler();
			scheduler.scheduleSyncRepeatingTask(plugin, timerTask, 0, 1);

			running = true;
		}
	}

	public void stop() {
		HandlerList.unregisterAll(this);
		if (timerTask != null) {
			timerTask.cancel();
		}
		running = false;
	}

	private void breakIfGlassPane(Block block) {
		var material = block.getType();
		if (material != Material.GLASS_PANE) {
			return;
		}

		// add placeholder
		var location = block.getLocation();
		var armorStand = (ArmorStand) world.spawnEntity(location, EntityType.ARMOR_STAND);
		armorStand.addScoreboardTag(PLACEHOLDER_TAG);
		armorStand.setBasePlate(false);
		armorStand.setGravity(false);
		armorStand.setInvisible(true);
		armorStand.setMarker(true);

		// break block
		block.breakNaturally();

		// play sound
		var onlinePlayers = Bukkit.getOnlinePlayers().toArray(new Player[0]);
		for (var player : onlinePlayers) {
			player.playSound(location, Sound.BLOCK_GLASS_BREAK, 1, 1);
		}
	}

	private void initScoreboard() {
		var scoreboardManager = Bukkit.getScoreboardManager();
		var scoreboard = scoreboardManager.getMainScoreboard();

		timerObjective = scoreboard.getObjective(TIMER_OBJECTIVE);
		if (timerObjective == null) {
			timerObjective = scoreboard.registerNewObjective(TIMER_OBJECTIVE, Criteria.DUMMY, TIMER_OBJECTIVE);
		}
	}

	private void tick() {
		var entities = world.getEntities();
		for (var entity : entities) {
			var tags = entity.getScoreboardTags();
			if (tags.contains(PLACEHOLDER_TAG)) {
				var entityId = String.valueOf(entity.getEntityId());
				var score = timerObjective.getScore(entityId);

				// reset glass panes
				if (RESPAWN_TIME <= score.getScore()) {
					var location = entity.getLocation();
					var block = world.getBlockAt(location);
					block.setType(Material.GLASS_PANE);
					entity.remove();
				}

				// update timer
				score.setScore(score.getScore() + 1);
			}
		}
	}
}
