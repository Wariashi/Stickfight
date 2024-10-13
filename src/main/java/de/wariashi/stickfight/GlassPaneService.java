package de.wariashi.stickfight;

import java.util.Set;
import java.util.TimerTask;

import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.scoreboard.Criteria;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;

/**
 * The {@link GlassPaneService} breaks glass panes within the play area when a {@link Player} hits them.
 * Broken glass panes will be restored after a while.
 */
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

	private boolean running = false;
	private final Stickfight stickfight;
	private Objective timerObjective;
	private TimerTask timerTask;
	private final World world;

	/**
	 * Creates a new {@link GlassPaneService}.
	 *
	 * @param stickfight the stickfight plugin that this service is associated with
	 * @param world      the world in which glass panes are supposed to break when a {@link Player} hits them
	 */
	public GlassPaneService(Stickfight stickfight, World world) {
		this.stickfight = stickfight;
		this.world = world;
		initScoreboard();
	}

	/**
	 * Breaks glass panes and replaces them with invisible placeholders
	 * if a {@link Player} in {@link GameMode#ADVENTURE adventure game mode} hits them.
	 *
	 * @param event the {@link Event} that is sent when a {@link Player} moves
	 */
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

	/**
	 * Starts the service if it has not already been started.
	 */
	public synchronized void start() {
		if (!running) {
			// register events
			var pluginManager = Bukkit.getPluginManager();
			pluginManager.registerEvents(this, stickfight);

			// create timer task
			timerTask = new TimerTask() {
				@Override
				public void run() {
					tick();
				}
			};

			// start timer
			var scheduler = Bukkit.getScheduler();
			scheduler.scheduleSyncRepeatingTask(stickfight, timerTask, 0, 1);

			running = true;
		}
	}

	/**
	 * Stops the service by canceling all timers and unregistering it as a {@link Listener}.
	 */
	public void stop() {
		HandlerList.unregisterAll(this);
		if (timerTask != null) {
			timerTask.cancel();
		}
		running = false;
	}

	/**
	 * Breaks a {@link Block} and replaces it with an invisible placeholder if it is a type of glass pane and located within the play area.
	 *
	 * @param block the {@link Block} to break
	 */
	private void breakIfGlassPane(Block block) {
		// ignore blocks that are no glass panes
		var material = block.getType();
		if (!isGlassPane(material)) {
			return;
		}

		// ignore blocks outside the play area
		var location = block.getLocation().add(0.5, 0.5, 0.5);
		if (!stickfight.isWithinConfinedArea(location)) {
			return;
		}

		// add placeholder
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

	/**
	 * Returns a String describing the color of a glass pane.
	 *
	 * @param material the {@link Material} to check
	 * @return a String describing the color of a glass pane or <code>null</code> if it is a clear glass pane or not a glass pane at all
	 */
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

	/**
	 * Looks up, which {@link Material} should be used to replace a placeholder based on the tags of the placeholder.
	 *
	 * @param tags a {@link Set} of tags
	 * @return a glass pane {@link Material}
	 */
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

	/**
	 * Initializes the {@link Scoreboard} with an {@link Objective} to track the remaining time until placeholders are replaced with glass panes.
	 */
	private void initScoreboard() {
		var scoreboardManager = Bukkit.getScoreboardManager();
		var scoreboard = scoreboardManager.getMainScoreboard();

		timerObjective = scoreboard.getObjective(TIMER_OBJECTIVE);
		if (timerObjective == null) {
			timerObjective = scoreboard.registerNewObjective(TIMER_OBJECTIVE, Criteria.DUMMY, TIMER_OBJECTIVE);
		}
	}

	/**
	 * Checks whether a given {@link Material} is a type of glass pane.
	 *
	 * @param material the {@link Material} to check
	 * @return <code>true</code> if the {@link Material} is a type of glass pane, <code>false</code> otherwise
	 */
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

	/**
	 * Updates the timers for placeholder entities.
	 * When they reach the {@link GlassPaneService#RESPAWN_TIME respawn time}, the placeholders are replaced with glass panes.
	 */
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
