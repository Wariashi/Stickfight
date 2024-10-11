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
	private static final String PLACEHOLDER_TAG_BLACK = "stickfight_placeholder_black";
	private static final String PLACEHOLDER_TAG_BLUE = "stickfight_placeholder_blue";
	private static final String PLACEHOLDER_TAG_BROWN = "stickfight_placeholder_brown";
	private static final String PLACEHOLDER_TAG_CLEAR = "stickfight_placeholder";
	private static final String PLACEHOLDER_TAG_CYAN = "stickfight_placeholder_cyan";
	private static final String PLACEHOLDER_TAG_GRAY = "stickfight_placeholder_gray";
	private static final String PLACEHOLDER_TAG_GREEN = "stickfight_placeholder_green";
	private static final String PLACEHOLDER_TAG_LIGHT_BLUE = "stickfight_placeholder_light_blue";
	private static final String PLACEHOLDER_TAG_LIGHT_GRAY = "stickfight_placeholder_light_gray";
	private static final String PLACEHOLDER_TAG_LIME = "stickfight_placeholder_lime";
	private static final String PLACEHOLDER_TAG_MAGENTA = "stickfight_placeholder_magenta";
	private static final String PLACEHOLDER_TAG_ORANGE = "stickfight_placeholder_orange";
	private static final String PLACEHOLDER_TAG_PINK = "stickfight_placeholder_pink";
	private static final String PLACEHOLDER_TAG_PURPLE = "stickfight_placeholder_purple";
	private static final String PLACEHOLDER_TAG_RED = "stickfight_placeholder_red";
	private static final String PLACEHOLDER_TAG_WHITE = "stickfight_placeholder_white";
	private static final String PLACEHOLDER_TAG_YELLOW = "stickfight_placeholder_yellow";
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
		var placeholderTag = getTagForMaterial(material);
		if (placeholderTag == null) {
			return;
		}

		// add placeholder
		var location = block.getLocation();
		var armorStand = (ArmorStand) world.spawnEntity(location, EntityType.ARMOR_STAND);
		armorStand.addScoreboardTag(placeholderTag);
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

	private Material getMaterialForTag(String tag) {
		return switch (tag) {
			case PLACEHOLDER_TAG_CLEAR -> Material.GLASS_PANE;
			case PLACEHOLDER_TAG_BLACK -> Material.BLACK_STAINED_GLASS_PANE;
			case PLACEHOLDER_TAG_BLUE -> Material.BLUE_STAINED_GLASS_PANE;
			case PLACEHOLDER_TAG_BROWN -> Material.BROWN_STAINED_GLASS_PANE;
			case PLACEHOLDER_TAG_CYAN -> Material.CYAN_STAINED_GLASS_PANE;
			case PLACEHOLDER_TAG_GRAY -> Material.GRAY_STAINED_GLASS_PANE;
			case PLACEHOLDER_TAG_GREEN -> Material.GREEN_STAINED_GLASS_PANE;
			case PLACEHOLDER_TAG_MAGENTA -> Material.MAGENTA_STAINED_GLASS_PANE;
			case PLACEHOLDER_TAG_LIGHT_BLUE -> Material.LIGHT_BLUE_STAINED_GLASS_PANE;
			case PLACEHOLDER_TAG_LIGHT_GRAY -> Material.LIGHT_GRAY_STAINED_GLASS_PANE;
			case PLACEHOLDER_TAG_LIME -> Material.LIME_STAINED_GLASS_PANE;
			case PLACEHOLDER_TAG_ORANGE -> Material.ORANGE_STAINED_GLASS_PANE;
			case PLACEHOLDER_TAG_PINK -> Material.PINK_STAINED_GLASS_PANE;
			case PLACEHOLDER_TAG_PURPLE -> Material.PURPLE_STAINED_GLASS_PANE;
			case PLACEHOLDER_TAG_RED -> Material.RED_STAINED_GLASS_PANE;
			case PLACEHOLDER_TAG_YELLOW -> Material.YELLOW_STAINED_GLASS_PANE;
			case PLACEHOLDER_TAG_WHITE -> Material.WHITE_STAINED_GLASS_PANE;
			default -> null;
		};
	}

	private String getTagForMaterial(Material material) {
		return switch (material) {
			case GLASS_PANE -> PLACEHOLDER_TAG_CLEAR;
			case BLACK_STAINED_GLASS_PANE -> PLACEHOLDER_TAG_BLACK;
			case BLUE_STAINED_GLASS_PANE -> PLACEHOLDER_TAG_BLUE;
			case BROWN_STAINED_GLASS_PANE -> PLACEHOLDER_TAG_BROWN;
			case CYAN_STAINED_GLASS_PANE -> PLACEHOLDER_TAG_CYAN;
			case GRAY_STAINED_GLASS_PANE -> PLACEHOLDER_TAG_GRAY;
			case GREEN_STAINED_GLASS_PANE -> PLACEHOLDER_TAG_GREEN;
			case MAGENTA_STAINED_GLASS_PANE -> PLACEHOLDER_TAG_MAGENTA;
			case LIGHT_BLUE_STAINED_GLASS_PANE -> PLACEHOLDER_TAG_LIGHT_BLUE;
			case LIGHT_GRAY_STAINED_GLASS_PANE -> PLACEHOLDER_TAG_LIGHT_GRAY;
			case LIME_STAINED_GLASS_PANE -> PLACEHOLDER_TAG_LIME;
			case ORANGE_STAINED_GLASS_PANE -> PLACEHOLDER_TAG_ORANGE;
			case PINK_STAINED_GLASS_PANE -> PLACEHOLDER_TAG_PINK;
			case PURPLE_STAINED_GLASS_PANE -> PLACEHOLDER_TAG_PURPLE;
			case RED_STAINED_GLASS_PANE -> PLACEHOLDER_TAG_RED;
			case YELLOW_STAINED_GLASS_PANE -> PLACEHOLDER_TAG_YELLOW;
			case WHITE_STAINED_GLASS_PANE -> PLACEHOLDER_TAG_WHITE;
			default -> null;
		};
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
			for (var tag : tags) {
				var material = getMaterialForTag(tag);
				if (material != null) {
					var entityId = String.valueOf(entity.getEntityId());
					var score = timerObjective.getScore(entityId);

					// reset glass panes
					if (RESPAWN_TIME <= score.getScore()) {
						var location = entity.getLocation();
						var block = world.getBlockAt(location);
						block.setType(material);
						entity.remove();
					}

					// update timer
					score.setScore(score.getScore() + 1);
				}
			}
		}
	}
}
