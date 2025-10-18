package com.bocktom.phoenixPhantoms.util;

import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.OfflinePlayer;
import org.bukkit.plugin.RegisteredServiceProvider;

import static org.bukkit.Bukkit.getLogger;
import static org.bukkit.Bukkit.getServer;

public class Economy {

	private net.milkbowl.vault.economy.Economy economy;

	public boolean setup() {
		if (getServer().getPluginManager().getPlugin("Vault") == null) {
			getLogger().severe("Vault not found! Disabling plugin...");
			return false;
		}
		RegisteredServiceProvider<net.milkbowl.vault.economy.Economy> rsp = getServer().getServicesManager().getRegistration(net.milkbowl.vault.economy.Economy.class);
		if (rsp == null) {
			getLogger().severe("No Economy Service found. Disabling plugin...");
			return false;
		}
		economy = rsp.getProvider();
		return true;
	}

	public void deposit(OfflinePlayer player, int rubineAmount) {
		EconomyResponse response = economy.depositPlayer(player, rubineAmount);
		if(response.type != EconomyResponse.ResponseType.SUCCESS) {
			getLogger().severe("Failed to deposit " + rubineAmount + " Rubine to " + player.getName() + " (" + player.getUniqueId() + "). Reason: " + response.errorMessage);
		}
	}
}
