package com.bocktom.phoenixPhantoms;

import org.bukkit.NamespacedKey;
import org.bukkit.plugin.java.JavaPlugin;

public final class PhoenixPhantoms extends JavaPlugin {

	public static PhoenixPhantoms plugin;

	public final NamespacedKey phantomKey = new NamespacedKey(this, "is_phoenix_phantom");

	@Override
	public void onEnable() {
		plugin = this;

		saveDefaultConfig();
		reloadConfig();

		getCommand("awake").setExecutor(new AwakeCommand());

		getServer().getPluginManager().registerEvents(new PhantomSpawnListener(), this);
	}

	public void debug(String message) {
		if(plugin.getConfig().getBoolean("debug", false)) {
			plugin.getLogger().info("[DEBUG] " + message);
		}
	}
}
