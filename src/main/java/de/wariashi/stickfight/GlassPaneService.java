package de.wariashi.stickfight;

import java.util.Set;
import java.util.TimerTask;

import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scoreboard.Criteria;
import org.bukkit.scoreboard.Objective;

public class GlassPaneService implements Listener {
	private static final int RESPAWN_TIME = 60 * 20; // seconds * ticks per second
	private static final String TAG_BLACK = "black";
	private static final String TAG_BLUE = "blue";
	private static final String TAG_BROWN = "brown";
	private static final String TAG_CYAN = "cyan";
	private static final String TAG_GRAY = "gray";
	private static final String TAG_GREEN = "green";
	private static final String TAG_LIGHT_BLUE = "light_blue";
	private static final String TAG_LIGHT_GRAY = "light_gray";
	private static final String TAG_LIME = "lime";
	private static final String TAG_MAGENTA = "magenta";
	private static final String TAG_ORANGE = "orange";
	private static final String TAG_PINK = "pink";
	private static final String TAG_PLACEHOLDER = "placeholder";
	private static final String TAG_PURPLE = "purple";
	private static final String TAG_RED = "red";
	private static final String TAG_STICKFIGHT = "stickfight";
	private static final String TAG_WHITE = "white";
	private static final String TAG_YELLOW = "yellow";
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
		if (!isGlassPane(material)) {
			return;
		}

		// add placeholder
		var location = block.getLocation().add(0.5, 0.5, 0.5);
		var armorStand = world.createEntity(location, ArmorStand.class);
		armorStand.addScoreboardTag(TAG_STICKFIGHT);
		armorStand.addScoreboardTag(TAG_PLACEHOLDER);
		var colorTag = getColorTag(material);
		if (colorTag != null) {
			armorStand.addScoreboardTag(colorTag);
		}
		armorStand.setBasePlate(false);
		armorStand.setGravity(false);
		armorStand.setInvisible(true);
		armorStand.setMarker(true);
		world.addEntity(armorStand);

		// break block
		block.breakNaturally();

		// spawn particles and play a sound effect
		var onlinePlayers = Bukkit.getOnlinePlayers().toArray(new Player[0]);
		for (var player : onlinePlayers) {
			player.spawnParticle(Particle.BLOCK, location, 16, material.createBlockData());
			player.playSound(location, Sound.BLOCK_GLASS_BREAK, 1, 1);
		}
	}

	private String getColorTag(Material material) {
		return switch (material) {
			case BLACK_STAINED_GLASS_PANE -> TAG_BLACK;
			case BLUE_STAINED_GLASS_PANE -> TAG_BLUE;
			case BROWN_STAINED_GLASS_PANE -> TAG_BROWN;
			case CYAN_STAINED_GLASS_PANE -> TAG_CYAN;
			case GRAY_STAINED_GLASS_PANE -> TAG_GRAY;
			case GREEN_STAINED_GLASS_PANE -> TAG_GREEN;
			case MAGENTA_STAINED_GLASS_PANE -> TAG_MAGENTA;
			case LIGHT_BLUE_STAINED_GLASS_PANE -> TAG_LIGHT_BLUE;
			case LIGHT_GRAY_STAINED_GLASS_PANE -> TAG_LIGHT_GRAY;
			case LIME_STAINED_GLASS_PANE -> TAG_LIME;
			case ORANGE_STAINED_GLASS_PANE -> TAG_ORANGE;
			case PINK_STAINED_GLASS_PANE -> TAG_PINK;
			case PURPLE_STAINED_GLASS_PANE -> TAG_PURPLE;
			case RED_STAINED_GLASS_PANE -> TAG_RED;
			case YELLOW_STAINED_GLASS_PANE -> TAG_YELLOW;
			case WHITE_STAINED_GLASS_PANE -> TAG_WHITE;
			default -> null;
		};
	}

	private Material getMaterialForTags(Set<String> tags) {
		if (tags.contains(TAG_BLACK)) {
			return Material.BLACK_STAINED_GLASS_PANE;
		} else if (tags.contains(TAG_BLUE)) {
			return Material.BLUE_STAINED_GLASS_PANE;
		} else if (tags.contains(TAG_BROWN)) {
			return Material.BROWN_STAINED_GLASS_PANE;
		} else if (tags.contains(TAG_CYAN)) {
			return Material.CYAN_STAINED_GLASS_PANE;
		} else if (tags.contains(TAG_GRAY)) {
			return Material.GRAY_STAINED_GLASS_PANE;
		} else if (tags.contains(TAG_GREEN)) {
			return Material.GREEN_STAINED_GLASS_PANE;
		} else if (tags.contains(TAG_MAGENTA)) {
			return Material.MAGENTA_STAINED_GLASS_PANE;
		} else if (tags.contains(TAG_LIGHT_BLUE)) {
			return Material.LIGHT_BLUE_STAINED_GLASS_PANE;
		} else if (tags.contains(TAG_LIGHT_GRAY)) {
			return Material.LIGHT_GRAY_STAINED_GLASS_PANE;
		} else if (tags.contains(TAG_LIME)) {
			return Material.LIME_STAINED_GLASS_PANE;
		} else if (tags.contains(TAG_ORANGE)) {
			return Material.ORANGE_STAINED_GLASS_PANE;
		} else if (tags.contains(TAG_PINK)) {
			return Material.PINK_STAINED_GLASS_PANE;
		} else if (tags.contains(TAG_PURPLE)) {
			return Material.PURPLE_STAINED_GLASS_PANE;
		} else if (tags.contains(TAG_RED)) {
			return Material.RED_STAINED_GLASS_PANE;
		} else if (tags.contains(TAG_YELLOW)) {
			return Material.YELLOW_STAINED_GLASS_PANE;
		} else if (tags.contains(TAG_WHITE)) {
			return Material.WHITE_STAINED_GLASS_PANE;
		} else {
			return Material.GLASS_PANE;
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

	private boolean isGlassPane(Material material) {
		return switch (material) {
			case GLASS_PANE,
				 BLACK_STAINED_GLASS_PANE,
				 BLUE_STAINED_GLASS_PANE,
				 BROWN_STAINED_GLASS_PANE,
				 CYAN_STAINED_GLASS_PANE,
				 GRAY_STAINED_GLASS_PANE,
				 GREEN_STAINED_GLASS_PANE,
				 MAGENTA_STAINED_GLASS_PANE,
				 LIGHT_BLUE_STAINED_GLASS_PANE,
				 LIGHT_GRAY_STAINED_GLASS_PANE,
				 LIME_STAINED_GLASS_PANE,
				 ORANGE_STAINED_GLASS_PANE,
				 PINK_STAINED_GLASS_PANE,
				 PURPLE_STAINED_GLASS_PANE,
				 RED_STAINED_GLASS_PANE,
				 YELLOW_STAINED_GLASS_PANE,
				 WHITE_STAINED_GLASS_PANE -> true;
			default -> false;
		};
	}

	private void tick() {
		var entities = world.getEntities();
		for (var entity : entities) {
			var tags = entity.getScoreboardTags();
			if (tags.contains(TAG_STICKFIGHT) && tags.contains(TAG_PLACEHOLDER)) {
				var uuid = entity.getUniqueId();
				var score = timerObjective.getScore(uuid.toString());

				// reset glass panes
				if (RESPAWN_TIME <= score.getScore()) {
					var location = entity.getLocation();
					var block = world.getBlockAt(location);
					var material = getMaterialForTags(tags);
					block.setType(material);
					entity.remove();
				}

				// update timer
				score.setScore(score.getScore() + 1);
			}
		}
	}
}
