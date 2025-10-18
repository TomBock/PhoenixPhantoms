package com.bocktom.phoenixPhantoms;

import com.bocktom.phoenixPhantoms.util.Config;
import com.bocktom.phoenixPhantoms.util.Economy;
import com.bocktom.phoenixPhantoms.util.Permissions;
import org.bukkit.NamespacedKey;
import org.bukkit.plugin.java.JavaPlugin;

public final class PhoenixPhantoms extends JavaPlugin {

	public static PhoenixPhantoms plugin;

	public Economy economy;
	public Permissions perms;

	public final NamespacedKey phantomKey = new NamespacedKey(this, "is_phoenix_phantom");

	@Override
	public void onEnable() {
		plugin = this;
		economy = new Economy();
		perms = new Permissions();

		saveDefaultConfig();
		reloadConfig();
		new Config(this);

		getCommand("awake").setExecutor(new AwakeCommand());
		getCommand("phantom").setExecutor(new ShopCommand());

		getServer().getPluginManager().registerEvents(new PhantomSpawnListener(), this);

		new PhantomPlaceholderExpansion().register();

		if(!economy.setup()) {
			getServer().getPluginManager().disablePlugin(this);
		}

		if(!perms.setup()) {
			getServer().getPluginManager().disablePlugin(this);
		}
	}


	public void debug(String message) {
		if(plugin.getConfig().getBoolean("debug", false)) {
			plugin.getLogger().info("[DEBUG] " + message);
		}
	}
}
