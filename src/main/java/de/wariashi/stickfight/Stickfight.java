package de.wariashi.stickfight;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

public class Stickfight extends JavaPlugin {
	private Configuration configuration;

	@Override
	public void onEnable() {
		configuration = new Configuration(this);

		var scheduler = Bukkit.getScheduler();
		scheduler.runTaskTimer(this, this::giveSticks, 0, 1);
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
			if (gameMode == GameMode.ADVENTURE && isWithinConfinedArea(player)) {
				var stick = new ItemStack(Material.STICK);
				stick.addUnsafeEnchantment(Enchantment.KNOCKBACK, 5);
				var inventory = player.getInventory();
				inventory.setItem(0, stick);
			}
		}
	}

	/**
	 * Tests whether an {@link Entity} is within the area that is specified in the {@link Configuration}.
	 * If the area is unlimited, this method will always return <code>true</code>.
	 *
	 * @param entity the {@link Entity} to test
	 * @return <code>true</code> if the {@link Entity} is within the area, <code>false</code> otherwise
	 */
	private boolean isWithinConfinedArea(Entity entity) {
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

		var location = entity.getLocation();
		var x = location.getBlockX();
		var y = location.getBlockY();
		var z = location.getBlockZ();

		return minX <= x && x <= maxX && minY <= y && y <= maxY && minZ <= z && z <= maxZ;
	}
}
