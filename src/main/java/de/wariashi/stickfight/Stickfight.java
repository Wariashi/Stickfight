package de.wariashi.stickfight;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

public class Stickfight extends JavaPlugin {
	@Override
	public void onEnable() {
		var scheduler = Bukkit.getScheduler();
		scheduler.runTaskTimer(this, this::giveSticks, 0, 1);
	}

	/**
	 * Gives a stick with {@link Enchantment#KNOCKBACK Knockback 5 enchantment} to every {@link Player} that is online
	 * and in {@link GameMode#ADVENTURE adventure mode}.
	 */
	private void giveSticks() {
		var players = Bukkit.getOnlinePlayers().toArray(new Player[0]);
		for (var player : players) {
			var gameMode = player.getGameMode();
			if (gameMode == GameMode.ADVENTURE) {
				var stick = new ItemStack(Material.STICK);
				stick.addUnsafeEnchantment(Enchantment.KNOCKBACK, 5);
				var inventory = player.getInventory();
				inventory.setItem(0, stick);
			}
		}
	}
}
