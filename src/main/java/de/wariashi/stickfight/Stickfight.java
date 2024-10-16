package de.wariashi.stickfight;

import org.bukkit.*;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

public class Stickfight extends JavaPlugin implements Listener {
	private Configuration configuration;
	private GlassPaneService glassPaneService;
	private KillCounterService killCounterService;

	/**
	 * Tests whether a {@link Location} is within the area that is specified in the {@link Configuration}.
	 * If the area is unlimited, this method will always return <code>true</code>.
	 *
	 * @param location the {@link Location} to test
	 * @return <code>true</code> if the {@link Location} is within the area, <code>false</code> otherwise
	 */
	public boolean isWithinConfinedArea(Location location) {
		var isUnlimited = configuration.isUnlimited();
		if (isUnlimited) {
			return true;
		}

		var maxX = configuration.getMaxX();
		var maxY = configuration.getMaxY();
		var maxZ = configuration.getMaxZ();
		var minX = configuration.getMinX();
		var minY = configuration.getMinY();
		var minZ = configuration.getMinZ();

		var x = location.getBlockX();
		var y = location.getBlockY();
		var z = location.getBlockZ();

		return minX <= x && x <= maxX && minY <= y && y <= maxY && minZ <= z && z <= maxZ;
	}

	@Override
	public void onDisable() {
		glassPaneService.stop();
		killCounterService.stop();
	}

	@Override
	public void onEnable() {
		var worlds = Bukkit.getWorlds();
		var overworld = worlds.getFirst();

		configuration = new Configuration(this);
		glassPaneService = new GlassPaneService(this, overworld);
		glassPaneService.start();
		killCounterService = new KillCounterService(this);
		killCounterService.start();

		var pluginManager = Bukkit.getPluginManager();
		pluginManager.registerEvents(this, this);

		var scheduler = Bukkit.getScheduler();
		scheduler.runTaskTimer(this, this::giveSticks, 0, 1);
	}

	/**
	 * Cancels {@link PlayerInteractEvent interactions} of {@link Player players} that are not in
	 * {@link GameMode#CREATIVE creative mode} except for interactions with {@link Material#BELL bell blocks}.
	 *
	 * @param event the event that is called when a player interacts with a block
	 */
	@EventHandler
	public void onPlayerInteract(PlayerInteractEvent event) {
		var player = event.getPlayer();
		var gameMode = player.getGameMode();
		if (gameMode == GameMode.CREATIVE) {
			return;
		}

		var block = event.getClickedBlock();
		if (block != null) {
			var material = block.getType();
			if (material != Material.BELL) {
				event.setCancelled(true);
			}
		}
	}

	/**
	 * Kills all {@link Player players} in or below the kill layer if they are in {@link GameMode#ADVENTURE adventure mode}.
	 *
	 * @param event the event that is called when a player moves
	 * @see Configuration#getKillLayer()
	 */
	@EventHandler
	public void onPlayerMove(PlayerMoveEvent event) {
		var player = event.getPlayer();
		var gameMode = player.getGameMode();
		if (gameMode != GameMode.ADVENTURE) {
			return;
		}

		var location = player.getLocation();
		var y = location.getBlockY();
		var killLayer = configuration.getKillLayer();
		if (y <= killLayer) {
			var onlinePlayers = Bukkit.getOnlinePlayers().toArray(new Player[0]);
			for (var onlinePlayer : onlinePlayers) {
				onlinePlayer.spawnParticle(Particle.LAVA, location, 50);
				onlinePlayer.playSound(location, Sound.ENTITY_FIREWORK_ROCKET_LARGE_BLAST, 1.0f, 1.0f);
			}
			player.setHealth(0.0);
		}
	}

	/**
	 * Gives a stick with {@link Enchantment#KNOCKBACK Knockback 5 enchantment} to every {@link Player} that is online
	 * and in {@link GameMode#ADVENTURE adventure mode}.
	 * If the play area is limited, only {@link Player players} within the defined area will receive sticks.
	 */
	private void giveSticks() {
		var players = Bukkit.getOnlinePlayers().toArray(new Player[0]);
		for (var player : players) {
			var gameMode = player.getGameMode();
			var location = player.getLocation();
			if (gameMode == GameMode.ADVENTURE) {
				var inventory = player.getInventory();
				if (isWithinConfinedArea(location)) {
					var stick = new ItemStack(Material.STICK);
					stick.addUnsafeEnchantment(Enchantment.KNOCKBACK, 5);
					inventory.setItem(0, stick);
				} else {
					inventory.setItem(0, null);
				}
			}
		}
	}
}
