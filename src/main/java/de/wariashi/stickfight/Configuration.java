package de.wariashi.stickfight;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Contains several parameters that can be set by the server admin.
 */
public class Configuration {
	/**
	 * The {@link FileConfiguration} that is used to configure the {@link Stickfight} plugin.
	 */
	private final FileConfiguration fileConfiguration;

	/**
	 * The config key to configure the maximum x coordinate of the play area.
	 */
	private static final String MAX_X = "play-area.max.x";

	/**
	 * The config key to configure the maximum y coordinate of the play area.
	 */
	private static final String MAX_Y = "play-area.max.y";

	/**
	 * The config key to configure the maximum z coordinate of the play area.
	 */
	private static final String MAX_Z = "play-area.max.z";

	/**
	 * The config key to configure the minimum x coordinate of the play area.
	 */
	private static final String MIN_X = "play-area.min.x";

	/**
	 * The config key to configure the minimum y coordinate of the play area.
	 */
	private static final String MIN_Y = "play-area.min.y";

	/**
	 * The config key to configure the minimum z coordinate of the play area if.
	 */
	private static final String MIN_Z = "play-area.min.z";

	/**
	 * The config key to configure whether the play area should be unlimited.
	 */
	private static final String UNLIMITED = "play-area.unlimited";

	/**
	 * Creates a new {@link Configuration} object.
	 *
	 * @param plugin the {@link JavaPlugin} whose {@link FileConfiguration} should be used
	 */
	public Configuration(JavaPlugin plugin) {
		fileConfiguration = plugin.getConfig();
		var options = fileConfiguration.options();
		options.copyDefaults(true);
		addDefaults();
		plugin.saveConfig();
	}

	/**
	 * @return the maximum x coordinate of the play area
	 */
	public long getMaxX() {
		return fileConfiguration.getLong(MAX_X);
	}

	/**
	 * @return the maximum y coordinate of the play area
	 */
	public long getMaxY() {
		return fileConfiguration.getLong(MAX_Y);
	}

	/**
	 * @return the maximum z coordinate of the play area
	 */
	public long getMaxZ() {
		return fileConfiguration.getLong(MAX_Z);
	}

	/**
	 * @return the minimum x coordinate of the play area
	 */
	public long getMinX() {
		return fileConfiguration.getLong(MIN_X);
	}

	/**
	 * @return the minimum y coordinate of the play area
	 */
	public long getMinY() {
		return fileConfiguration.getLong(MIN_Y);
	}

	/**
	 * @return the minimum z coordinate of the play area
	 */
	public long getMinZ() {
		return fileConfiguration.getLong(MIN_Z);
	}

	/**
	 * @return <code>true</code> it the play area should be unlimited, <code>false</code> otherwise
	 */
	public boolean isUnlimited() {
		return fileConfiguration.getBoolean(UNLIMITED);
	}

	/**
	 * Adds the missing default values to the config file.
	 */
	private void addDefaults() {
		fileConfiguration.addDefault(MAX_X, 10);
		fileConfiguration.addDefault(MAX_Y, 10);
		fileConfiguration.addDefault(MAX_Z, 10);
		fileConfiguration.addDefault(MIN_X, -10);
		fileConfiguration.addDefault(MIN_Y, -10);
		fileConfiguration.addDefault(MIN_Z, -10);
		fileConfiguration.addDefault(UNLIMITED, true);
	}
}
